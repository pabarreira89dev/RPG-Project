package pab.rpg.android.network

import okhttp3.Interceptor
import okhttp3.Response

// Runs after AuthAuthenticator's retry-on-401 gives up, so a 401 seen here is unrecoverable;
// 403 never reaches Authenticator at all (OkHttp only invokes it for 401/407), so it's caught here too.
class AuthFailureInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        if (response.code == 401 || response.code == 403) {
            AuthEvents.notifyAuthFailure()
        }
        return response
    }
}
