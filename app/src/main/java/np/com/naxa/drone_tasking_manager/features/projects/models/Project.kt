package np.com.naxa.drone_tasking_manager.features.projects.models

import androidx.compose.ui.graphics.Color
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
    val status: ProjectStatus? = null,
    val createdAt: String? = null,
    val authorId: String? = null
)

enum class ProjectStatus(
    val color: Color,
    val label: String
) {
    Ongoing(Color(65, 126, 201), "Ongoing"),
    NotStarted(Color(128, 128, 128), "Not Started"),
    Completed(Color(2, 138, 15), "Completed"), ;

    val key: String = name
        .replace(Regex("(?<!^)([A-Z])"), "-$1")
        .lowercase()

    companion object {
        private val keyLookup by lazy { entries.associateBy { it.key } }
        fun fromString(state: String?) = state?.trim()?.lowercase()?.let { keyLookup[it] }
    }
}