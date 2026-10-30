package com.hitchy.feature.profile

import com.hitchy.core.data.AppResult
import com.hitchy.core.model.AppLanguage
import com.hitchy.core.model.Gender
import com.hitchy.core.model.HostStatus
import com.hitchy.core.model.Role
import com.hitchy.core.model.Tier
import com.hitchy.feature.profile.domain.Company
import com.hitchy.feature.profile.domain.CompleteProfileInput
import com.hitchy.feature.profile.domain.CompleteProfileResult
import com.hitchy.feature.profile.domain.HostStats
import com.hitchy.feature.profile.domain.ProfileFields
import com.hitchy.feature.profile.domain.ProfileRepository
import com.hitchy.feature.profile.domain.PublicProfile
import com.hitchy.feature.profile.domain.RiderStats
import com.hitchy.feature.profile.domain.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeProfileRepository(
    initialUser: User? = null,
    initialCompanies: List<Company> = emptyList(),
) : ProfileRepository {
    val meFlow = MutableStateFlow(initialUser)
    val companiesFlow = MutableStateFlow(initialCompanies)
    val publicProfilesFlow = MutableStateFlow<Map<String, PublicProfile>>(emptyMap())

    var completeProfileResult: AppResult<CompleteProfileResult> = AppResult.Ok(
        CompleteProfileResult(
            activeRole = Role.RIDER,
            hostStatus = HostStatus.NONE,
            referralApplied = false,
        ),
    )
    var switchRoleResult: AppResult<Role>? = null
    var lastCompleteProfileInput: CompleteProfileInput? = null
    var lastUpdateFields: ProfileFields? = null

    override fun observeMe(): Flow<User?> = meFlow

    override fun observePublicProfile(uid: String): Flow<PublicProfile?> = publicProfilesFlow.map { it[uid] }

    override fun observeCompanies(): Flow<List<Company>> = companiesFlow

    override suspend fun completeProfile(input: CompleteProfileInput): AppResult<CompleteProfileResult> {
        lastCompleteProfileInput = input
        if (completeProfileResult is AppResult.Ok) {
            val ok = (completeProfileResult as AppResult.Ok).value
            meFlow.value = sampleUser(
                firstName = input.firstName,
                lastName = input.lastName,
                gender = input.gender,
                activeRole = ok.activeRole,
                hostStatus = ok.hostStatus,
            )
        }
        return completeProfileResult
    }

    override suspend fun updateProfile(fields: ProfileFields): AppResult<Unit> {
        lastUpdateFields = fields
        return AppResult.Ok(Unit)
    }

    override suspend fun switchRole(role: Role): AppResult<Role> {
        val custom = switchRoleResult
        if (custom != null) return custom
        meFlow.value = meFlow.value?.copy(activeRole = role)
        return AppResult.Ok(role)
    }

    companion object {
        @Suppress("LongParameterList")
        fun sampleUser(
            uid: String = "rider-test-1",
            phone: String = "+201000000001",
            firstName: String = "Omar",
            lastName: String = "Test",
            gender: Gender = Gender.MALE,
            activeRole: Role = Role.RIDER,
            hostStatus: HostStatus = HostStatus.NONE,
        ): User = User(
            uid = uid,
            phone = phone,
            firstName = firstName,
            lastName = lastName,
            gender = gender,
            companyId = null,
            companyOther = null,
            workHours = null,
            language = AppLanguage.ARABIC,
            photoPath = null,
            savedPlaces = emptyList(),
            activeRole = activeRole,
            referralCode = "HTTEST01",
            suspended = false,
            rider = RiderStats(
                tier = Tier.BRONZE,
                ratingSum = 0,
                ratingCount = 0,
                tripsCompleted = 0,
                unpaidFees = 0,
                lateCancels = 0,
            ),
            host = HostStats(
                status = hostStatus,
                verified = false,
                approvedAt = null,
                freeUntil = null,
                creditsRegular = 0,
                creditsFixed = 0,
                tier = Tier.BRONZE,
                ratingSum = 0,
                ratingCount = 0,
                tripsCompleted = 0,
                lateCancels = 0,
                hostLateCount = 0,
            ),
            pointsBalance = 0,
        )
    }
}
