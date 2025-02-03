package np.com.naxa.drone_tasking_manager.features.user.profile.models

import java.math.BigInteger

data class UserProfile(
    val certificate_file: String?,
    val certificate_url: String?,
    val certified_drone_operator: Boolean?,
    val city: String?,
    val country: String?,
    val drone_you_own: String?,
    val email_address: String?,
    val experience_years: Int?,
    val has_user_profile: Boolean?,
    val id: String?,
    val is_active: Boolean?,
    val is_superuser: Boolean?,
    val job_title: String?,
    val name: String?,
    val notify_for_projects_within_km: Int?,
    val organization_address: String?,
    val organization_name: String?,
    val phone_number: String?,
    val profile_img: String?,
    val registration_certificate_url: String?,
    val registration_file: String?,
    val role: List<String?>?,
    val user_id: BigInteger?
)