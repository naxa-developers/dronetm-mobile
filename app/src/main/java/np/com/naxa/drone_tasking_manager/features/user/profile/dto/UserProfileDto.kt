package np.com.naxa.drone_tasking_manager.features.user.profile.dto


import com.google.gson.annotations.SerializedName
import java.math.BigInteger

data class UserProfileDto(
    @SerializedName("certificate_file") val certificateFile: String?,
    @SerializedName("certificate_url") val certificateUrl: String?,
    @SerializedName("certified_drone_operator") val certifiedDroneOperator: Boolean?,
    @SerializedName("city") val city: String?,
    @SerializedName("country") val country: String?,
    @SerializedName("drone_you_own") val droneYouOwn: String?,
    @SerializedName("email_address") val emailAddress: String?,
    @SerializedName("experience_years") val experienceYears: Int?,
    @SerializedName("has_user_profile") val hasUserProfile: Boolean?,
    @SerializedName("id") val id: String?,
    @SerializedName("is_active") val isActive: Boolean?,
    @SerializedName("is_superuser") val isSuperuser: Boolean?,
    @SerializedName("job_title") val jobTitle: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("notify_for_projects_within_km") val notifyForProjectsWithinKm: Int?,
    @SerializedName("organization_address") val organizationAddress: String?,
    @SerializedName("organization_name") val organizationName: String?,
    @SerializedName("phone_number") val phoneNumber: String?,
    @SerializedName("profile_img") val profileImg: String?,
    @SerializedName("registration_certificate_url") val registrationCertificateUrl: String?,
    @SerializedName("registration_file") val registrationFile: String?,
    @SerializedName("role") val role: List<String?>?,
    @SerializedName("user_id") val userId: BigInteger?
)