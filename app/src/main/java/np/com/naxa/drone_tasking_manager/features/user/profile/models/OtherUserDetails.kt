package np.com.naxa.drone_tasking_manager.features.user.profile.models

data class OtherUserDetails(
    val certified_drone_operator: Boolean?,
    val drone_you_own: String?,
    val experience_years: Int?,
    val notify_for_projects_within_km: Int?,
    val registration_certificate_url: String?,
    val registration_file: String?
)