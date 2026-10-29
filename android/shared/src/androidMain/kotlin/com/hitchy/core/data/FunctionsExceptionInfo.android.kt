package com.hitchy.core.data

import com.google.firebase.functions.FirebaseFunctionsException

internal actual fun extractFunctionsExceptionInfo(throwable: Throwable): FunctionsExceptionInfo {
    val candidates = listOfNotNull(throwable, throwable.cause)
    for (candidate in candidates) {
        if (candidate is FirebaseFunctionsException) {
            return FunctionsExceptionInfo(
                code = candidate.code.name,
                details = candidate.details
            )
        }
        try {
            val codeMethod = candidate.javaClass.methods.firstOrNull { it.name == "getCode" || it.name == "code" }
            val detailsMethod = candidate.javaClass.methods.firstOrNull { it.name == "getDetails" || it.name == "details" }
            val code = codeMethod?.invoke(candidate)?.toString()
            val details = detailsMethod?.invoke(candidate)
            if (code != null || details != null) {
                return FunctionsExceptionInfo(code = code, details = details)
            }
        } catch (_: Throwable) {}
    }
    return FunctionsExceptionInfo(null, null)
}
