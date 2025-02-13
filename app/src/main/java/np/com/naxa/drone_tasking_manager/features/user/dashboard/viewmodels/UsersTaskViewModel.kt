package np.com.naxa.drone_tasking_manager.features.user.dashboard.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import np.com.naxa.drone_tasking_manager.core.utils.Response
import np.com.naxa.drone_tasking_manager.features.user.dashboard.viewmodels.events.UsersTaskEvents
import np.com.naxa.drone_tasking_manager.features.user.dashboard.viewmodels.states.UserTaskStatStates
import np.com.naxa.drone_tasking_manager.features.user.dashboard.viewmodels.states.UsersTaskStates
import np.com.naxa.drone_tasking_manager.features.user.dashboard.viewmodels.usecases.UsersTaskStatUseCase
import np.com.naxa.drone_tasking_manager.features.user.dashboard.viewmodels.usecases.UsersTaskUseCase
import javax.inject.Inject

@HiltViewModel
class UsersTaskViewModel @Inject constructor(
    private val usersTaskUseCase: UsersTaskUseCase,
    private val usersTaskStatUseCase: UsersTaskStatUseCase
) : ViewModel() {

    private val _taskState = MutableStateFlow(UsersTaskStates())
    val usersTaskState = _taskState.asStateFlow()

    private val _statState = MutableStateFlow(UserTaskStatStates())
    val usersTaskStatState = _statState.asStateFlow()

    /**
 * Handles events related to user tasks.
 *
 * @param event The event to handle.
 */
fun onEvent(event: UsersTaskEvents) {
    when (event) {
        is UsersTaskEvents.fetchUsersTask -> {
            fetchUsersTaskFromServer(event.forceRefresh)
        }

        is UsersTaskEvents.fetchUsersTaskStat -> {
            fetchUsersTaskStatFromServer(event.forceRefresh)
        }
    }
}

    /**
 * Fetches user tasks from the server.
 *
 * @param forceRefresh Whether to force a refresh of the data.
 */
private fun fetchUsersTaskFromServer(forceRefresh: Boolean) {
    viewModelScope.launch {
        usersTaskUseCase.invoke(forceRefresh = forceRefresh).collect { result ->
            when (result) {
                is Response.Loading -> {
                    _taskState.value = _taskState.value.copy(
                        isFetching = true,
                        isSuccess = false,
                        errorMessage = ""
                    )
                }

                is Response.Error -> {
                    _taskState.value = _taskState.value.copy(
                        isFetching = false,
                        isSuccess = false,
                        errorMessage = result.message,
                        usersTask = null
                    )
                }

                is Response.Success -> {
                    _taskState.value = _taskState.value.copy(
                        isFetching = false,
                        isSuccess = true,
                        usersTask = result.data,
                        errorMessage = ""
                    )
                }
            }
        }
    }
}

    /**
 * Fetches user task statistics from the server.
 *
 * @param forceRefresh Whether to force a refresh of the data.
 */
private fun fetchUsersTaskStatFromServer(forceRefresh: Boolean) {
    viewModelScope.launch {
        usersTaskStatUseCase.invoke(forceRefresh = forceRefresh).collect { result ->
            when (result) {
                is Response.Loading -> {
                    _statState.value = _statState.value.copy(
                        isFetching = true,
                        isSuccess = false,
                        errorMessage = ""
                    )
                }

                is Response.Error -> {
                    _statState.value = _statState.value.copy(
                        isFetching = false,
                        isSuccess = false,
                        errorMessage = result.message,
                        usersTaskStat = null
                    )
                }

                is Response.Success -> {
                    _statState.value = _statState.value.copy(
                        isFetching = false,
                        isSuccess = true,
                        usersTaskStat = result.data,
                        errorMessage = ""
                    )
                }
            }
        }
    }
}
}