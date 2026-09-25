package pab.rpg.android.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Standard OAuth2 token endpoint response (RFC 6749 §5.1) — field names are dictated by the spec, not us.
@Serializable
data class TokenResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String? = null,
    @SerialName("token_type") val tokenType: String? = null,
    @SerialName("expires_in") val expiresIn: Long? = null,
    val scope: String? = null
)

// Standard OAuth2 token endpoint error response (RFC 6749 §5.2).
@Serializable
data class OAuth2ErrorResponse(
    val error: String,
    @SerialName("error_description") val errorDescription: String? = null
)

@Serializable
data class RegisterRequest(
    val username: String,
    val password: String,
    val email: String? = null
)
