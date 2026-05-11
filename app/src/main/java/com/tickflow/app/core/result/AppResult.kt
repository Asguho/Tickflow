package com.tickflow.app.core.result

sealed interface AppResult<out T> {
    data class Success<T>(val value: T) : AppResult<T>
    data class Failure(val reason: String, val throwable: Throwable? = null) : AppResult<Nothing>
}
