package com.hitchy.core.platform

import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.hitchy.core.data.AppError
import com.hitchy.core.data.AppResult
import com.hitchy.core.data.FirebaseModule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.time.Clock

private const val TIMEOUT_SECONDS = 60L
private const val RESEND_TIMEOUT_MS = 60_000L

class AndroidPhoneAuth(
    private val activityProvider: ActivityProvider,
    private val clock: Clock,
) : PhoneAuth {

    private val _events = MutableSharedFlow<PhoneAuthEvent>(extraBufferCapacity = 8)
    override val events: Flow<PhoneAuthEvent> = _events.asSharedFlow()

    @Volatile
    private var verificationId: String? = null

    @Volatile
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            FirebaseAuth.getInstance()
                .signInWithCredential(credential)
                .addOnSuccessListener { result ->
                    FirebaseModule.setCrashlyticsUserId(result.user?.uid)
                    _events.tryEmit(PhoneAuthEvent.AutoVerified)
                }
                .addOnFailureListener { err ->
                    _events.tryEmit(PhoneAuthEvent.Failed(mapPhoneAuthError(err)))
                }
        }

        override fun onVerificationFailed(exception: FirebaseException) {
            _events.tryEmit(PhoneAuthEvent.Failed(mapPhoneAuthError(exception)))
        }

        override fun onCodeSent(
            verId: String,
            token: PhoneAuthProvider.ForceResendingToken,
        ) {
            verificationId = verId
            resendToken = token
            val canResendAtMs = clock.now().toEpochMilliseconds() + RESEND_TIMEOUT_MS
            _events.tryEmit(PhoneAuthEvent.CodeSent(canResendAtMs = canResendAtMs))
        }
    }

    override fun start(phoneE164: String, forceResend: Boolean) {
        val activity = activityProvider.currentActivity
        if (activity == null) {
            _events.tryEmit(PhoneAuthEvent.Failed(AppError.Unknown("No active Activity")))
            return
        }

        val builder = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
            .setPhoneNumber(phoneE164)
            .setTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)

        val token = resendToken
        if (forceResend && token != null) {
            builder.setForceResendingToken(token)
        }

        PhoneAuthProvider.verifyPhoneNumber(builder.build())
    }

    @Suppress("TooGenericExceptionCaught")
    override suspend fun confirm(code: String): AppResult<Unit> {
        val verId = verificationId ?: return AppResult.Err(AppError.Reason("OTP_EXPIRED"))
        val credential = PhoneAuthProvider.getCredential(verId, code)
        return try {
            val result = FirebaseAuth.getInstance().signInWithCredential(credential).awaitTask()
            FirebaseModule.setCrashlyticsUserId(result.user?.uid)
            AppResult.Ok(Unit)
        } catch (e: Exception) {
            AppResult.Err(mapPhoneAuthError(e))
        }
    }

    private suspend fun <T> Task<T>.awaitTask(): T = suspendCancellableCoroutine { cont ->
        addOnSuccessListener { res -> cont.resume(res) }
        addOnFailureListener { err -> cont.resumeWithException(err) }
        addOnCanceledListener { cont.cancel() }
    }
}

@Suppress("CyclomaticComplexMethod")
internal fun mapPhoneAuthError(throwable: Throwable): AppError = when (throwable) {
    is FirebaseTooManyRequestsException -> AppError.Reason("TOO_MANY_ATTEMPTS")

    is FirebaseNetworkException -> AppError.Network

    is FirebaseAuthInvalidCredentialsException -> when (throwable.errorCode) {
        "ERROR_SESSION_EXPIRED" -> AppError.Reason("OTP_EXPIRED")
        "ERROR_INVALID_PHONE_NUMBER" -> AppError.Reason("INVALID_INPUT")
        else -> AppError.Reason("OTP_INVALID")
    }

    is FirebaseAuthException -> when (throwable.errorCode) {
        "ERROR_QUOTA_EXCEEDED" -> AppError.Reason("SMS_QUOTA")
        "ERROR_SESSION_EXPIRED" -> AppError.Reason("OTP_EXPIRED")
        "ERROR_INVALID_VERIFICATION_CODE" -> AppError.Reason("OTP_INVALID")
        "ERROR_TOO_MANY_REQUESTS" -> AppError.Reason("TOO_MANY_ATTEMPTS")
        else -> AppError.Unknown(throwable.message)
    }

    else -> {
        val msg = throwable.message.orEmpty()
        when {
            msg.contains("SMS quota", ignoreCase = true) ||
                msg.contains("QUOTA_EXCEEDED", ignoreCase = true) -> AppError.Reason("SMS_QUOTA")

            msg.contains("SESSION_EXPIRED", ignoreCase = true) -> AppError.Reason("OTP_EXPIRED")

            msg.contains("INVALID_VERIFICATION_CODE", ignoreCase = true) -> AppError.Reason("OTP_INVALID")

            msg.contains("TOO_MANY_ATTEMPTS", ignoreCase = true) ||
                msg.contains("TOO_MANY_REQUESTS", ignoreCase = true) -> AppError.Reason("TOO_MANY_ATTEMPTS")

            msg.contains("NETWORK", ignoreCase = true) -> AppError.Network

            else -> AppError.Unknown(throwable.message)
        }
    }
}
