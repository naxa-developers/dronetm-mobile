package np.com.naxa.drone_tasking_manager.features.projects.viewmodels.states

import np.com.naxa.drone_tasking_manager.features.projects.models.Project

/**
 * Represents the state of the projects list screen.
 *
 * This data class encapsulates all the information needed to render the
 * projects list UI, including loading states, the list of projects,
 * and any potential errors.
 *
 * @property refresh Indicates whether the list is currently being refreshed with swipe to refresh.
 *                  This is typically used to show a refreshing indicator to the user.
 *                  Defaults to `false`.
 * @property fetching Indicates whether the list is currently being fetched for the first time or loading more data.
 *                  This is typically used to show a loading indicator while data is being fetched.
 *                  Defaults to `false`.
 * @property projects The list of projects currently displayed.
 *                  Defaults to an empty list.
 * @property error An optional error message that occurred while fetching or refreshing the projects list.
 *                  If `null`, no error has occurred.
 *                  Defaults to `null`.
 * @property onlyMine Indicates whether the list should only display projects created by the current user.
 */
data class ProjectsListState(
    val refresh: Boolean = false,
    val fetching: Boolean = false,
    val projects: List<Project> = emptyList(),
    val error: String? = null,
    val onlyMine: Boolean = false
)