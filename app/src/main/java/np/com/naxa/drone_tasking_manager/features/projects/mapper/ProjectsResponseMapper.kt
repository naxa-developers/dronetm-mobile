package np.com.naxa.drone_tasking_manager.features.projects.mapper

import np.com.naxa.drone_tasking_manager.features.projects.dto.Outline
import np.com.naxa.drone_tasking_manager.features.projects.dto.ProjectsResponseDto
import np.com.naxa.drone_tasking_manager.features.projects.dto.Result
import np.com.naxa.drone_tasking_manager.features.projects.models.Project
import np.com.naxa.drone_tasking_manager.features.projects.models.ProjectGeometry
import np.com.naxa.drone_tasking_manager.features.projects.models.ProjectsResponse


fun Outline.toProjectGeometry() = ProjectGeometry(
    type = type,
    coordinates = coordinates
)

fun Result.toProject() = Project(
    id = id,
    slug = slug,
    name = name,
    description = description,
    perTaskInstructions = perTaskInstructions,
    requiresApprovalFromManagerForLocking = requiresApprovalFromManagerForLocking,
    outline = outline?.toProjectGeometry(),
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
    tasks = tasks,
    imageUrl = imageUrl,
    ongoingTaskCount = ongoingTaskCount,
    completedTaskCount = completedTaskCount,
    status = status,
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