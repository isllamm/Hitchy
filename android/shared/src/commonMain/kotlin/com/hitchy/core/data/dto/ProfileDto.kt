package com.hitchy.core.data.dto

import com.hitchy.core.model.AppLanguage
import com.hitchy.core.model.Role
import kotlinx.serialization.Serializable

@Serializable
data class CompleteProfileInputDto(
    val firstName: String,
    val lastName: String,
    val gender: GenderDto,
    val companyId: String?,
    val companyOther: String?,
    val workHours: WorkHoursDto?,
    val language: AppLanguage,
    val referralCode: String?,
)

@Serializable
data class CompleteProfileOutputDto(
    val activeRole: Role,
    val hostStatus: HostStatusDto,
    val referralApplied: Boolean,
)

@Serializable
data class SwitchRoleInputDto(
    val role: Role,
)

@Serializable
data class SwitchRoleOutputDto(
    val activeRole: Role,
)
