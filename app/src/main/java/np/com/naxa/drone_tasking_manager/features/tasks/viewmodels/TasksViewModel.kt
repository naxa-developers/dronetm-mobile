package np.com.naxa.drone_tasking_manager.features.tasks.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import np.com.naxa.drone_tasking_manager.core.utils.Response
import np.com.naxa.drone_tasking_manager.features.tasks.usecases.FetchTaskDetailUseCase
import np.com.naxa.drone_tasking_manager.features.tasks.usecases.LockTaskUseCase
import np.com.naxa.drone_tasking_manager.features.tasks.usecases.UnlockTaskUseCase
import np.com.naxa.drone_tasking_manager.features.tasks.viewmodels.events.TasksEvent
import np.com.naxa.drone_tasking_manager.features.tasks.viewmodels.states.TaskDetailState
import np.com.naxa.drone_tasking_manager.features.tasks.viewmodels.states.TaskLockOrUnlockState
import javax.inject.Inject

@HiltViewModel
class TasksViewModel @Inject constructor(
    private val fetchTaskDetailUseCase: FetchTaskDetailUseCase,
    private val lockTaskUseCase: LockTaskUseCase,
    private val unlockTaskUseCase: UnlockTaskUseCase,
) : ViewModel() {

    /**
     * Represents the different states of task details.
     */
    private val _tasksDetailState =
        MutableStateFlow<TaskDetailState>(TaskDetailState.Idle)
    val tasksDetailState = _tasksDetailState.asStateFlow()

    /**
     * Represents the different states of locking task.
     */
    private val _tasksLockState =
        MutableStateFlow<TaskLockOrUnlockState>(TaskLockOrUnlockState.Idle)
    val tasksLockState = _tasksLockState.asStateFlow()

    /**
     * Represents the different states of unlocking task.
     */
    private val _tasksUnlockState =
        MutableStateFlow<TaskLockOrUnlockState>(TaskLockOrUnlockState.Idle)
    val tasksUnlockState = _tasksUnlockState.asStateFlow()


    /**
     * Handles events related to projects.
     *
     * This function receives a [TasksEvent] and performs the corresponding action.
     *
     * @param event The [TasksEvent] to handle.
     *
     * @see TasksEvent
     */
    fun triggerEvent(event: TasksEvent) {
        when (event) {
            is TasksEvent.LockTask -> {
                lockTask(
                    taskId = event.taskId,
                    projectId = event.projectId
                )
            }

            is TasksEvent.UnlockTask -> {
                unlockTask(
                    taskId = event.taskId,
                    projectId = event.projectId
                )
            }

            is TasksEvent.FetchTaskById -> {
                fetchTask(
                    id = event.id,
                    forceRefresh = event.forceRefresh
                )

            }
        }
    }

    /**
     * Locks a task with the given ID and project ID.
     *
     * This function interacts with the `lockTaskUseCase` to lock a specific task.
     * It uses a coroutine launched within the ViewModel's scope to perform the operation
     * asynchronously on the IO dispatcher. The function then collects the result
     * from the `lockTaskUseCase` and updates the `_tasksLockState` accordingly,
     * reflecting the state of the locking process.
     *
     * @param taskId The unique identifier of the task to be locked.
     * @param projectId The unique identifier of the project to which the task belongs.
     *
     * The function updates the `_tasksLockState` LiveData with the following states:
     *   - `TaskLockOrUnlockState.Requesting`: Emitted when the request to lock the task is initiated.
     *   - `TaskLockOrUnlockState.Success(isLocked: Boolean)`: Emitted when the task is successfully locked.
     *        The boolean returned is the result of the lock operation, `true` if the lock was acquired,
     *        `false` otherwise.
     *   - `TaskLockOrUnlockState.Error(message: String)`: Emitted when an error occurs during the task locking process.
     *        The `message` contains details about the error.
     *
     *  It uses the following external components:
     *      - `viewModelScope`: To launch the coroutine within the ViewModel's lifecycle.
     *      - `Dispatchers.IO`: To perform the operation on the IO thread pool, suitable for network or database operations.
     *      - `lockTaskUseCase`: A use case responsible for the actual task locking logic.
     *      - `_tasksLockState`: A MutableStateFlow (or similar) used to emit the current state of the task locking operation.
     *      - `Response`: A sealed class (or similar) representing the different possible outcomes of the use case invocation (Loading, Success, Error).
     */
    private fun lockTask(taskId: String, projectId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            lockTaskUseCase.invoke(taskId, projectId).collect { result ->
                when (result) {
                    is Response.Loading -> {
                        _tasksLockState.emit(TaskLockOrUnlockState.Requesting)
                    }

                    is Response.Success -> {
                        _tasksLockState.emit(
                            TaskLockOrUnlockState.Success(result.data!!)
                        )
                    }

                    is Response.Error -> {
                        _tasksLockState.emit(TaskLockOrUnlockState.Error(result.message))
                    }
                }
            }
        }
    }

    /**
     * Unlocks a specific task within a project.
     *
     * This function interacts with the `unlockTaskUseCase` to unlock a task identified by `taskId`
     * within the project specified by `projectId`. It then observes the result of the operation
     * and updates the `_tasksUnlockState` accordingly, signaling the UI about the progress
     * and outcome of the unlock request.
     *
     * The function operates on the `Dispatchers.IO` coroutine context, suitable for network or disk I/O operations.
     *
     * @param taskId The ID of the task to unlock.
     * @param projectId The ID of the project containing the task.
     *
     * Emits states to `_tasksUnlockState` representing the request lifecycle:
     *   - `TaskLockOrUnlockState.Requesting`: Emitted when the request to unlock is initiated.
     *   - `TaskLockOrUnlockState.Success`: Emitted when the task is successfully unlocked, containing the result data.
     *   - `TaskLockOrUnlockState.Error`: Emitted when an error occurs during the unlock process, providing the error message.
     */
    private fun unlockTask(taskId: String, projectId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            unlockTaskUseCase.invoke(taskId, projectId).collect { result ->
                when (result) {
                    is Response.Loading -> {
                        _tasksUnlockState.emit(TaskLockOrUnlockState.Requesting)
                    }

                    is Response.Success -> {
                        _tasksUnlockState.emit(
                            TaskLockOrUnlockState.Success(result.data!!)
                        )
                    }

                    is Response.Error -> {
                        _tasksUnlockState.emit(TaskLockOrUnlockState.Error(result.message))
                    }
                }
            }
        }
    }

    /**
     * Fetches the details of a task from the data source.
     *
     * This function retrieves the details of a task identified by the given `id`.
     * It uses the `fetchTaskDetailUseCase` to interact with the data layer and collect the results.
     * The result is then processed and emitted to the `_tasksDetailState` as a `TaskDetailState`.
     *
     * @param id The unique identifier of the task to fetch.
     * @param forceRefresh A boolean indicating whether to force a refresh of the data from the
     *                     source, ignoring any cached data. Defaults to `false`.
     *
     * The function handles different states of the response from the use case:
     * - `Response.Loading`: Emits `TaskDetailState.Loading`.
     * - `Response.Error`: Emits `TaskDetailState.Error` with the error message.
     * - `Response.Success`:
     *   - If the data is not null, emits `TaskDetailState.Success` with the task details.
     *   - If the data is null, emits `TaskDetailState.Error` with a "No task found" message.
     *
     * The function operates on the `viewModelScope` using the `Dispatchers.IO` for background operations.
     */
    private fun fetchTask(
        id: String,
        forceRefresh: Boolean = false
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            fetchTaskDetailUseCase.invoke(
                taskId = id,
                forceRefresh = forceRefresh,
            ).collect { result ->
                when (result) {
                    is Response.Loading -> {
                        _tasksDetailState.emit(TaskDetailState.Loading)
                    }

                    is Response.Error -> {
                        _tasksDetailState.emit(TaskDetailState.Error(result.message))
                    }

                    is Response.Success -> {

                        if (result.data != null) {
                            _tasksDetailState.emit(
                                TaskDetailState.Success(
                                    result.data!!,
                                )
                            )

                            return@collect
                        }
                        _tasksDetailState.emit(
                            TaskDetailState.Error("No task found with the given id")
                        )
                    }
                }
            }
        }
    }
}