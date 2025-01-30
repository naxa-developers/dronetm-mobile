package np.com.naxa.drone_tasking_manager.features.tasks.viewmodels.states


import np.com.naxa.drone_tasking_manager.features.tasks.models.TaskLockUnlockResponse

/**
 * Represents the state of a task lock or unlock operation.
 * This sealed class allows representing different states that the operation can be in, such as idle, success, error, or requesting.
 */
sealed class TaskLockOrUnlockState {
    data object Idle : TaskLockOrUnlockState()
    data class Success(val response: TaskLockUnlockResponse) : TaskLockOrUnlockState()
    data class Error(val message: String) : TaskLockOrUnlockState()
    data object Requesting : TaskLockOrUnlockState()
}