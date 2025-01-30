package np.com.naxa.drone_tasking_manager.features.project_details.viewmodels.states


import np.com.naxa.drone_tasking_manager.features.projects.models.Project

/**
 * Represents the different states of a project-related operation.
 * This sealed class defines a finite set of possible states, which can be used to
 * represent the lifecycle of fetching, processing, or managing a project.
 */
sealed class ProjectDetailState {
    data object Idle : ProjectDetailState()
    data class Success(val project: Project) : ProjectDetailState()
    data class Error(val message: String) : ProjectDetailState()
    data object Loading : ProjectDetailState()
}