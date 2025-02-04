package np.com.naxa.drone_tasking_manager.features.user.profile.mapper

import np.com.naxa.drone_tasking_manager.features.user.profile.dto.UserProfileDto
import np.com.naxa.drone_tasking_manager.features.user.profile.dto.UserProfileUpdateDto
import np.com.naxa.drone_tasking_manager.features.user.profile.models.BasicUserDetails
import np.com.naxa.drone_tasking_manager.features.user.profile.models.OtherUserDetails
import np.com.naxa.drone_tasking_manager.features.user.profile.models.UserProfile
import np.com.naxa.drone_tasking_manager.features.user.profile.models.UserProfileUpdateDetails

fun UserProfileDto.toUserProfile(): UserProfile {
    return UserProfile(
        id = this.id,
        email_address = this.emailAddress,
        is_active = this.isActive,
        is_superuser = this.isSuperuser,
        name = this.name,
        phone_number = this.phoneNumber,
        profile_img = this.profileImg,
        certificate_file = this.certificateFile,
        certificate_url = this.certificateUrl,
        certified_drone_operator = this.certifiedDroneOperator,
        city = this.city,
        country = this.country,
        drone_you_own = this.droneYouOwn,
        experience_years = this.experienceYears,
        has_user_profile = this.hasUserProfile,
        job_title = this.jobTitle,
        notify_for_projects_within_km = this.notifyForProjectsWithinKm,
        organization_address = this.organizationAddress,
        organization_name = this.organizationName,
        registration_certificate_url = this.registrationCertificateUrl,
        registration_file = this.registrationFile,
        role = this.role,
        user_id = this.userId
    )
}

fun UserProfileDto.toBasicUserDetails(): BasicUserDetails {
    return BasicUserDetails(
        name = this.name,
        phone_number = this.phoneNumber,
        profile_img = this.profileImg,
        city = this.city,
        country = this.country
    )
}

fun UserProfileDto.toOtherUserDetails(): OtherUserDetails {
    return OtherUserDetails(
        certified_drone_operator = this.certifiedDroneOperator,
        drone_you_own = this.droneYouOwn,
        experience_years = this.experienceYears,
        notify_for_projects_within_km = this.notifyForProjectsWithinKm,
        registration_certificate_url = this.registrationCertificateUrl,
        registration_file = this.registrationFile,
    )
}

fun UserProfile.toBasicUserDetails(): BasicUserDetails {
    return BasicUserDetails(
        name = this.name,
        phone_number = this.phone_number,
        profile_img = this.phone_number,
        city = this.city,
        country = this.country
    )
}

fun UserProfile.toOtherUserDetails(): OtherUserDetails {
    return OtherUserDetails(
        certified_drone_operator = this.certified_drone_operator,
        drone_you_own = this.drone_you_own,
        experience_years = this.experience_years,
        notify_for_projects_within_km = this.notify_for_projects_within_km,
        registration_certificate_url = this.registration_certificate_url,
        registration_file = this.registration_file,
    )
}

fun UserProfileUpdateDto.toBasicUserProfileUpdateDetails(): UserProfileUpdateDetails {
    return UserProfileUpdateDetails(
        details = this.message,
        result = this.results?.toUserProfile()
    )
}

fun UserProfileUpdateDto.toOtherUserProfileUpdateDetails(): UserProfileUpdateDetails {
    return UserProfileUpdateDetails(
        details = this.message,
        result = this.results?.toUserProfile()
    )
}
