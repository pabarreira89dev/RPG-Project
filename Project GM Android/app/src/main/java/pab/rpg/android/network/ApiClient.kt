package pab.rpg.android.network

import android.content.Context
import java.util.concurrent.TimeUnit
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import pab.rpg.android.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

object ApiClient {

    private val json = Json { ignoreUnknownKeys = true }

    lateinit var authSessionManager: AuthSessionManager
        private set

    fun init(context: Context) {
        if (::authSessionManager.isInitialized) return
        authSessionManager = AuthSessionManagerProvider.create(context.applicationContext)
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            // OkHttp defaults (10s) are shorter than the backend's own OpenAI budget (connect+read up to 25s,
            // see OPENAI_CONNECT_TIMEOUT/OPENAI_READ_TIMEOUT), which caused client timeouts on slow-but-successful narrations.
            .connectTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .readTimeout(45, TimeUnit.SECONDS)
            .addInterceptor(DevIdentityInterceptor())
            .addInterceptor(AuthInterceptor(authSessionManager))
            .addInterceptor(AuthFailureInterceptor())
            .authenticator(AuthAuthenticator(authSessionManager))
            .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
                }
            }
            .build()
    }

    val gameApi: GameApi by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(GameApi::class.java)
    }
}

