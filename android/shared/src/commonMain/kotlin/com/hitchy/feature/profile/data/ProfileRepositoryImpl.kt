package com.hitchy.feature.profile.data

import com.hitchy.core.data.AppError
import com.hitchy.core.data.AppResult
import com.hitchy.core.data.FunctionsClient
import com.hitchy.core.data.call
import com.hitchy.core.data.dto.CompleteProfileInputDto
import com.hitchy.core.data.dto.CompleteProfileOutputDto
import com.hitchy.core.data.dto.GenderDto
import com.hitchy.core.data.dto.HostStatusDto
import com.hitchy.core.data.dto.SwitchRoleInputDto
import com.hitchy.core.data.dto.SwitchRoleOutputDto
import com.hitchy.core.data.dto.WorkHoursDto
import com.hitchy.core.data.map
import com.hitchy.core.data.mapFunctionsError
import com.hitchy.core.data.toIsoString
import com.hitchy.core.model.AppLanguage
import com.hitchy.core.model.Gender
import com.hitchy.core.model.HostStatus
import com.hitchy.core.model.LocalizedName
import com.hitchy.core.model.Role
import com.hitchy.core.model.SavedPlace
import com.hitchy.core.model.Tier
import com.hitchy.core.model.WorkHours
import com.hitchy.feature.profile.domain.Company
import com.hitchy.feature.profile.domain.CompleteProfileInput
import com.hitchy.feature.profile.domain.CompleteProfileResult
import com.hitchy.feature.profile.domain.HostStats
import com.hitchy.feature.profile.domain.ProfileFields
import com.hitchy.feature.profile.domain.ProfileRepository
import com.hitchy.feature.profile.domain.PublicProfile
import com.hitchy.feature.profile.domain.RiderStats
import com.hitchy.feature.profile.domain.User
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.FieldValue
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.Timestamp
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable

@Serializable
private data class EmergencyContactFirestore(
    val name: String = "",
    val phone: String = "",
)

@Serializable
private data class RiderStatsFirestore(
    val tier: Tier = Tier.BRONZE,
    val ratingSum: Int = 0,
    val ratingCount: Int = 0,
    val tripsCompleted: Int = 0,
    val unpaidFees: Int = 0,
    val lateCancels: Int = 0,
)

@Serializable
private data class CreditsFirestore(
    val regular: Int = 0,
    val fixed: Int = 0,
)

@Serializable
private data class HostStatsFirestore(
    val status: HostStatus = HostStatus.NONE,
    val verified: Boolean = false,
    val approvedAt: Timestamp? = null,
    val freeUntil: Timestamp? = null,
    val freeEndingNotified: Boolean = false,
    val credits: CreditsFirestore = CreditsFirestore(),
    val tier: Tier = Tier.BRONZE,
    val ratingSum: Int = 0,
    val ratingCount: Int = 0,
    val tripsCompleted: Int = 0,
    val lateCancels: Int = 0,
    val hostLateCount: Int = 0,
)

@Serializable
private data class PointsBalanceFirestore(
    val balance: Int = 0,
)

@Serializable
private data class UserFirestoreDoc(
    val schemaVersion: Int = 1,
    val phone: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val gender: Gender = Gender.MALE,
    val companyId: String? = null,
    val companyOther: String? = null,
    val workHours: WorkHours? = null,
    val language: AppLanguage = AppLanguage.ARABIC,
    val photoPath: String? = null,
    val emergencyContact: EmergencyContactFirestore? = null,
    val savedPlaces: List<SavedPlace> = emptyList(),
    val activeRole: Role = Role.RIDER,
    val referralCode: String = "",
    val referredBy: String? = null,
    val referralRewarded: Boolean = false,
    val blockedUids: List<String> = emptyList(),
    val suspended: Boolean = false,
    val rider: RiderStatsFirestore = RiderStatsFirestore(),
    val host: HostStatsFirestore = HostStatsFirestore(),
    val points: PointsBalanceFirestore = PointsBalanceFirestore(),
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null,
)

@Serializable
private data class CompanyFirestoreDoc(
    val name: LocalizedName = LocalizedName(ar = "", en = ""),
    val zoneId: String = "",
    val active: Boolean = true,
)

@Serializable
private data class RatingFirestore(
    val avg: Double? = null,
    val count: Int = 0,
)

@Serializable
private data class ProfileFirestoreDoc(
    val schemaVersion: Int = 1,
    val firstName: String = "",
    val photoPath: String? = null,
    val hostRating: RatingFirestore = RatingFirestore(),
    val hostTrips: Int = 0,
    val riderRating: RatingFirestore = RatingFirestore(),
    val verified: Boolean = false,
    val hostTier: Tier = Tier.BRONZE,
    val riderTier: Tier = Tier.BRONZE,
    val updatedAt: Timestamp? = null,
)

class ProfileRepositoryImpl(
    private val functionsClient: FunctionsClient,
    private val auth: FirebaseAuth = Firebase.auth,
    private val firestore: FirebaseFirestore = Firebase.firestore,
) : ProfileRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeMe(): Flow<User?> = auth.authStateChanged.flatMapLatest { firebaseUser ->
        val uid = firebaseUser?.uid
        if (uid == null) {
            flowOf(null)
        } else {
            firestore
                .collection("users")
                .document(uid)
                .snapshots
                .map { snapshot ->
                    if (!snapshot.exists) {
                        null
                    } else {
                        runCatching {
                            snapshot.data<UserFirestoreDoc>().toDomain(uid)
                        }.getOrNull()
                    }
                }
                .catch { emit(null) }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observePublicProfile(uid: String): Flow<PublicProfile?> = auth.authStateChanged.flatMapLatest { user ->
        if (user == null || uid.isBlank()) {
            flowOf(null)
        } else {
            firestore
                .collection("profiles")
                .document(uid)
                .snapshots
                .map { snapshot ->
                    if (!snapshot.exists) {
                        null
                    } else {
                        runCatching {
                            val data = snapshot.data<ProfileFirestoreDoc>()
                            PublicProfile(
                                uid = uid,
                                firstName = data.firstName,
                                photoPath = data.photoPath,
                                hostRatingAvg = data.hostRating.avg,
                                hostRatingCount = data.hostRating.count,
                                hostTrips = data.hostTrips,
                                riderRatingAvg = data.riderRating.avg,
                                riderRatingCount = data.riderRating.count,
                                verified = data.verified,
                                hostTier = data.hostTier,
                                riderTier = data.riderTier,
                            )
                        }.getOrNull()
                    }
                }
                .catch { emit(null) }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeCompanies(): Flow<List<Company>> = auth.authStateChanged.flatMapLatest { user ->
        if (user == null) {
            flowOf(emptyList())
        } else {
            firestore
                .collection("companies")
                .snapshots
                .map { querySnapshot ->
                    querySnapshot.documents.mapNotNull { doc ->
                        runCatching {
                            val data = doc.data<CompanyFirestoreDoc>()
                            Company(
                                id = doc.id,
                                name = data.name,
                                zoneId = data.zoneId,
                                active = data.active,
                            )
                        }.getOrNull()
                    }.filter { it.active }
                }
                .catch { emit(emptyList()) }
        }
    }

    override suspend fun completeProfile(input: CompleteProfileInput): AppResult<CompleteProfileResult> {
        val dto = CompleteProfileInputDto(
            firstName = input.firstName,
            lastName = input.lastName,
            gender = when (input.gender) {
                Gender.FEMALE -> GenderDto.FEMALE
                Gender.MALE -> GenderDto.MALE
            },
            companyId = input.companyId,
            companyOther = input.companyOther,
            workHours = input.workHours?.let { WorkHoursDto(start = it.start, end = it.end) },
            language = input.language,
            referralCode = input.referralCode,
        )
        return functionsClient.call<CompleteProfileInputDto, CompleteProfileOutputDto>(
            name = "completeProfile",
            input = dto,
        ).map { out ->
            CompleteProfileResult(
                activeRole = out.activeRole,
                hostStatus = when (out.hostStatus) {
                    HostStatusDto.NONE -> HostStatus.NONE
                    HostStatusDto.SUBMITTED -> HostStatus.SUBMITTED
                    HostStatusDto.APPROVED -> HostStatus.APPROVED
                    HostStatusDto.REJECTED -> HostStatus.REJECTED
                },
                referralApplied = out.referralApplied,
            )
        }
    }

    @Suppress("TooGenericExceptionCaught")
    override suspend fun updateProfile(fields: ProfileFields): AppResult<Unit> {
        val uid = auth.currentUser?.uid ?: return AppResult.Err(AppError.NotSignedIn)
        return try {
            firestore
                .collection("users")
                .document(uid)
                .update(
                    "firstName" to fields.firstName,
                    "lastName" to fields.lastName,
                    "companyId" to fields.companyId,
                    "companyOther" to fields.companyOther,
                    "workHours" to fields.workHours,
                    "language" to fields.language.code,
                    "photoPath" to fields.photoPath,
                    "updatedAt" to FieldValue.serverTimestamp,
                )
            AppResult.Ok(Unit)
        } catch (t: Throwable) {
            AppResult.Err(mapFunctionsError(t))
        }
    }

    override suspend fun switchRole(role: Role): AppResult<Role> {
        val dto = SwitchRoleInputDto(role = role)
        return functionsClient.call<SwitchRoleInputDto, SwitchRoleOutputDto>(
            name = "switchRole",
            input = dto,
        ).map { it.activeRole }
    }
}

private fun UserFirestoreDoc.toDomain(uid: String): User = User(
    uid = uid,
    phone = phone,
    firstName = firstName,
    lastName = lastName,
    gender = gender,
    companyId = companyId,
    companyOther = companyOther,
    workHours = workHours,
    language = language,
    photoPath = photoPath,
    savedPlaces = savedPlaces,
    activeRole = activeRole,
    referralCode = referralCode,
    suspended = suspended,
    rider = RiderStats(
        tier = rider.tier,
        ratingSum = rider.ratingSum,
        ratingCount = rider.ratingCount,
        tripsCompleted = rider.tripsCompleted,
        unpaidFees = rider.unpaidFees,
        lateCancels = rider.lateCancels,
    ),
    host = HostStats(
        status = host.status,
        verified = host.verified,
        approvedAt = host.approvedAt?.toIsoString(),
        freeUntil = host.freeUntil?.toIsoString(),
        creditsRegular = host.credits.regular,
        creditsFixed = host.credits.fixed,
        tier = host.tier,
        ratingSum = host.ratingSum,
        ratingCount = host.ratingCount,
        tripsCompleted = host.tripsCompleted,
        lateCancels = host.lateCancels,
        hostLateCount = host.hostLateCount,
    ),
    pointsBalance = points.balance,
)
