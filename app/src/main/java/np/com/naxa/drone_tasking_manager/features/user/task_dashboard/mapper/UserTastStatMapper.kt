package np.com.naxa.drone_tasking_manager.features.user.task_dashboard.mapper

import np.com.naxa.drone_tasking_manager.features.user.task_dashboard.dto.UsersTaskStatDto
import np.com.naxa.drone_tasking_manager.features.user.task_dashboard.models.UsersTaskStat

fun UsersTaskStatDto.toUserTaskStat() : UsersTaskStat {
    return UsersTaskStat(
        requestLogs = this.requestLogs,
        completedTasks = this.completedTasks,
        ongoingTasks = this.ongoingTasks,
        unflyableTasks = this.unflyableTasks
    )
}