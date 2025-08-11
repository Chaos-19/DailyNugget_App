package com.chaosdev.devbuddy.ui.common

/**
 * A generic sealed class that represents the state of an operation,
 * typically used for asynchronous tasks like network requests.
 *
 * @param T The type of data expected in the successful case.
 */
sealed class Response<out T> { // Use 'out T' for covariance, allowing assignment to Response<Any> if needed

    /**
     * Represents a successful operation.
     * @param data The data returned by the operation.
     */
    data class Success<out T>(val data: T) : Response<T>()

    /**
     * Represents a failed operation.
     * @param message A descriptive message about the error.
     * @param throwable An optional Throwable that caused the error, for debugging or further processing.
     */
    data class Error(val message: String, val throwable: Throwable? = null) : Response<Nothing>()
    // Use Response<Nothing> because Error state doesn't hold data of type T

    /**
     * Represents an operation that is currently in progress.
     */
    object Loading : Response<Nothing>()
    // Use Response<Nothing> because Loading state doesn't hold data of type T
}
