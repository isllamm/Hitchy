@file:Suppress("MagicNumber")

package com.hitchy.core.data.dto

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ContractDtoTest {

    private val strictJson = Json {
        ignoreUnknownKeys = false
        isLenient = false
        explicitNulls = true
        encodeDefaults = false
    }

    private data class CallableContract<I : Any, O : Any>(
        val name: String,
        val inputSerializer: KSerializer<I>,
        val outputSerializer: KSerializer<O>,
    )

    private val contracts: List<CallableContract<*, *>> = listOf(
        CallableContract(
            name = "completeProfile",
            inputSerializer = CompleteProfileInputDto.serializer(),
            outputSerializer = CompleteProfileOutputDto.serializer(),
        ),
        CallableContract(
            name = "switchRole",
            inputSerializer = SwitchRoleInputDto.serializer(),
            outputSerializer = SwitchRoleOutputDto.serializer(),
        ),
        CallableContract(
            name = "addVehicle",
            inputSerializer = AddVehicleInputDto.serializer(),
            outputSerializer = AddVehicleOutputDto.serializer(),
        ),
        CallableContract(
            name = "submitHostApplication",
            inputSerializer = SubmitHostApplicationInputDto.serializer(),
            outputSerializer = SubmitHostApplicationOutputDto.serializer(),
        ),
        CallableContract(
            name = "requestVerification",
            inputSerializer = RequestVerificationInputDto.serializer(),
            outputSerializer = EmptyOutputDto.serializer(),
        ),
        CallableContract(
            name = "quotePrice",
            inputSerializer = QuotePriceInputDto.serializer(),
            outputSerializer = QuotePriceOutputDto.serializer(),
        ),
        CallableContract(
            name = "createRide",
            inputSerializer = CreateRideInputDto.serializer(),
            outputSerializer = CreateRideOutputDto.serializer(),
        ),
        CallableContract(
            name = "cancelRide",
            inputSerializer = CancelRideInputDto.serializer(),
            outputSerializer = CancelRideOutputDto.serializer(),
        ),
        CallableContract(
            name = "searchRides",
            inputSerializer = SearchRidesInputDto.serializer(),
            outputSerializer = SearchRidesOutputDto.serializer(),
        ),
        CallableContract(
            name = "requestSeats",
            inputSerializer = RequestSeatsInputDto.serializer(),
            outputSerializer = RequestSeatsOutputDto.serializer(),
        ),
        CallableContract(
            name = "respondToRequest",
            inputSerializer = RespondToRequestInputDto.serializer(),
            outputSerializer = RespondToRequestOutputDto.serializer(),
        ),
        CallableContract(
            name = "cancelBooking",
            inputSerializer = CancelBookingInputDto.serializer(),
            outputSerializer = CancelBookingOutputDto.serializer(),
        ),
        CallableContract(
            name = "confirmSeatPayment",
            inputSerializer = ConfirmSeatPaymentInputDto.serializer(),
            outputSerializer = EmptyOutputDto.serializer(),
        ),
        CallableContract(
            name = "startRide",
            inputSerializer = StartRideInputDto.serializer(),
            outputSerializer = EmptyOutputDto.serializer(),
        ),
        CallableContract(
            name = "checkIn",
            inputSerializer = CheckInInputDto.serializer(),
            outputSerializer = CheckInOutputDto.serializer(),
        ),
        CallableContract(
            name = "markBoarded",
            inputSerializer = MarkBoardedInputDto.serializer(),
            outputSerializer = EmptyOutputDto.serializer(),
        ),
        CallableContract(
            name = "reportLate",
            inputSerializer = ReportLateInputDto.serializer(),
            outputSerializer = ReportLateOutputDto.serializer(),
        ),
        CallableContract(
            name = "finishRide",
            inputSerializer = FinishRideInputDto.serializer(),
            outputSerializer = FinishRideOutputDto.serializer(),
        ),
        CallableContract(
            name = "disputeFee",
            inputSerializer = DisputeFeeInputDto.serializer(),
            outputSerializer = EmptyOutputDto.serializer(),
        ),
        CallableContract(
            name = "markFeePaid",
            inputSerializer = MarkFeePaidInputDto.serializer(),
            outputSerializer = EmptyOutputDto.serializer(),
        ),
        CallableContract(
            name = "sendChatMessage",
            inputSerializer = SendChatMessageInputDto.serializer(),
            outputSerializer = SendChatMessageOutputDto.serializer(),
        ),
        CallableContract(
            name = "getContactPhone",
            inputSerializer = GetContactPhoneInputDto.serializer(),
            outputSerializer = GetContactPhoneOutputDto.serializer(),
        ),
        CallableContract(
            name = "submitRating",
            inputSerializer = SubmitRatingInputDto.serializer(),
            outputSerializer = EmptyOutputDto.serializer(),
        ),
        CallableContract(
            name = "approveHost",
            inputSerializer = ApproveHostInputDto.serializer(),
            outputSerializer = ApproveHostOutputDto.serializer(),
        ),
        CallableContract(
            name = "rejectHost",
            inputSerializer = RejectHostInputDto.serializer(),
            outputSerializer = EmptyOutputDto.serializer(),
        ),
        CallableContract(
            name = "approveVehicle",
            inputSerializer = ApproveVehicleInputDto.serializer(),
            outputSerializer = EmptyOutputDto.serializer(),
        ),
        CallableContract(
            name = "setVerified",
            inputSerializer = SetVerifiedInputDto.serializer(),
            outputSerializer = EmptyOutputDto.serializer(),
        ),
        CallableContract(
            name = "setGender",
            inputSerializer = SetGenderInputDto.serializer(),
            outputSerializer = EmptyOutputDto.serializer(),
        ),
        CallableContract(
            name = "resolveFee",
            inputSerializer = ResolveFeeInputDto.serializer(),
            outputSerializer = EmptyOutputDto.serializer(),
        ),
        CallableContract(
            name = "updateConfig",
            inputSerializer = UpdateConfigInputDto.serializer(),
            outputSerializer = UpdateConfigOutputDto.serializer(),
        ),
        CallableContract(
            name = "arriveAtStop",
            inputSerializer = ArriveAtStopInputDto.serializer(),
            outputSerializer = ArriveAtStopOutputDto.serializer(),
        ),
        CallableContract(
            name = "confirmRidePayment",
            inputSerializer = ConfirmRidePaymentInputDto.serializer(),
            outputSerializer = ConfirmRidePaymentOutputDto.serializer(),
        ),
        CallableContract(
            name = "topUpWallet",
            inputSerializer = TopUpWalletInputDto.serializer(),
            outputSerializer = TopUpWalletOutputDto.serializer(),
        ),
    )

    private fun findFixturesDir(): File {
        val sysProp = System.getProperty("hitchy.contract.fixturesDir")
        if (!sysProp.isNullOrBlank()) {
            val dir = File(sysProp)
            if (dir.exists()) return dir
        }
        var current: File? = File(".").canonicalFile
        while (current != null) {
            val candidate = File(current, "contract/fixtures")
            if (candidate.isDirectory) return candidate
            current = current.parentFile
        }
        error("Could not find contract fixtures directory from ${File(".").canonicalPath}")
    }

    @Test
    fun testAllFixturesHaveSerializersAndRoundTripStrictly() {
        val fixturesDir = findFixturesDir()
        assertTrue(
            fixturesDir.exists() && fixturesDir.isDirectory,
            "Fixtures directory does not exist: $fixturesDir",
        )

        val files = fixturesDir.listFiles()?.filter { it.extension == "json" } ?: emptyList()
        assertTrue(files.isNotEmpty(), "Fixtures directory is empty: $fixturesDir")

        val requestFiles = files.filter { it.name.endsWith(".request.json") }
        val responseFiles = files.filter { it.name.endsWith(".response.json") }

        val contractMap = contracts.associateBy { it.name }

        assertEquals(
            expected = contracts.size,
            actual = requestFiles.size,
            message = "Expected exactly ${contracts.size} request fixtures",
        )
        assertEquals(
            expected = contracts.size,
            actual = responseFiles.size,
            message = "Expected exactly ${contracts.size} response fixtures",
        )

        // Verify every fixture has a matching contract serializer
        for (file in requestFiles) {
            val callableName = file.name.removeSuffix(".request.json")
            assertTrue(
                contractMap.containsKey(callableName),
                "No contract serializer registered for request fixture: ${file.name}",
            )
        }
        for (file in responseFiles) {
            val callableName = file.name.removeSuffix(".response.json")
            assertTrue(
                contractMap.containsKey(callableName),
                "No contract serializer registered for response fixture: ${file.name}",
            )
        }

        // Test each contract against its request and response fixture
        for (contract in contracts) {
            testContract(contract, fixturesDir)
        }
    }

    @Test
    fun testRideViewDtoDecoding() {
        // RideViewDto represents a ride detail with vehicle details, exact GPS and status
        val jsonText = """
            {
              "rideId": "ride-test-1",
              "departAt": "2026-11-02T05:30:00.000Z",
              "arriveAt": "2026-11-02T06:35:00.000Z",
              "fromZone": "fifth_settlement",
              "toZone": "smart_village",
              "fromPointId": "p1",
              "toPointId": "p2",
              "fromPointName": {
                "ar": "نقطة تجريبية 1",
                "en": "Test point 1"
              },
              "toPointName": {
                "ar": "نقطة تجريبية 2",
                "en": "Test point 2"
              },
              "pricePerSeat": 56,
              "fairPrice": true,
              "seatsLeft": 2,
              "walkM": 250,
              "bestMatch": true,
              "womenOnly": false,
              "vehicleType": "car",
              "fixed": false,
              "host": {
                "uid": "host-test-uid",
                "firstName": "Test Host",
                "photoPath": "avatars/host-test-uid.jpg",
                "rating": 4.9,
                "ratingCount": 18,
                "trips": 24,
                "verified": true,
                "tier": "silver",
                "sameCompany": true
              },
              "status": "open",
              "fromPoint": {
                "lat": 30.01234,
                "lng": 31.45678
              },
              "vehicle": {
                "make": "Toyota",
                "model": "Corolla",
                "color": "Silver",
                "plate": "أ ب ج 1 2 3 4"
              }
            }
        """.trimIndent()

        val dto = strictJson.decodeFromString(RideViewDto.serializer(), jsonText)
        assertNotNull(dto)
        assertEquals("ride-test-1", dto.rideId)
        assertEquals(RideStatusDto.OPEN, dto.status)
        assertEquals(30.01234, dto.fromPoint.lat)
        assertEquals("Corolla", dto.vehicle.model)
    }

    private fun <I : Any, O : Any> testContract(
        contract: CallableContract<I, O>,
        fixturesDir: File,
    ) {
        val reqFile = File(fixturesDir, "${contract.name}.request.json")
        assertTrue(reqFile.exists(), "Missing request fixture for ${contract.name}")
        val reqText = reqFile.readText()

        // Decode request strictly
        val inputDto = strictJson.decodeFromString(contract.inputSerializer, reqText)
        assertNotNull(inputDto, "Failed to decode request for ${contract.name}")

        // Re-encode and assert JsonElement equality
        val reEncodedElement = strictJson.encodeToJsonElement(contract.inputSerializer, inputDto)
        val expectedReqElement = strictJson.parseToJsonElement(reqText)
        assertEquals(
            expected = expectedReqElement,
            actual = reEncodedElement,
            message = "Re-encoded JSON for ${contract.name}.request.json does not match fixture",
        )

        // Decode response strictly
        val resFile = File(fixturesDir, "${contract.name}.response.json")
        assertTrue(resFile.exists(), "Missing response fixture for ${contract.name}")
        val resText = resFile.readText()

        val outputDto = strictJson.decodeFromString(contract.outputSerializer, resText)
        assertNotNull(outputDto, "Failed to decode response for ${contract.name}")
    }
}
