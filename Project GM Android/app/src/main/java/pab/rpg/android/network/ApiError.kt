package pab.rpg.android.network

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import retrofit2.HttpException

// Mirrors GlobalExceptionHandler's ApiError shape (see Project GM/docs-agent/api-contracts.md).
// Does NOT apply to 401/403: Spring Security's default body doesn't follow this shape yet.
@Serializable
data class ApiError(
    val code: String,
    val message: String,
    val timestamp: String? = null,
    val correlationId: String? = null
)

class ApiException(val apiError: ApiError, cause: Throwable? = null) : Exception(apiError.message, cause)

private val errorJson = Json { ignoreUnknownKeys = true }

fun Throwable.toApiExceptionOrNull(): ApiException? {
    if (this !is HttpException) return null
    val body = response()?.errorBody()?.string() ?: return null
    return runCatching { errorJson.decodeFromString(ApiError.serializer(), body) }
        .getOrNull()
        ?.let { ApiException(it, this) }
}
