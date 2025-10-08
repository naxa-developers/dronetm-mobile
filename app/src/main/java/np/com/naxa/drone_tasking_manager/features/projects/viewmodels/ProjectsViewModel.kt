package np.com.naxa.drone_tasking_manager.features.projects.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import np.com.naxa.drone_tasking_manager.core.utils.Response
import np.com.naxa.drone_tasking_manager.features.projects.models.ProjectsResponse
import np.com.naxa.drone_tasking_manager.features.projects.usecases.FetchProjectsCentroidUseCase
import np.com.naxa.drone_tasking_manager.features.projects.usecases.FetchProjectsUseCase
import np.com.naxa.drone_tasking_manager.features.projects.viewmodels.events.ProjectsEvent
import np.com.naxa.drone_tasking_manager.features.projects.viewmodels.states.ProjectsCentroidState
import np.com.naxa.drone_tasking_manager.features.projects.viewmodels.states.ProjectsListState
import javax.inject.Inject

@HiltViewModel
class ProjectsViewModel @Inject constructor(
    private val fetchProjectsUseCase: FetchProjectsUseCase,
    private val fetchProjectsCentroidUseCase: FetchProjectsCentroidUseCase,
) : ViewModel() {

    /**
     * Represents the different states of fetching projects.
     */
    private val _projectsState = MutableStateFlow(ProjectsListState())
    val projectsState = _projectsState.asStateFlow()

    /**
     * Represents the different states of fetching projects centroid data to show in map.
     */
    private val _projectsCentroidState =
        MutableStateFlow<ProjectsCentroidState>(ProjectsCentroidState.Idle)
    val projectsCentroidState = _projectsCentroidState.asStateFlow()

    /**
     * The response object containing the list of projects and paginated data.
     *
     * @see ProjectsResponse
     */
    private var _projectsResponse: ProjectsResponse? = null


    /**
     * Handles events related to projects.
     *
     * This function receives a [ProjectsEvent] and performs the corresponding action.
     * Currently, it supports fetching a project by its ID and fetching all projects.
     *
     * @param event The [ProjectsEvent] to handle.
     *
     * @see ProjectsEvent
     */
    fun triggerEvent(event: ProjectsEvent) {
        when (event) {
            is ProjectsEvent.FetchProjects -> {
                fetchProjects(
                    size = event.size,
                    query = event.query,
                    onlyMine = event.onlyMine,
                    refresh = event.refresh,
                    onCompleted = event.onCompleted,
                )
            }

            is ProjectsEvent.FetchProjectsCentroid -> {
                fetchProjectsCentroid(event.forceFully)
            }
        }
    }


    /**
     * Fetches a list of projects from the data source.
     *
     * This function interacts with the `fetchProjectsUseCase` to retrieve a paginated list of projects.
     * It handles different response states (Loading, Error, Success) and updates the `_projectsState`
     * accordingly. The function operates in the IO dispatcher to perform the network/database operation.
     *
     * @param refresh A boolean flag indicating whether to refresh the project list. Defaults to false.
     * @param size The number of projects to fetch per page. Defaults to 20.
     * @param query An optional search query to filter projects by. If null, no filtering is applied.
     * @param onlyMine A boolean flag indicating whether to fetch only the current user's projects. Defaults to false.
     */
    private fun fetchProjects(
        size: Int = 20,
        query: String?,
        onlyMine: Boolean = false,
        refresh: Boolean = false,
        onCompleted: (() -> Unit)? = null
    ) {

        if (refresh) _projectsResponse = null
        if (_projectsResponse != null && _projectsResponse?.hasNext == false) return

        val page = (_projectsResponse?.page ?: 0) + 1

        viewModelScope.launch(Dispatchers.IO) {
            fetchProjectsUseCase.invoke(
                page = page,
                size = size,
                query = query,
                onlyMine = onlyMine,
            ).collect { result ->
                when (result) {
                    is Response.Loading -> {
                        _projectsState.emit(
                            ProjectsListState(
                                refresh = refresh,
                                fetching = true,
                                projects = if (page == 1) emptyList() else projectsState.value.projects,
                                error = null
                            )
                        )
                    }

                    is Response.Error -> {
                        _projectsState.emit(
                            ProjectsListState(
                                fetching = false,
                                projects = if (page == 1) emptyList() else projectsState.value.projects,
                                error = if (page == 1) result.message else null,
                                onlyMine = onlyMine
                            )
                        )
                    }

                    is Response.Success -> {
                        _projectsResponse = result.data

                        _projectsState.emit(
                            ProjectsListState(
                                fetching = false,
                                projects = if (page == 1) result.data?.projects
                                    ?: emptyList() else listOf(
                                    *(projectsState.value.projects.toTypedArray()),
                                    *(result.data?.projects?.toTypedArray() ?: emptyArray())
                                ),
                                error = null,
                                onlyMine = onlyMine
                            )
                        )
                    }
                }
            }
        }.invokeOnCompletion {
            onCompleted?.invoke()
        }
    }

    /**
     * Fetches a projects centroid from the data source.
     *
     * This function interacts with the `fetchSingleProjectUseCase` to retrieve a list of projects centroid.
     * It handles different response states (Loading, Error, Success) and updates the `_projectsCentroidState`
     * accordingly. The function operates in the IO dispatcher to perform the network/database operation.
     *
     */
    private fun fetchProjectsCentroid(forceFully: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            fetchProjectsCentroidUseCase.invoke(forceFully).collect { result ->
                when (result) {
                    is Response.Loading -> {
                        _projectsCentroidState.emit(ProjectsCentroidState.Loading)
                    }

                    is Response.Success -> {
                        _projectsCentroidState.emit(
                            ProjectsCentroidState.Success(result.data ?: emptyList())
                        )
                    }

                    is Response.Error -> {
                        _projectsCentroidState.emit(ProjectsCentroidState.Error(result.message))
                    }
                }
            }
        }
    }
}