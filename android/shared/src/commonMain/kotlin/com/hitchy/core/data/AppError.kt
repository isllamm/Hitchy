package com.hitchy.core.data

sealed interface AppError {
    data object Network : AppError
    data object NotSignedIn : AppError
    data class Reason(val code: String, val extra: String? = null) : AppError
    data class Unknown(val message: String?) : AppError
}
