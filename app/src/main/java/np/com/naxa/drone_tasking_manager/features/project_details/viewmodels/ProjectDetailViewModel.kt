package np.com.naxa.drone_tasking_manager.features.project_details.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import np.com.naxa.drone_tasking_manager.core.utils.Response
import np.com.naxa.drone_tasking_manager.features.project_details.usecases.FetchProjectDetailUseCase
import np.com.naxa.drone_tasking_manager.features.project_details.viewmodels.events.ProjectDetailEvent
import np.com.naxa.drone_tasking_manager.features.project_details.viewmodels.states.ProjectDetailState
import javax.inject.Inject

@HiltViewModel
class ProjectDetailViewModel @Inject constructor(
    private val fetchSingleProjectUseCase: FetchProjectDetailUseCase
) : ViewModel() {

    /**
     * Represents the different states of fetching project by id.
     */
    private val _projectState = MutableStateFlow<ProjectDetailState>(ProjectDetailState.Idle)
    val projectState = _projectState.asStateFlow()

    /**
     * Handles events related to projects.
     *
     * This function receives a [ProjectDetailEvent] and performs the corresponding action.
     * Currently, it supports fetching a project by its ID and fetching all projects.
     *
     * @param event The [ProjectDetailEvent] to handle.
     *
     * @see ProjectDetailEvent
     */
    fun triggerEvent(event: ProjectDetailEvent) {
        when (event) {
            is ProjectDetailEvent.FetchProjectById -> {
                fetchProjectById(event.id)
            }
        }
    }


    private fun fetchProjectById(
        id: String,
        forceRefresh: Boolean = false
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            fetchSingleProjectUseCase.invoke(
                id = id,
                forceRefresh = forceRefresh,
            ).collect { result ->
                when (result) {
                    is Response.Loading -> {
                        _projectState.emit(ProjectDetailState.Loading)
                    }

                    is Response.Error -> {
                        _projectState.emit(ProjectDetailState.Error(result.message))
                    }

                    is Response.Success -> {

                        if (result.data != null) {
                            _projectState.emit(
                                ProjectDetailState.Success(
                                    result.data!!,
                                )
                            )

                            return@collect
                        }
                        _projectState.emit(
                            ProjectDetailState.Error("No project found with the given id")
                        )
                    }
                }
            }
        }
    }

}