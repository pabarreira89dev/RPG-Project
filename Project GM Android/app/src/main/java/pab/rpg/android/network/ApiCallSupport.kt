package pab.rpg.android.network

// Shared by every repository: maps a failed call's HttpException body into an ApiException when possible.
suspend fun <T> apiCall(block: suspend () -> T): Result<T> = try {
    Result.success(block())
} catch (error: Throwable) {
    Result.failure(error.toApiExceptionOrNull() ?: error)
}
