package pab.rpg.android.network

import okhttp3.Interceptor
import okhttp3.Response

// Adds the local-dev identity header the backend's DevelopmentIdentityFilter reads (see SecurityConfig).
// devPlayerId == null means "omit the header", which the backend then resolves to its fixed dev identity.
// Replace with a real Authorization: Bearer <jwt> interceptor once the cloud profile/IdP is wired up.
class DevIdentityInterceptor(private val devPlayerId: String? = null) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder().apply {
            if (!devPlayerId.isNullOrBlank()) {
                addHeader("X-Dev-Player-Id", devPlayerId)
            }
        }.build()
        return chain.proceed(request)
    }
}
