package com.hitchy.core.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AppResultTest {

    @Test
    fun okResultCarriesValueAndReportsState() {
        val result: AppResult<String> = AppResult.Ok("success")
        assertTrue(result.isOk)
        assertFalse(result.isErr)
        assertEquals("success", result.getOrNull())
    }

    @Test
    fun errResultCarriesErrorAndReportsState() {
        val result: AppResult<String> = AppResult.Err(AppError.Network)
        assertFalse(result.isOk)
        assertTrue(result.isErr)
        assertNull(result.getOrNull())
    }

    @Test
    fun mapTransformsOkValue() {
        val ok: AppResult<Int> = AppResult.Ok(42)
        val mapped = ok.map { it * 2 }
        assertIs<AppResult.Ok<Int>>(mapped)
        assertEquals(84, mapped.value)
    }

    @Test
    fun mapPreservesError() {
        val err: AppResult<Int> = AppResult.Err(AppError.NotSignedIn)
        val mapped = err.map { it * 2 }
        assertIs<AppResult.Err>(mapped)
        assertEquals(AppError.NotSignedIn, mapped.error)
    }

    @Test
    fun flatMapChainsSuccessfulOperations() {
        val ok: AppResult<Int> = AppResult.Ok(10)
        val chained = ok.flatMap { AppResult.Ok("$it items") }
        assertIs<AppResult.Ok<String>>(chained)
        assertEquals("10 items", chained.value)
    }

    @Test
    fun flatMapPropagatesInitialError() {
        val err: AppResult<Int> = AppResult.Err(AppError.Reason("RIDE_FULL"))
        val chained = err.flatMap { AppResult.Ok("$it items") }
        assertIs<AppResult.Err>(chained)
        val error = assertIs<AppError.Reason>(chained.error)
        assertEquals("RIDE_FULL", error.code)
    }

    @Test
    fun flatMapPropagatesDownstreamError() {
        val ok: AppResult<Int> = AppResult.Ok(10)
        val chained = ok.flatMap { AppResult.Err(AppError.Unknown("failed")) }
        assertIs<AppResult.Err>(chained)
        val error = assertIs<AppError.Unknown>(chained.error)
        assertEquals("failed", error.message)
    }

    @Test
    fun onSuccessExecutesOnlyOnOk() {
        var sideEffect = 0
        val ok: AppResult<Int> = AppResult.Ok(5)
        ok.onSuccess { sideEffect = it }
        assertEquals(5, sideEffect)

        val err: AppResult<Int> = AppResult.Err(AppError.Network)
        err.onSuccess { sideEffect = 999 }
        assertEquals(5, sideEffect)
    }

    @Test
    fun onFailureExecutesOnlyOnErr() {
        var handledError: AppError? = null
        val err: AppResult<Int> = AppResult.Err(AppError.Reason("PRICE_OUT_OF_RANGE", "too high"))
        err.onFailure { handledError = it }
        assertEquals(AppError.Reason("PRICE_OUT_OF_RANGE", "too high"), handledError)

        var okExecuted = false
        val ok: AppResult<Int> = AppResult.Ok(100)
        ok.onFailure { okExecuted = true }
        assertFalse(okExecuted)
    }
}
