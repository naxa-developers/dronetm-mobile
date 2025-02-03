package np.com.naxa.drone_tasking_manager.features.tasks.viewmodels.states

import np.com.naxa.drone_tasking_manager.features.tasks.models.ProjectTask


/**
 * Represents the different states of the task detail screen.
 *
 * This sealed class defines the possible states that the UI can be in
 * while displaying the details of a specific task.
 *
 * The states include:
 * - [Idle]: The initial state, indicating that no data has been loaded yet.
 * - [Success]: The state when task details are successfully loaded, containing the [ProjectTask].
 * - [Error]: The state when an error occurred during data loading, with an associated [message].
 * - [Loading]: The state when data is being loaded.
 */
sealed class TaskDetailState {
    data object Idle : TaskDetailState()
    data class Success(val projectTask: ProjectTask) : TaskDetailState()
    data class Error(val message: String) : TaskDetailState()
    data object Loading : TaskDetailState()
}