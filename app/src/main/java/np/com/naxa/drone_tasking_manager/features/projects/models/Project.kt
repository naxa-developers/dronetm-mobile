package np.com.naxa.drone_tasking_manager.features.projects.models

import np.com.naxa.drone_tasking_manager.features.projects.dto.project.NoFlyZones

data class Project(
    val id: String? = null,
    val slug: String? = null,
    val name: String? = null,
    val description: String? = null,
    val perTaskInstructions: String? = null,
    val requiresApprovalFromManagerForLocking: Boolean? = null,
    val geometry: ProjectGeometry? = null,
    val noFlyZones: NoFlyZones? = null,
    val requiresApprovalFromRegulator: Boolean? = null,
    val regulatorEmails: String? = null,
    val regulatorApprovalStatus: String? = null,
    val imageProcessingStatus: String? = null,
    val regulatorComment: String? = null,
    val commentingRegulatorId: String? = null,
    val authorName: String? = null,
    val projectArea: Double? = null,
    val totalTaskCount: Int? = null,
    val tasks: List<ProjectTask> = arrayListOf(),
    val imageUrl: String? = null,
    val ongoingTaskCount: Int? = null,
    val completedTaskCount: Int? = null,
    val status: String? = null,
    val createdAt: String? = null,
    val authorId: String? = null
)