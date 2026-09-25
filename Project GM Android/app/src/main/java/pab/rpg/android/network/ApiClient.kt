package pab.rpg.android.network

import android.content.Context
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
            .addInterceptor(DevIdentityInterceptor())
            .addInterceptor(AuthInterceptor(authSessionManager))
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

