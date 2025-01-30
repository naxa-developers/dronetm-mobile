package np.com.naxa.drone_tasking_manager.features.projects.dto.project

import com.google.gson.annotations.SerializedName
import np.com.naxa.drone_tasking_manager.features.tasks.dto.TaskDto


data class ProjectDto(
    @SerializedName("id") var id: String? = null,
    @SerializedName("slug") var slug: String? = null,
    @SerializedName("name") var name: String? = null,
    @SerializedName("description") var description: String? = null,
    @SerializedName("per_task_instructions") var perTaskInstructions: String? = null,
    @SerializedName("requires_approval_from_manager_for_locking") var requiresApprovalFromManagerForLocking: Boolean? = null,
    @SerializedName("outline") var outline: Outline? = null,
    @SerializedName("no_fly_zones") var noFlyZones: NoFlyZones? = null,
    @SerializedName("requires_approval_from_regulator") var requiresApprovalFromRegulator: Boolean? = null,
    @SerializedName("regulator_emails") var regulatorEmails: List<String>? = null,
    @SerializedName("regulator_approval_status") var regulatorApprovalStatus: String? = null,
    @SerializedName("image_processing_status") var imageProcessingStatus: String? = null,
    @SerializedName("regulator_comment") var regulatorComment: String? = null,
    @SerializedName("commenting_regulator_id") var commentingRegulatorId: String? = null,
    @SerializedName("author_name") var authorName: String? = null,
    @SerializedName("project_area") var projectArea: Double? = null,
    @SerializedName("total_task_count") var totalTaskCount: Int? = null,
    @SerializedName("tasks") var tasks: ArrayList<TaskDto> = arrayListOf(),
    @SerializedName("image_url") var imageUrl: String? = null,
    @SerializedName("ongoing_task_count") var ongoingTaskCount: Int? = null,
    @SerializedName("completed_task_count") var completedTaskCount: Int? = null,
    @SerializedName("status") var status: String? = null,
    @SerializedName("created_at") var createdAt: String? = null,
    @SerializedName("author_id") var authorId: String? = null
)