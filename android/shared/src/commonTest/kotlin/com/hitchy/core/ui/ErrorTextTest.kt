package com.hitchy.core.ui

import com.hitchy.core.data.AppError
import hitchy.shared.generated.resources.Res
import hitchy.shared.generated.resources.error_internal
import hitchy.shared.generated.resources.error_network
import hitchy.shared.generated.resources.error_unauthenticated
import kotlin.test.Test
import kotlin.test.assertEquals

class ErrorTextTest {

    @Test
    fun mapsNetworkError() {
        assertEquals(Res.string.error_network, AppError.Network.message())
    }

    @Test
    fun mapsNotSignedInError() {
        assertEquals(Res.string.error_unauthenticated, AppError.NotSignedIn.message())
    }

    @Test
    fun mapsReasonUnauthenticated() {
        assertEquals(Res.string.error_unauthenticated, AppError.Reason("UNAUTHENTICATED").message())
        assertEquals(Res.string.error_unauthenticated, AppError.Reason("unauthenticated").message())
    }

    @Test
    fun mapsReasonInternal() {
        assertEquals(Res.string.error_internal, AppError.Reason("INTERNAL").message())
        assertEquals(Res.string.error_internal, AppError.Reason("internal").message())
    }

    @Test
    fun mapsUnknownReasonToInternal() {
        assertEquals(Res.string.error_internal, AppError.Reason("SOME_FUTURE_REASON").message())
    }

    @Test
    fun mapsUnknownErrorToInternal() {
        assertEquals(Res.string.error_internal, AppError.Unknown("unexpected exception").message())
        assertEquals(Res.string.error_internal, AppError.Unknown(null).message())
    }
}
