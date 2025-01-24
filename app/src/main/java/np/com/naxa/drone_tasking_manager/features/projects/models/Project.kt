package np.com.naxa.drone_tasking_manager.features.projects.models

data class Project(
    val id: String? = null,
    val slug: String? = null,
    val name: String? = null,
    val description: String? = null,
    val perTaskInstructions: String? = null,
    val requiresApprovalFromManagerForLocking: Boolean? = null,
    val outline: ProjectGeometry? = null,
    val noFlyZones: String? = null,
    val requiresApprovalFromRegulator: Boolean? = null,
    val regulatorEmails: String? = null,
    val regulatorApprovalStatus: String? = null,
    val imageProcessingStatus: String? = null,
    val regulatorComment: String? = null,
    val commentingRegulatorId: String? = null,
    val authorName: String? = null,
    val projectArea: String? = null,
    val totalTaskCount: Int? = null,
    val tasks: ArrayList<String> = arrayListOf(),
    val imageUrl: String? = null,
    val ongoingTaskCount: Int? = null,
    val completedTaskCount: Int? = null,
    val status: String? = null,
    val createdAt: String? = null,
    val authorId: String? = null
)