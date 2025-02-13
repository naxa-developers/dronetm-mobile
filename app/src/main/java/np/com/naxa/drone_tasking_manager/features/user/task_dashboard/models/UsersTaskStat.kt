package np.com.naxa.drone_tasking_manager.features.user.task_dashboard.models

data class UsersTaskStat(
    val completedTasks: Int?,
    val ongoingTasks: Int?,
    val requestLogs: Int?,
    val unflyableTasks: Int?
)