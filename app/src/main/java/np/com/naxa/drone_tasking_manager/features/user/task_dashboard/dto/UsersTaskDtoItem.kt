package np.com.naxa.drone_tasking_manager.features.user.task_dashboard.dto

import com.google.gson.annotations.SerializedName

data class UsersTaskDtoItem(
    @SerializedName("certificate_url") val certificateUrl: String?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("flight_distance_km") val flightDistanceKm: Double?,
    @SerializedName("flight_time_minutes") val flightTimeMinutes: Int?,
    @SerializedName("project_id") val projectId: String?,
    @SerializedName("project_name") val projectName: String?,
    @SerializedName("project_task_index") val projectTaskIndex: Int?,
    @SerializedName("registration_certificate_url") val registrationCertificateUrl: String?,
    @SerializedName("state") val state: String?,
    @SerializedName("task_id") val taskId: String?,
    @SerializedName("total_area_sqkm") val totalAreaSqkm: Double?,
    @SerializedName("updated_at") val updatedAt: String?
)