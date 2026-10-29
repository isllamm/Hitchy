package com.hitchy.core.data

import kotlinx.coroutines.test.runTest
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

@Serializable
private data class TestRequest(val query: String)

@Serializable
private data class TestResponse(val result: String)

private class FakeFunctionsClient(
    var resultProvider: (String, Any) -> AppResult<Any>,
) : FunctionsClient {
    @Suppress("UNCHECKED_CAST")
    override suspend fun <I : Any, O : Any> call(
        name: String,
        input: I,
        inputSerializer: KSerializer<I>,
        outputSerializer: KSerializer<O>,
    ): AppResult<O> = resultProvider(name, input) as AppResult<O>
}

class FunctionsClientTest {

    @Test
    fun typedCallSerializesAndReturnsOk() = runTest {
        val client = FakeFunctionsClient { name, input ->
            val req = input as TestRequest
            assertEquals("testMethod", name)
            AppResult.Ok(TestResponse("echo: ${req.query}"))
        }

        val response = client.call<TestRequest, TestResponse>("testMethod", TestRequest("hello"))
        assertIs<AppResult.Ok<TestResponse>>(response)
        assertEquals("echo: hello", response.value.result)
    }

    @Test
    fun typedCallPropagatesError() = runTest {
        val client = FakeFunctionsClient { _, _ ->
            AppResult.Err(AppError.Reason("RIDE_FULL"))
        }

        val response = client.call<TestRequest, TestResponse>("book", TestRequest("123"))
        assertIs<AppResult.Err>(response)
        assertEquals(AppError.Reason("RIDE_FULL"), response.error)
    }

    @Test
    fun mapFunctionsErrorMapsNetworkErrors() {
        // By code
        assertEquals(AppError.Network, mapFunctionsError(code = "UNAVAILABLE"))
        assertEquals(AppError.Network, mapFunctionsError(code = "DEADLINE_EXCEEDED"))
        assertEquals(AppError.Network, mapFunctionsError(code = "network_error"))

        // By message
        assertEquals(
            AppError.Network,
            mapFunctionsError(message = "Failed to connect to 10.0.2.2:5001"),
        )
        assertEquals(
            AppError.Network,
            mapFunctionsError(message = "java.net.ConnectException: Connection refused"),
        )
        assertEquals(
            AppError.Network,
            mapFunctionsError(message = "Unable to resolve host \"example.com\""),
        )
    }

    @Test
    fun mapFunctionsErrorMapsUnauthenticatedErrors() {
        assertEquals(AppError.NotSignedIn, mapFunctionsError(code = "UNAUTHENTICATED"))
        assertEquals(AppError.NotSignedIn, mapFunctionsError(code = "unauthenticated"))
        assertEquals(AppError.NotSignedIn, mapFunctionsError(code = "NOT_SIGNED_IN"))
    }

    @Test
    fun mapFunctionsErrorMapsReasonWithoutExtra() {
        val fromMap = mapFunctionsError(details = mapOf("reason" to "RIDE_FULL"))
        assertIs<AppError.Reason>(fromMap)
        assertEquals("RIDE_FULL", fromMap.code)
        assertNull(fromMap.extra)

        val fromJsonString = mapFunctionsError(details = """{"reason":"RIDE_FULL"}""")
        assertIs<AppError.Reason>(fromJsonString)
        assertEquals("RIDE_FULL", fromJsonString.code)
        assertNull(fromJsonString.extra)
    }

    @Test
    fun mapFunctionsErrorMapsReasonWithExtra() {
        val fromMap = mapFunctionsError(
            details = mapOf("reason" to "PRICE_OUT_OF_RANGE", "extra" to "too high"),
        )
        assertIs<AppError.Reason>(fromMap)
        assertEquals("PRICE_OUT_OF_RANGE", fromMap.code)
        assertEquals("too high", fromMap.extra)

        val fromJsonString = mapFunctionsError(
            details = """{"reason":"PRICE_OUT_OF_RANGE","extra":"too high"}""",
        )
        assertIs<AppError.Reason>(fromJsonString)
        assertEquals("PRICE_OUT_OF_RANGE", fromJsonString.code)
        assertEquals("too high", fromJsonString.extra)
    }

    @Test
    fun mapFunctionsErrorMapsUnknownErrors() {
        val result = mapFunctionsError(code = "INTERNAL", message = "Internal database failure")
        assertIs<AppError.Unknown>(result)
        assertEquals("Internal database failure", result.message)

        val generic = mapFunctionsError(message = "Unexpected runtime crash")
        assertIs<AppError.Unknown>(generic)
        assertEquals("Unexpected runtime crash", generic.message)

        val nullMsg = mapFunctionsError(message = null)
        assertIs<AppError.Unknown>(nullMsg)
        assertNull(nullMsg.message)
    }

    @Test
    fun mapFunctionsErrorFromThrowable() {
        val error = RuntimeException("Failed to connect to 10.0.2.2:5001")
        assertEquals(AppError.Network, mapFunctionsError(error))

        val genericError = IllegalStateException("Something failed")
        val mapped = mapFunctionsError(genericError)
        assertIs<AppError.Unknown>(mapped)
        assertEquals("Something failed", mapped.message)
    }
}
