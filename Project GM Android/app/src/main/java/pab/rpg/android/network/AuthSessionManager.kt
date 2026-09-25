package pab.rpg.android.network

// Abstraction over how the app authenticates against the backend, implemented differently per build
// flavor: "local" is a no-op (keeps using DevIdentityInterceptor's X-Dev-Player-Id), "cloud" drives a
// native username/password login against Project GM Auth's custom "password" grant — no browser
// involved (see CloudAuthSessionManager in the cloud source set).
interface AuthSessionManager {
    fun isLoggedIn(): Boolean

    fun accessToken(): String?

    suspend fun login(username: String, password: String): Result<Unit>

    suspend fun register(username: String, password: String, email: String?): Result<Unit>

    fun logout()

    // Blocking; safe to call from OkHttp's background Authenticator thread. Null if refresh failed.
    fun refreshIfNeeded(): String?
}
