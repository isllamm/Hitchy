package com.hitchy.feature.host.domain

import com.hitchy.core.model.VehicleType

/** The four documents the application needs (plan 8.4, `KycDocKey`), in the order the app asks for them. */
enum class KycDocKey { ID_FRONT, ID_BACK, DRIVING_LICENSE, VEHICLE_LICENSE }

/**
 * What the owner entered so far in the application (Figma DEMO "car owner" flow), kept in memory so "Leave for
 * now?" can promise to keep it and the Welcome choice can reach the wizard after sign-in.
 */
class HostDraft {
    /** The Welcome screen's "I have a vehicle" choice: open the application after Complete profile. */
    var wantsToHost: Boolean = false
    var vehicleType: VehicleType? = null
    var fullName: String = ""
    var selfie: ByteArray? = null
    val docs: MutableMap<KycDocKey, ByteArray> = mutableMapOf()
    var makeModel: String = ""
    var year: Int? = null
    var plate: String = ""
    var color: String = ""

    fun clear() {
        wantsToHost = false
        vehicleType = null
        fullName = ""
        selfie = null
        docs.clear()
        makeModel = ""
        year = null
        plate = ""
        color = ""
    }
}
