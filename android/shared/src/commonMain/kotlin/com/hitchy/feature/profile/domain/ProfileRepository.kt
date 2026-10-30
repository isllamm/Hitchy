package com.hitchy.feature.profile.domain

import com.hitchy.core.data.AppResult
import com.hitchy.core.model.AppLanguage
import com.hitchy.core.model.Gender
import com.hitchy.core.model.HostStatus
import com.hitchy.core.model.LocalizedName
import com.hitchy.core.model.Role
import com.hitchy.core.model.SavedPlace
import com.hitchy.core.model.Tier
import com.hitchy.core.model.WorkHours
import kotlinx.coroutines.flow.Flow
import kotlin.math.round

data class RiderStats(
    val tier: Tier,
    val ratingSum: Int,
    val ratingCount: Int,
    val tripsCompleted: Int,
    val unpaidFees: Int,
    val lateCancels: Int,
) {
    val ratingAvg: Double?
        get() = if (ratingCount > 0) round((ratingSum.toDouble() / ratingCount) * 10.0) / 10.0 else null
}

data class HostStats(
    val status: HostStatus,
    val verified: Boolean,
    val approvedAt: String?,
    val freeUntil: String?,
    val creditsRegular: Int,
    val creditsFixed: Int,
    val tier: Tier,
    val ratingSum: Int,
    val ratingCount: Int,
    val tripsCompleted: Int,
    val lateCancels: Int,
    val hostLateCount: Int,
) {
    val ratingAvg: Double?
        get() = if (ratingCount > 0) round((ratingSum.toDouble() / ratingCount) * 10.0) / 10.0 else null
}

data class User(
    val uid: String,
    val phone: String,
    val firstName: String,
    val lastName: String,
    val gender: Gender,
    val companyId: String?,
    val companyOther: String?,
    val workHours: WorkHours?,
    val language: AppLanguage,
    val photoPath: String?,
    val savedPlaces: List<SavedPlace>,
    val activeRole: Role,
    val referralCode: String,
    val suspended: Boolean,
    val rider: RiderStats,
    val host: HostStats,
    val pointsBalance: Int,
)

data class Company(
    val id: String,
    val name: LocalizedName,
    val zoneId: String,
    val active: Boolean,
)

data class CompleteProfileInput(
    val firstName: String,
    val lastName: String,
    val gender: Gender,
    val companyId: String?,
    val companyOther: String?,
    val workHours: WorkHours?,
    val language: AppLanguage,
    val referralCode: String?,
)

data class CompleteProfileResult(
    val activeRole: Role,
    val hostStatus: HostStatus,
    val referralApplied: Boolean,
)

data class ProfileFields(
    val firstName: String,
    val lastName: String,
    val companyId: String?,
    val companyOther: String?,
    val workHours: WorkHours?,
    val language: AppLanguage,
    val photoPath: String?,
)

data class PublicProfile(
    val uid: String,
    val firstName: String,
    val photoPath: String?,
    val hostRatingAvg: Double?,
    val hostRatingCount: Int,
    val hostTrips: Int,
    val riderRatingAvg: Double?,
    val riderRatingCount: Int,
    val verified: Boolean,
    val hostTier: Tier,
    val riderTier: Tier,
)

interface ProfileRepository {
    fun observeMe(): Flow<User?>
    fun observePublicProfile(uid: String): Flow<PublicProfile?>
    fun observeCompanies(): Flow<List<Company>>
    suspend fun completeProfile(input: CompleteProfileInput): AppResult<CompleteProfileResult>
    suspend fun updateProfile(fields: ProfileFields): AppResult<Unit>
    suspend fun switchRole(role: Role): AppResult<Role>
}
