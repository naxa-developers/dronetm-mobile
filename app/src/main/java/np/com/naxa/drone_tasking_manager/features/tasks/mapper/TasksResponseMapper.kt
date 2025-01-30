package np.com.naxa.drone_tasking_manager.features.tasks.mapper

import np.com.naxa.drone_tasking_manager.features.projects.mapper.toProjectGeometry
import np.com.naxa.drone_tasking_manager.features.tasks.dto.TaskDto
import np.com.naxa.drone_tasking_manager.features.tasks.models.ProjectTask
import np.com.naxa.drone_tasking_manager.features.tasks.models.ProjectTaskState

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