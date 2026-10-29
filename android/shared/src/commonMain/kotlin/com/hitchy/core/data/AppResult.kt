package com.hitchy.core.data

sealed interface AppResult<out T> {
    data class Ok<T>(val value: T) : AppResult<T>
    data class Err(val error: AppError) : AppResult<Nothing>

    val isOk: Boolean get() = this is Ok<T>
    val isErr: Boolean get() = this is Err

    fun getOrNull(): T? = when (this) {
        is Ok -> value
        is Err -> null
    }
}

inline fun <T, R> AppResult<T>.map(transform: (value: T) -> R): AppResult<R> = when (this) {
    is AppResult.Ok -> AppResult.Ok(transform(value))
    is AppResult.Err -> this
}

inline fun <T, R> AppResult<T>.flatMap(transform: (value: T) -> AppResult<R>): AppResult<R> = when (this) {
    is AppResult.Ok -> transform(value)
    is AppResult.Err -> this
}

inline fun <T> AppResult<T>.onSuccess(action: (value: T) -> Unit): AppResult<T> {
    if (this is AppResult.Ok) action(value)
    return this
}

inline fun <T> AppResult<T>.onFailure(action: (error: AppError) -> Unit): AppResult<T> {
    if (this is AppResult.Err) action(error)
    return this
}
