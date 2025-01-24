package np.com.naxa.drone_tasking_manager.features.projects.viewmodels.states

import np.com.naxa.drone_tasking_manager.features.projects.models.Project

/**
 * Represents the different states of the projects list.
 * This sealed class is used to encapsulate the possible states of the UI
 * when displaying a list of projects. It can be in an idle, loading,
 * successful, or error state.
 */
sealed class ProjectsListState {
    data object Idle : ProjectsListState()
    data class Success(val projects: List<Project>) : ProjectsListState()
    data class Error(val message: String) : ProjectsListState()
    data object Loading : ProjectsListState()
}