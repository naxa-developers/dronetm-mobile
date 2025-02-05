package np.com.naxa.drone_tasking_manager.features.tasks.viewmodels.states


import np.com.naxa.drone_tasking_manager.features.tasks.models.TaskUnFlyableResponse


/**
 * Represents the different states of a request to mark a task as un-flyable.
 * This sealed class encapsulates the possible outcomes of such a request,
 * allowing for type-safe handling of each state.
 */
sealed class TaskUnFlyableRequestState {
    data object Idle : TaskUnFlyableRequestState()
    data class Success(val response: TaskUnFlyableResponse) : TaskUnFlyableRequestState()
    data class Error(val message: String) : TaskUnFlyableRequestState()
    data object Requesting : TaskUnFlyableRequestState()
}