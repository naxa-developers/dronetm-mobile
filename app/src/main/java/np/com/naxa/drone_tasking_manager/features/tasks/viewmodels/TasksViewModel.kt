package np.com.naxa.drone_tasking_manager.features.tasks.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import np.com.naxa.drone_tasking_manager.core.utils.Response
import np.com.naxa.drone_tasking_manager.features.projects.dto.projects_centroid.Centroid
import np.com.naxa.drone_tasking_manager.features.tasks.usecases.FetchTaskDetailUseCase
import np.com.naxa.drone_tasking_manager.features.tasks.usecases.LockTaskUseCase
import np.com.naxa.drone_tasking_manager.features.tasks.usecases.TaskWayPointsOrWayLinesUseCase
import np.com.naxa.drone_tasking_manager.features.tasks.usecases.UnFlyableTaskUseCase
import np.com.naxa.drone_tasking_manager.features.tasks.usecases.UnlockTaskUseCase
import np.com.naxa.drone_tasking_manager.features.tasks.usecases.UpdateTakeOffPointUseCase
import np.com.naxa.drone_tasking_manager.features.tasks.viewmodels.events.TasksEvent
import np.com.naxa.drone_tasking_manager.features.tasks.viewmodels.states.TaskDetailState
import np.com.naxa.drone_tasking_manager.features.tasks.viewmodels.states.TaskLockOrUnlockState
import np.com.naxa.drone_tasking_manager.features.tasks.viewmodels.states.TaskUnFlyableRequestState
import np.com.naxa.drone_tasking_manager.features.tasks.viewmodels.states.TaskWayPointsOrWayLinesState
import np.com.naxa.drone_tasking_manager.utils.rotate
import org.maplibre.android.geometry.LatLng
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Point
import javax.inject.Inject

@HiltViewModel
class TasksViewModel @Inject constructor(
    private val fetchTaskDetailUseCase: FetchTaskDetailUseCase,
    private val lockTaskUseCase: LockTaskUseCase,
    private val unlockTaskUseCase: UnlockTaskUseCase,
    private val unFlyableTaskUseCase: UnFlyableTaskUseCase,
    private val wayPointsOrWayLinesUseCase: TaskWayPointsOrWayLinesUseCase,
    private val updateTakeOffPointUseCase: UpdateTakeOffPointUseCase
) : ViewModel() {

    /**
     * Represents the different states of task details.
     */
    private val _taskDetailState =
        MutableStateFlow<TaskDetailState>(TaskDetailState.Idle)
    val taskDetailState = _taskDetailState.asStateFlow()

    /**
     * Represents the different states of locking task.
     */
    private val _taskLockState =
        MutableStateFlow<TaskLockOrUnlockState>(TaskLockOrUnlockState.Idle)
    val taskLockState = _taskLockState.asStateFlow()

    /**
     * Represents the different states of unlocking task.
     */
    private val _taskUnlockState =
        MutableStateFlow<TaskLockOrUnlockState>(TaskLockOrUnlockState.Idle)
    val taskUnlockState = _taskUnlockState.asStateFlow()

    /**
     * Represents the different states of task un flyable request
     */
    private val _taskUnFlyableRequestState =
        MutableStateFlow<TaskUnFlyableRequestState>(TaskUnFlyableRequestState.Idle)
    val taskUnFlyableRequestState = _taskUnFlyableRequestState.asStateFlow()

    /**
     * Represents the different states of task way points or way lines.
     */
    private val _taskWayPointsOrWayLinesState =
        MutableStateFlow<TaskWayPointsOrWayLinesState>(TaskWayPointsOrWayLinesState.Idle)
    val taskWayPointsOrWayLinesState = _taskWayPointsOrWayLinesState.asStateFlow()


    /**
     * The collection of features to be displayed on the map.
     *
     * This property holds the GeoJSON FeatureCollection that defines the features
     * (e.g., points, lines, polygons) to be visualized.  Setting this property
     * triggers an update to the map's displayed features.
     *
     * If `null`, no features will be displayed.
     */
    private var _featureCollection: FeatureCollection? = null
    private var _changeableFeatureCollection: FeatureCollection? = null


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

            is TasksEvent.ResetState -> {
                viewModelScope.launch {
                    if (event.lockState) {
                        _taskLockState.emit(TaskLockOrUnlockState.Idle)
                    }

                    if (event.unlockState) {
                        _taskUnlockState.emit(TaskLockOrUnlockState.Idle)
                    }

                    if (event.unFlyableState) {
                        _taskUnFlyableRequestState.emit(TaskUnFlyableRequestState.Idle)
                    }

                    if (event.taskDetailState) {
                        _taskDetailState.emit(TaskDetailState.Idle)
                    }

                    if (event.taskWayPointsOrWayLinesState) {
                        _taskWayPointsOrWayLinesState.emit(TaskWayPointsOrWayLinesState.Idle)
                    }
                }
            }

            is TasksEvent.FetchWayPointsOrWayLines -> {
                fetchWayPointsOrWayLines(
                    taskId = event.taskId,
                    projectId = event.projectId,
                    rotationAngle = event.rotationAngle,
                    download = event.download,
                    isWayPoints = event.isWayPoints,
                    forceRefresh = event.forceRefresh
                )
            }

            is TasksEvent.RotateWayPointsOrWayLines -> {
                rotate(
                    angle = event.angle,
                    centroid = event.centroid,
                    onRotatedSuccess = event.onRotatedSuccess
                )
            }

            is TasksEvent.FlagTaskAsUnFlyable -> {
                unFlyableTask(
                    taskId = event.taskId,
                    projectId = event.projectId,
                    comment = event.comment
                )
            }

            is TasksEvent.DragTakeOffPoint -> {
                handleDragTakeOffPoint(
                    latLng = event.latLng,
                    onFeatureCollectionUpdated = event.onFeatureCollectionUpdated
                )
            }

            is TasksEvent.UpdateTakeOffPoint -> {
                updateTakeOffPoint(
                    taskId = event.taskId,
                    projectId = event.projectId,
                    rotationAngle = event.rotationAngle,
                    download = event.download,
                    isWayPoints = event.isWayPoints,
                    latitude = event.latitude,
                    longitude = event.longitude
                )
            }

            is TasksEvent.RestoreFeatureCollection -> {
                restoreFeatureCollection {
                    event.onRestored.invoke(it)
                }
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
                        _taskLockState.emit(TaskLockOrUnlockState.Requesting)
                    }

                    is Response.Success -> {
                        _taskLockState.emit(
                            TaskLockOrUnlockState.Success(result.data!!)
                        )
                    }

                    is Response.Error -> {
                        _taskLockState.emit(TaskLockOrUnlockState.Error(result.message))
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
                        _taskUnlockState.emit(TaskLockOrUnlockState.Requesting)
                    }

                    is Response.Success -> {
                        _taskUnlockState.emit(
                            TaskLockOrUnlockState.Success(result.data!!)
                        )
                    }

                    is Response.Error -> {
                        _taskUnlockState.emit(TaskLockOrUnlockState.Error(result.message))
                    }
                }
            }
        }
    }

    private fun unFlyableTask(taskId: String, projectId: String, comment: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            unFlyableTaskUseCase.invoke(taskId, projectId, comment).collect { result ->
                when (result) {
                    is Response.Loading -> {
                        _taskUnFlyableRequestState.emit(TaskUnFlyableRequestState.Requesting)
                    }

                    is Response.Success -> {
                        _taskUnFlyableRequestState.emit(
                            TaskUnFlyableRequestState.Success(result.data!!)
                        )
                    }

                    is Response.Error -> {
                        _taskUnFlyableRequestState.emit(TaskUnFlyableRequestState.Error(result.message))
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
                        _taskDetailState.emit(TaskDetailState.Loading)
                    }

                    is Response.Error -> {
                        _taskDetailState.emit(TaskDetailState.Error(result.message))
                    }

                    is Response.Success -> {

                        if (result.data != null) {
                            _taskDetailState.emit(
                                TaskDetailState.Success(
                                    result.data!!,
                                )
                            )

                            return@collect
                        }
                        _taskDetailState.emit(
                            TaskDetailState.Error("No task found with the given id")
                        )
                    }
                }
            }
        }
    }

    /**
     * Fetches waypoints or waylines for a given task and project.
     *
     * This function retrieves either waypoints or waylines associated with a specific task within a project.
     * It interacts with a use case to perform the data fetching and updates a state object to reflect the current status
     * (loading, success, or error) of the operation.
     *
     * @param taskId The ID of the task for which to fetch waypoints or waylines.
     * @param projectId The ID of the project to which the task belongs.
     * @param rotationAngle An optional rotation angle (in degrees) that might be applied to the fetched data. Defaults to 0.
     * @param download A boolean flag indicating whether the data should be downloaded. Defaults to false.
     *                 If set to true, the function might attempt to download the data from a remote source.
     * @param isWayPoints A boolean flag indicating whether to fetch waypoints (true) or waylines (false).
     * @param forceRefresh A boolean flag indicating whether to force a refresh of the data, bypassing any potential cache.
     *                     Defaults to true.
     */
    private fun fetchWayPointsOrWayLines(
        taskId: String,
        projectId: String,
        rotationAngle: Int = 0,
        download: Boolean = false,
        isWayPoints: Boolean,
        forceRefresh: Boolean = true,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            wayPointsOrWayLinesUseCase.invoke(
                taskId,
                projectId,
                rotationAngle,
                download,
                isWayPoints,
                forceRefresh
            ).collect { result ->
                when (result) {
                    is Response.Loading -> {
                        _featureCollection = null
                        _changeableFeatureCollection = null
                        _taskWayPointsOrWayLinesState.emit(TaskWayPointsOrWayLinesState.Loading)
                    }

                    is Response.Error -> {
                        _taskWayPointsOrWayLinesState.emit(TaskWayPointsOrWayLinesState.Error(result.message))
                    }

                    is Response.Success -> {
                        _featureCollection = result.data

                        if (result.data != null) {
                            _taskWayPointsOrWayLinesState.emit(
                                TaskWayPointsOrWayLinesState.Success(
                                    result.data!!,
                                    isWayPoints
                                )
                            )

                            return@collect
                        }

                        _taskWayPointsOrWayLinesState.emit(
                            TaskWayPointsOrWayLinesState.Error("No task ${if (isWayPoints) "waypoints" else "waylines"} found with the given id")
                        )
                    }
                }
            }
        }
    }

    /**
     * Rotates the features in the current feature collection around a given centroid by a specified angle.
     *
     * This function performs the rotation operation asynchronously on the IO dispatcher.
     * It iterates through the features of the internal [_featureCollection],
     * rotates each `Point` geometry around the provided `centroid` using the given `angle`, and creates a new `FeatureCollection` with the rotated geometries.
     * Non-`Point` geometries are not rotated.
     *
     * @param angle The angle of rotation in degrees. Positive values rotate clockwise, negative values rotate counterclockwise.
     * @param centroid An optional [Centroid] representing the point around which the features should be rotated.
     *                 If `null`, no rotation will occur.
     * @param onRotatedSuccess A callback function that is invoked with the new [FeatureCollection] containing the rotated features
     *                         if rotation was successful (i.e., `centroid` is not null).
     *                         This callback is executed on the main thread.
     */
    private fun rotate(
        angle: Float,
        centroid: Centroid? = null,
        onRotatedSuccess: (FeatureCollection) -> Unit
    ) {
        if (angle == 0f) {
            _featureCollection?.let {
                _changeableFeatureCollection = it
                viewModelScope.launch(Dispatchers.Main) {
                    onRotatedSuccess.invoke(it)
                }
            }

            return
        }

        if (_changeableFeatureCollection == null) _changeableFeatureCollection = _featureCollection

        viewModelScope.launch(Dispatchers.IO) {
            val rotated = centroid?.let {
                FeatureCollection.fromFeatures(
                    _changeableFeatureCollection?.features()?.map { feature ->
                        Feature.fromGeometry(
                            when (val geometry = feature.geometry()) {
                                is Point -> geometry.rotate(
                                    Point.fromLngLat(
                                        it.coordinates.first(),
                                        it.coordinates.last()
                                    ), angle.toDouble()
                                )

                                else -> geometry
                            },
                            feature.properties(),
                            feature.id(),
                            feature.bbox()
                        )
                    }?.toTypedArray() ?: emptyArray()
                )
            }

            rotated?.let {
                _changeableFeatureCollection = it
                launch(Dispatchers.Main) {
                    onRotatedSuccess.invoke(it)
                }
            }
        }
    }

    /**
     * Handles the "take-off point" drag operation, updating the feature collection with the new location.
     *
     * This function is responsible for updating the location of a specific point feature (presumably the "take-off point")
     * within a `FeatureCollection` when it's dragged to a new location on the map. It modifies the `Point` geometry of
     * the corresponding feature in the collection and notifies the caller of the updated `FeatureCollection`.
     *
     * @param latLng The new latitude and longitude representing the dragged location of the take-off point.
     * @param onFeatureCollectionUpdated The callback function to be invoked with the updated `FeatureCollection`
     *
     */
    private fun handleDragTakeOffPoint(
        latLng: LatLng,
        onFeatureCollectionUpdated: (FeatureCollection) -> Unit
    ) {

        if (_changeableFeatureCollection == null) _changeableFeatureCollection = _featureCollection

        viewModelScope.launch(Dispatchers.IO) {
            val updated = _changeableFeatureCollection?.features()?.map { feature ->
                val index = if (feature.properties()
                        ?.get("index")?.isJsonPrimitive == true
                ) feature.properties()?.get("index")?.asJsonPrimitive else null

                if (index != null && index.isNumber && index.asNumber.toDouble() == 0.0) {
                    Feature.fromGeometry(
                        when (val geometry = feature.geometry()) {
                            is Point -> Point.fromLngLat(
                                latLng.longitude,
                                latLng.latitude,
                                geometry.altitude()
                            )

                            else -> geometry
                        },
                        feature.properties(),
                        feature.id(),
                        feature.bbox()
                    )
                } else {
                    feature
                }
            }?.toTypedArray()


            updated?.let {
                val collection = FeatureCollection.fromFeatures(it)
                _changeableFeatureCollection = collection
                launch(Dispatchers.Main) {
                    onFeatureCollectionUpdated.invoke(collection)
                }
            }
        }
    }

    /**
     * Updates the take-off point for a given task.
     *
     * This function communicates with the `updateTakeOffPointUseCase` to update the
     * take-off point (and potentially related data like waypoints or waylines)
     * associated with a specific task and project. It handles the different
     * states of the network request (Loading, Error, Success) and updates the
     * internal state accordingly.
     *
     * @param taskId The ID of the task to update.
     * @param projectId The ID of the project the task belongs to.
     * @param latitude The latitude of the new take-off point.
     * @param longitude The longitude of the new take-off point.
     * @param rotationAngle The rotation angle associated with the take-off point (default: 0).
     * @param download Indicates whether to download related data (e.g., waypoints) (default: false).
     * @param isWayPoints A flag indicating whether to treat the data as waypoints (true) or waylines (false) (default: true).
     *
     * The function performs the following actions:
     * 1. Launches a coroutine on the IO dispatcher to perform network operations.
     * 2. Invokes the `updateTakeOffPointUseCase` with the provided parameters.
     * 3. Collects the result of the use case, which is a `Response` object.
     * 4. Based on the result type:
     *    - `Response.Loading`: Emits a `TaskWayPointsOrWayLinesState.Loading` state, clears the `_featureCollection` and `_changableFeatureCollection`
     *    - `Response.Error`: Emits a `TaskWayPointsOrWayLinesState.Error` state with the error message.
     *    - `Response.Success`:
     *      - If the data is not null, it emits a `TaskWayPointsOrWayLinesState.Success` state with the received data and the `isWayPoints` flag.
     *      - If the data is null, it emits a `TaskWayPointsOrWayLinesState.Error` state with a message indicating that no waypoints or waylines were found.
     *
     *  It updates the following private properties:
     *      _featureCollection: updated with data */
    private fun updateTakeOffPoint(
        taskId: String,
        projectId: String,
        latitude: Double,
        longitude: Double,
        rotationAngle: Int = 0,
        download: Boolean = false,
        isWayPoints: Boolean = true,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            updateTakeOffPointUseCase.invoke(
                taskId,
                projectId,
                rotationAngle,
                download,
                if (isWayPoints) "waypoints" else "waylines",
                true,
                latitude,
                longitude
            ).collect { result ->
                when (result) {
                    is Response.Loading -> {
                        _featureCollection = null
                        _changeableFeatureCollection = null
                        _taskWayPointsOrWayLinesState.emit(TaskWayPointsOrWayLinesState.Loading)
                    }

                    is Response.Error -> {
                        _taskWayPointsOrWayLinesState.emit(TaskWayPointsOrWayLinesState.Error(result.message))
                    }

                    is Response.Success -> {
                        _featureCollection = result.data

                        if (result.data != null) {
                            _taskWayPointsOrWayLinesState.emit(
                                TaskWayPointsOrWayLinesState.Success(
                                    result.data!!,
                                    isWayPoints
                                )
                            )

                            return@collect
                        }

                        _taskWayPointsOrWayLinesState.emit(
                            TaskWayPointsOrWayLinesState.Error("No task ${if (isWayPoints) "waypoints" else "waylines"} found with the given id")
                        )
                    }
                }
            }
        }
    }

    /**
     * Restores the previously cached FeatureCollection.
     *
     * This function attempts to restore a cached `FeatureCollection` (if available) and
     * provides it to the caller via the `onRestored` callback. If a FeatureCollection
     * was previously stored in `_featureCollection`, it will be copied to
     * `_changableFeatureCollection` and then delivered to the `onRestored` callback.
     * The callback is invoked on the Main (UI) thread.
     *
     * @param onRestored A callback function that receives the restored `FeatureCollection`.
     *                     This callback will be executed on the main thread. It will be
     *                     invoked if and only if `_featureCollection` is not null.
     *
     * @see _featureCollection
     * @see _changeableFeatureCollection
     */
    private fun restoreFeatureCollection(onRestored: (FeatureCollection) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            _featureCollection?.let {
                _changeableFeatureCollection = it
                viewModelScope.launch(Dispatchers.Main) {
                    onRestored.invoke(it)
                }
            }
        }
    }
}