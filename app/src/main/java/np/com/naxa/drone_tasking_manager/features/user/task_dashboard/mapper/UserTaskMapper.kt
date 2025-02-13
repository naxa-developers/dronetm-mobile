package np.com.naxa.drone_tasking_manager.features.user.task_dashboard.mapper

import android.util.Log
import np.com.naxa.drone_tasking_manager.features.user.task_dashboard.dto.UsersTaskDtoItem
import np.com.naxa.drone_tasking_manager.features.user.task_dashboard.enums.TaskStatus
import np.com.naxa.drone_tasking_manager.features.user.task_dashboard.models.UsersTaskItem

fun List<UsersTaskDtoItem>.toUsersTask() : List<UsersTaskItem>{
    return this.map { it.toUsersTaskItem() }
}

fun UsersTaskDtoItem.toUsersTaskItem() : UsersTaskItem {
    return UsersTaskItem(
        taskId = this.taskId,
        totalAreaSqkm = this.totalAreaSqkm,
        flightTimeMinutes = this.flightTimeMinutes,
        flightDistanceKm = this.flightDistanceKm,
        createdAt = this.createdAt,
        state = this.state,
        projectId = this.projectId,
        projectTaskIndex = this.projectTaskIndex,
        projectName = this.projectName,
        updatedAt = this.updatedAt,
        registrationCertificateUrl = this.registrationCertificateUrl,
        certificateUrl = this.certificateUrl
    )

}


fun List<UsersTaskItem>.toUsersTaskCompleted() : List<UsersTaskItem>{
    return this.filter { task ->
        task.state?.let { state ->
            Log.d("TAG", "UsersTaskItem toUsersTaskOnGoing: $state")

            state == TaskStatus.IMAGE_PROCESSING_FINISHED.name
        } ?: false
    }
}

fun List<UsersTaskItem>.toUsersTaskOnUnFlyable() : List<UsersTaskItem>{
    return this.filter { task ->
        task.state?.let { state ->
            Log.d("TAG", "UsersTaskItem toUsersTaskOnGoing: $state")

            state == TaskStatus.UNFLYABLE_TASK.name
        } ?: false
    }
}

fun List<UsersTaskItem>.toUsersTaskOnGoing() : List<UsersTaskItem>{
    return this.filter { task ->
        task.state?.let { state ->
            Log.d("TAG", "UsersTaskItem toUsersTaskOnGoing: $state")

            state == TaskStatus.IMAGE_PROCESSING_FAILED.name ||
                    state == TaskStatus.LOCKED_FOR_MAPPING.name
        } ?: false
    }
}

