package com.hitchy.core.data

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.functions.FirebaseFunctions
import dev.gitlive.firebase.functions.functions
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer

interface FunctionsClient {
    suspend fun <I : Any, O : Any> call(
        name: String,
        input: I,
        inputSerializer: KSerializer<I>,
        outputSerializer: KSerializer<O>
    ): AppResult<O>
}

suspend inline fun <reified I : Any, reified O : Any> FunctionsClient.call(
    name: String,
    input: I
): AppResult<O> = call(name, input, serializer<I>(), serializer<O>())

class GitLiveFunctionsClient(
    private val functions: FirebaseFunctions
) : FunctionsClient {

    constructor(region: String) : this(Firebase.functions(region = region))

    override suspend fun <I : Any, O : Any> call(
        name: String,
        input: I,
        inputSerializer: KSerializer<I>,
        outputSerializer: KSerializer<O>
    ): AppResult<O> {
        return try {
            val callable = functions.httpsCallable(name)
            val result = callable(inputSerializer, input)
            val data = result.data(outputSerializer)
            AppResult.Ok(data)
        } catch (t: Throwable) {
            AppResult.Err(mapFunctionsError(t))
        }
    }
}

/**
 * Pure function mapping Firebase / platform function errors to domain [AppError].
 */
fun mapFunctionsError(
    message: String? = null,
    code: String? = null,
    details: Any? = null,
    cause: Throwable? = null
): AppError {
    // 1. Business error: details has reason (e.g. { reason: "RIDE_FULL", extra: "..." })
    val reason = extractReason(details)
    if (reason != null) return reason

    // 2. Check error codes
    val upperCode = code?.uppercase()
    if (upperCode in setOf("UNAVAILABLE", "DEADLINE_EXCEEDED", "NETWORK_ERROR")) {
        return AppError.Network
    }
    if (upperCode in setOf("UNAUTHENTICATED", "NOT_SIGNED_IN")) {
        return AppError.NotSignedIn
    }

    // 3. Check for network root cause or message
    if (isNetworkCause(cause) || isNetworkMessage(message)) {
        return AppError.Network
    }

    // 4. Fallback to Unknown
    return AppError.Unknown(message)
}

/**
 * Extracts error details and code from throwable and maps to [AppError].
 */
fun mapFunctionsError(throwable: Throwable): AppError {
    val message = throwable.message
    val info = extractFunctionsExceptionInfo(throwable)

    return mapFunctionsError(
        message = message,
        code = info.code,
        details = info.details,
        cause = throwable.cause ?: throwable
    )
}

private fun extractReason(details: Any?): AppError.Reason? {
    if (details == null) return null
    when (details) {
        is Map<*, *> -> {
            val reason = details["reason"]?.toString()
            if (!reason.isNullOrBlank()) {
                val extra = details["extra"]?.toString()
                return AppError.Reason(code = reason, extra = extra)
            }
        }
        is String -> {
            val reasonMatch = Regex(""""reason"\s*:\s*"([^"]+)"""").find(details)
            if (reasonMatch != null) {
                val reason = reasonMatch.groupValues[1]
                val extraMatch = Regex(""""extra"\s*:\s*"([^"]+)"""").find(details)
                return AppError.Reason(code = reason, extra = extraMatch?.groupValues?.get(1))
            }
        }
    }
    return null
}

private fun isNetworkCause(throwable: Throwable?): Boolean {
    if (throwable == null) return false
    val name = throwable::class.simpleName ?: ""
    if (name.contains("ConnectException") ||
        name.contains("UnknownHostException") ||
        name.contains("SocketTimeoutException") ||
        name.contains("SocketException")
    ) return true
    return isNetworkCause(throwable.cause)
}

private fun isNetworkMessage(message: String?): Boolean {
    if (message.isNullOrBlank()) return false
    val lower = message.lowercase()
    return lower.contains("connectexception") ||
        lower.contains("failed to connect") ||
        lower.contains("unable to resolve host") ||
        lower.contains("network error") ||
        lower.contains("connection refused") ||
        lower.contains("timed out")
}
