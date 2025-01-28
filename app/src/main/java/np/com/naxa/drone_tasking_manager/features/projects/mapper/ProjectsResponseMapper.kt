package np.com.naxa.drone_tasking_manager.features.projects.mapper

import np.com.naxa.drone_tasking_manager.features.projects.dto.project.Outline
import np.com.naxa.drone_tasking_manager.features.project_details.dto.project.ProjectResponseDto
import np.com.naxa.drone_tasking_manager.features.projects.dto.projects.ProjectsResponseDto
import np.com.naxa.drone_tasking_manager.features.projects.dto.projects.Result
import np.com.naxa.drone_tasking_manager.features.projects.models.Project
import np.com.naxa.drone_tasking_manager.features.projects.models.ProjectGeometry
import np.com.naxa.drone_tasking_manager.features.projects.models.ProjectsResponse

fun ProjectResponseDto.toProject() = Project(
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

fun Result.toProject() = Project(
    id = id,
    slug = slug,
    name = name,
    description = description,
    perTaskInstructions = perTaskInstructions,
    requiresApprovalFromManagerForLocking = requiresApprovalFromManagerForLocking,
    outline = ProjectGeometry(
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

fun Outline.toProjectGeometry() = ProjectGeometry(
    type = type,
    geometry = geometry,
    properties = properties,
    id = id,
)