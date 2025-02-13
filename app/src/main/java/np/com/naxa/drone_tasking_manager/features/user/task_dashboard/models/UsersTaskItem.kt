package np.com.naxa.drone_tasking_manager.features.user.task_dashboard.models

data class UsersTaskItem(
    val certificateUrl: String?,
    val createdAt: String?,
    val flightDistanceKm: Double?,
    val flightTimeMinutes: Double?,
    val projectId: String?,
    val projectName: String?,
    val projectTaskIndex: Int?,
    val registrationCertificateUrl: String?,
    val state: String?,
    val taskId: String?,
    val totalAreaSqkm: Double?,
    val updatedAt: String?
)