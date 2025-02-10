package np.com.naxa.drone_tasking_manager.features.tasks.mapper

import np.com.naxa.drone_tasking_manager.features.projects.mapper.toProjectGeometry
import np.com.naxa.drone_tasking_manager.features.tasks.dto.TaskDto
import np.com.naxa.drone_tasking_manager.features.tasks.dto.TaskEventResponseDto
import np.com.naxa.drone_tasking_manager.features.tasks.models.ProjectTask
import np.com.naxa.drone_tasking_manager.features.tasks.models.ProjectTaskState
import np.com.naxa.drone_tasking_manager.features.tasks.models.TaskLockUnlockResponse
import np.com.naxa.drone_tasking_manager.features.tasks.models.TaskUnFlyableResponse

fun TaskDto.toProjectTask() = ProjectTask(
    id = id,
    projectId = projectId,
    projectName = projectName,
    projectTaskIndex = projectTaskIndex,
    userId = userId,
    userName = userName,
    state = ProjectTaskState.fromString(state),
    frontOverlap = frontOverlap,
    totalAreaSqkm = totalAreaSqkm,
    flightTimeMinutes = flightTimeMinutes,
    flightDistanceKm = flightDistanceKm,
    totalImageUploaded = totalImageUploaded,
    imageCount = imageCount,
    assetsUrl = assetsUrl,
    geometry = outline?.toProjectGeometry(),
    centroid = centroid,
    sideOverlap = sideOverlap,
    gsdCmPx = gsdCmPx,
    gimbleAnglesDegrees = gimbleAnglesDegrees,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun TaskEventResponseDto.toTaskLockUnlockResponse() = TaskLockUnlockResponse(
    projectId = projectId,
    taskId = taskId,
    comment = comment
)

fun TaskEventResponseDto.toTaskUnFlyableResponse() = TaskUnFlyableResponse(
    projectId = projectId,
    taskId = taskId,
    comment = comment
)