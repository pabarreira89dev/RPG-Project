package pab.rpg.android.network

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import pab.rpg.android.BuildConfig
import pab.rpg.android.network.dto.OAuth2ErrorResponse
import pab.rpg.android.network.dto.RegisterRequest
import pab.rpg.android.network.dto.TokenResponse

// Drives Project GM Auth's custom "password" OAuth2 grant directly (no browser/Custom Tabs — see
// PasswordGrantAuthenticationProvider on the backend), and persists tokens in
// EncryptedSharedPreferences so a login survives app restarts.
class CloudAuthSessionManager(context: Context) : AuthSessionManager {

    private val appContext = context.applicationContext
    // No redirects expected from this JSON API; if the server ever sends one (e.g. a misconfigured
    // filter chain redirecting to /login), surface it as a normal non-2xx response instead of letting
    // OkHttp keep following it until it throws "Too many follow-up requests".
    private val httpClient = OkHttpClient.Builder()
        .followRedirects(false)
        .followSslRedirects(false)
        .build()
    private val json = Json { ignoreUnknownKeys = true }

    private val prefs by lazy {
        val masterKey = MasterKey.Builder(appContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            appContext,
            "auth_session",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    @Volatile
    private var accessToken: String? = null

    @Volatile
    private var refreshToken: String? = null

    init {
        accessToken = prefs.getString(KEY_ACCESS_TOKEN, null)
        refreshToken = prefs.getString(KEY_REFRESH_TOKEN, null)
    }

    override fun isLoggedIn(): Boolean = accessToken != null

    override fun accessToken(): String? = accessToken

    override suspend fun login(username: String, password: String): Result<Unit> = withContext(Dispatchers.IO) {
        val body = FormBody.Builder()
            .add("grant_type", "password")
            .add("client_id", BuildConfig.AUTH_CLIENT_ID)
            .add("client_secret", BuildConfig.AUTH_CLIENT_SECRET)
            .add("username", username)
            .add("password", password)
            .add("scope", "game.api")
            .build()
        runCatching { exchangeForToken(body) }
    }

    override suspend fun register(username: String, password: String, email: String?): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val payload = json.encodeToString(
                    RegisterRequest.serializer(),
                    RegisterRequest(username, password, email)
                )
                val request = Request.Builder()
                    .url(BuildConfig.AUTH_BASE_URL + "api/v1/register")
                    .post(payload.toRequestBody("application/json".toMediaType()))
                    .build()
                httpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        throw AuthException(describeRegisterError(response.code, response.body?.string().orEmpty()))
                    }
                }
            }
        }

    override fun logout() {
        accessToken = null
        refreshToken = null
        persist()
    }

    // OkHttp's Authenticator calls this synchronously on its own background thread, so a plain blocking
    // call is correct here — unlike login()/register(), which are suspend functions called from a
    // ViewModel's coroutine (see withContext(Dispatchers.IO) above).
    override fun refreshIfNeeded(): String? {
        val currentRefreshToken = refreshToken ?: return null
        val body = FormBody.Builder()
            .add("grant_type", "refresh_token")
            .add("client_id", BuildConfig.AUTH_CLIENT_ID)
            .add("client_secret", BuildConfig.AUTH_CLIENT_SECRET)
            .add("refresh_token", currentRefreshToken)
            .build()
        return runCatching { exchangeForToken(body); accessToken }.getOrNull()
    }

    private fun exchangeForToken(body: FormBody) {
        val request = Request.Builder()
            .url(BuildConfig.AUTH_BASE_URL + "oauth2/token")
            .post(body)
            .build()
        httpClient.newCall(request).execute().use { response ->
            val responseBody = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw AuthException(describeTokenError(response.code, responseBody))
            }
            val tokenResponse = json.decodeFromString(TokenResponse.serializer(), responseBody)
            accessToken = tokenResponse.accessToken
            refreshToken = tokenResponse.refreshToken ?: refreshToken
            persist()
        }
    }

    private fun describeTokenError(statusCode: Int, body: String): String {
        val error = runCatching { json.decodeFromString(OAuth2ErrorResponse.serializer(), body) }.getOrNull()
        return when (error?.error) {
            "invalid_grant" -> "Usuario o contraseña incorrectos."
            null -> "No se pudo iniciar sesión (HTTP $statusCode)."
            else -> error.errorDescription ?: "No se pudo iniciar sesión (${error.error})."
        }
    }

    private fun describeRegisterError(statusCode: Int, body: String): String {
        val message = runCatching {
            json.parseToJsonElement(body).jsonObject["message"]?.jsonPrimitive?.content
        }.getOrNull()
        return message ?: "No se pudo crear la cuenta (HTTP $statusCode)."
    }

    private fun persist() {
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .apply()
    }

    private companion object {
        const val KEY_ACCESS_TOKEN = "access_token"
        const val KEY_REFRESH_TOKEN = "refresh_token"
    }
}
