package np.com.naxa.drone_tasking_manager.features.projects.mapper

import np.com.naxa.drone_tasking_manager.features.projects.dto.project.Outline
import np.com.naxa.drone_tasking_manager.features.projects.dto.project.ProjectDto
import np.com.naxa.drone_tasking_manager.features.projects.dto.projects.ProjectsResponseDto
import np.com.naxa.drone_tasking_manager.features.projects.models.Project
import np.com.naxa.drone_tasking_manager.features.projects.models.ProjectGeometry
import np.com.naxa.drone_tasking_manager.features.projects.models.ProjectStatus
import np.com.naxa.drone_tasking_manager.features.projects.models.ProjectsResponse
import np.com.naxa.drone_tasking_manager.features.tasks.mapper.toProjectTask

fun ProjectDto.toProject() = Project(
    id = id,
    slug = slug,
    name = name,
    description = description,
    perTaskInstructions = perTaskInstructions,
    requiresApprovalFromManagerForLocking = requiresApprovalFromManagerForLocking,
    geometry = outline?.toProjectGeometry(),
    noFlyZones = noFlyZones,
    requiresApprovalFromRegulator = requiresApprovalFromRegulator,
    regulatorEmails = regulatorEmails ?: emptyList(),
    regulatorApprovalStatus = regulatorApprovalStatus,
    imageProcessingStatus = imageProcessingStatus,
    regulatorComment = regulatorComment,
    commentingRegulatorId = commentingRegulatorId,
    authorName = authorName,
    projectArea = projectArea,
    totalTaskCount = totalTaskCount,
    tasks = tasks.map {
        it.toProjectTask().copy(
            projectName = name,
        )
    },
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
    geometry = geometry?.coordinates?.let {
        ProjectGeometry.Geometry(
            type = geometry?.type,
            coordinates = it
        )
    },
    properties = properties?.let {
        properties?.bbox?.let { it1 ->
            ProjectGeometry.Properties(
                id = properties?.id,
                bbox = it1
            )
        }
    },
    id = id,
)