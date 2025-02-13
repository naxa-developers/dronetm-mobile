package np.com.naxa.drone_tasking_manager.features.user.task_dashboard.mapper

import np.com.naxa.drone_tasking_manager.features.user.task_dashboard.dto.UsersTaskDto
import np.com.naxa.drone_tasking_manager.features.user.task_dashboard.dto.UsersTaskDtoItem
import np.com.naxa.drone_tasking_manager.features.user.task_dashboard.enums.TaskStatus
import np.com.naxa.drone_tasking_manager.features.user.task_dashboard.models.UsersTask
import np.com.naxa.drone_tasking_manager.features.user.task_dashboard.models.UsersTaskItem

fun UsersTaskDto.toUsersTask() : UsersTask{
    return this.map { it.toUsersTaskItem() }.toCollection(ArrayList()) as UsersTask
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


fun UsersTask.toUsersTaskCompleted() : UsersTask{
    return this.map(fun(it: UsersTaskItem): UsersTaskItem? {
        return  if(it.state == TaskStatus.IMAGE_PROCESSING_FINISHED.name){
         it
        }else{
            return null
        }
    }).toCollection(ArrayList()) as UsersTask
}

fun UsersTask.toUsersTaskOnUnFlyable() : UsersTask{
    return this.map(fun(it: UsersTaskItem): UsersTaskItem? {
        return  if(it.state == TaskStatus.UNFLYABLE_TASK.name){
            it
        }else{
            return null
        }
    }).toCollection(ArrayList()) as UsersTask
}

fun UsersTask.toUsersTaskOnGoing() : UsersTask{
    return this.map(fun(it: UsersTaskItem): UsersTaskItem? {
        return  if(it.state == TaskStatus.IMAGE_PROCESSING_FAILED.name
            || it.state == TaskStatus.LOCKED_FOR_MAPPING.name){
            it
        }else{
            return null
        }
    }).toCollection(ArrayList()) as UsersTask
}

