package np.com.naxa.drone_tasking_manager.features.tasks.dto

import com.google.gson.annotations.SerializedName


data class TaskLockUnLockResponseDto(
    @SerializedName("project_id") var projectId: String? = null,
    @SerializedName("task_id") var taskId: String? = null,
    @SerializedName("comment") var comment: String? = null
)