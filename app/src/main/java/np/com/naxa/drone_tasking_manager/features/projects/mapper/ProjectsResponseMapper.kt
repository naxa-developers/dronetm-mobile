package np.com.naxa.drone_tasking_manager.features.projects.mapper

import np.com.naxa.drone_tasking_manager.features.projects.dto.project.Outline
import np.com.naxa.drone_tasking_manager.features.project_details.dto.project.ProjectResponseDto
import np.com.naxa.drone_tasking_manager.features.projects.dto.project.Tasks
import np.com.naxa.drone_tasking_manager.features.projects.dto.projects.ProjectsResponseDto
import np.com.naxa.drone_tasking_manager.features.projects.dto.projects.Result
import np.com.naxa.drone_tasking_manager.features.projects.models.Project
import np.com.naxa.drone_tasking_manager.features.projects.models.ProjectGeometry
import np.com.naxa.drone_tasking_manager.features.projects.models.ProjectStatus
import np.com.naxa.drone_tasking_manager.features.projects.models.ProjectTask
import np.com.naxa.drone_tasking_manager.features.projects.models.ProjectTaskState
import np.com.naxa.drone_tasking_manager.features.projects.models.ProjectsResponse

fun ProjectResponseDto.toProject() = Project(
    id = id,
    slug = slug,
    name = name,
    description = description,
    perTaskInstructions = perTaskInstructions,
    requiresApprovalFromManagerForLocking = requiresApprovalFromManagerForLocking,
    geometry = outline?.toProjectGeometry(),
    noFlyZones = noFlyZones,
    requiresApprovalFromRegulator = requiresApprovalFromRegulator,
    regulatorEmails = regulatorEmails,
    regulatorApprovalStatus = regulatorApprovalStatus,
    imageProcessingStatus = imageProcessingStatus,
    regulatorComment = regulatorComment,
    commentingRegulatorId = commentingRegulatorId,
    authorName = authorName,
    projectArea = projectArea,
    totalTaskCount = totalTaskCount,
    tasks = tasks.map { it.toProjectTask() },
    imageUrl = imageUrl,
    ongoingTaskCount = ongoingTaskCount,
    completedTaskCount = completedTaskCount,
    status = ProjectStatus.fromString(status),
    createdAt = createdAt,
    authorId = authorId,
)

fun Result.toProject() = Project(
    id = id,
    slug = slug,
    name = name,
    description = description,
    perTaskInstructions = perTaskInstructions,
    requiresApprovalFromManagerForLocking = requiresApprovalFromManagerForLocking,
    geometry = ProjectGeometry(
        geometry = outline,
    ),
    noFlyZones = null,
    requiresApprovalFromRegulator = requiresApprovalFromRegulator,
    regulatorEmails = regulatorEmails,
    regulatorApprovalStatus = regulatorApprovalStatus,
    imageProcessingStatus = imageProcessingStatus,
    regulatorComment = regulatorComment,
    commentingRegulatorId = commentingRegulatorId,
    authorName = authorName,
    projectArea = projectArea,
    totalTaskCount = totalTaskCount,
    imageUrl = imageUrl,
    ongoingTaskCount = ongoingTaskCount,
    completedTaskCount = completedTaskCount,
    status = ProjectStatus.fromString(status),
    createdAt = createdAt,
    authorId = authorId,
)

fun ProjectsResponseDto.toProjectResponse() = ProjectsResponse(
    hasNext = pagination?.hasNext,
    hasPrev = pagination?.hasPrev,
    page = pagination?.page,
    total = pagination?.total,
    projects = results.map { it.toProject() }.toList()
)

fun Outline.toProjectGeometry() = ProjectGeometry(
    type = type,
    geometry = geometry,
    properties = properties,
    id = id,
)


fun Tasks.toProjectTask() = ProjectTask(
    id = id,
    projectId = projectId,
    projectTaskIndex = projectTaskIndex,
    geometry = outline?.toProjectGeometry(),
    state = ProjectTaskState.fromString(state),
    userId = userId,
    name = name,
    imageCount = imageCount,
    assetsUrl = assetsUrl,
    totalAreaSqkm = totalAreaSqkm,
    flightTimeMinutes = flightTimeMinutes,
    flightDistanceKm = flightDistanceKm,
    totalImageUploaded = totalImageUploaded,
)