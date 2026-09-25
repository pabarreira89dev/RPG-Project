package pab.rpg.android.network

import okhttp3.Interceptor
import okhttp3.Response

// Attaches the OAuth2 access token when one exists (the "cloud" flavor); a no-op otherwise (e.g. "local",
// where AuthSessionManager never returns a token and DevIdentityInterceptor is used instead).
class AuthInterceptor(private val authSessionManager: AuthSessionManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = authSessionManager.accessToken()
        val request = chain.request().newBuilder().apply {
            if (!token.isNullOrBlank()) {
                addHeader("Authorization", "Bearer $token")
            }
        }.build()
        return chain.proceed(request)
    }
}
