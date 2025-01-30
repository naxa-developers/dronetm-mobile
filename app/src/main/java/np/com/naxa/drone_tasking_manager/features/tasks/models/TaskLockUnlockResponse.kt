package np.com.naxa.drone_tasking_manager.features.tasks.models

data class TaskLockUnlockResponse(
    val projectId: String? = null,
    val taskId: String? = null,
    val comment: String? = null
)