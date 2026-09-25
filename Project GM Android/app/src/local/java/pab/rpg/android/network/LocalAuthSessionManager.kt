package pab.rpg.android.network

// No-op: the "local" flavor keeps using DevIdentityInterceptor's X-Dev-Player-Id header, no real login.
class LocalAuthSessionManager : AuthSessionManager {
    override fun isLoggedIn(): Boolean = true

    override fun accessToken(): String? = null

    override suspend fun login(username: String, password: String): Result<Unit> = Result.success(Unit)

    override suspend fun register(username: String, password: String, email: String?): Result<Unit> = Result.success(Unit)

    override fun logout() {}

    override fun refreshIfNeeded(): String? = null
}
