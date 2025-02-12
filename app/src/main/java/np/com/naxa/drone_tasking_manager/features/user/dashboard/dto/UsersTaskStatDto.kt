package np.com.naxa.drone_tasking_manager.features.user.dashboard.dto

import com.google.gson.annotations.SerializedName

data class UsersTaskStatDto(
    @SerializedName("completed_tasks") val completedTasks: Int?,
    @SerializedName("ongoing_tasks") val ongoingTasks: Int?,
    @SerializedName("request_logs") val requestLogs: Int?,
    @SerializedName("unflyable_tasks") val unflyableTasks: Int?
)