package com.hitchy.core.data

internal data class FunctionsExceptionInfo(
    val code: String?,
    val details: Any?,
)

internal expect fun extractFunctionsExceptionInfo(throwable: Throwable): FunctionsExceptionInfo
