package np.com.naxa.drone_tasking_manager.features.tasks.viewmodels.events

import np.com.naxa.drone_tasking_manager.features.projects.dto.projects_centroid.Centroid
import np.com.naxa.drone_tasking_manager.features.tasks.models.ProjectTask
import org.maplibre.android.geometry.LatLng
import org.maplibre.geojson.FeatureCollection


sealed class TasksEvent {

    /**
     * Represents an event to fetch a specific task by its ID.
     *
     * This event is used to trigger the fetching of a task with the given ID.
     * It can optionally force a refresh of the data, bypassing any cached values.
     *
     * @property id The unique identifier of the task to be fetched.
     * @property forceRefresh Indicates whether to force a refresh of the task data.
     *                       If `true`, the data will be fetched from the source, ignoring any cached values.
     *                       If `false` (default), the system may use cached data if available.
     *
     * @constructor Creates a [FetchTaskById] event with the specified task ID and refresh option.
     *
     * Example usage:
     * ```kotlin
     * // Fetch task with ID "123" and use cached data if available.
     * val fetchTaskEvent = FetchTaskById(id = "123")
     *
     * // Fetch task with ID "456" and force a refresh from the data source.
     * val fetchTaskEventForceRefresh = FetchTaskById(id = "456", forceRefresh = true)
     * ```
     */
    data class FetchTaskById(val id: String, val forceRefresh: Boolean = false) : TasksEvent()

    /**
     * Represents an event indicating that a task should be unlocked.
     *
     * This event is typically used to signal that a task, previously locked
     * (e.g., for editing or processing), is now available for modification
     * or further actions.  It contains the ID of the task to unlock and
     * the ID of the project it belongs to.
     *
     * @property taskId The unique identifier of the task to be unlocked.
     * @property projectId The unique identifier of the project to which the task belongs.
     *
     * This class extends [TasksEvent], indicating that it is a specific type of
     * event related to tasks.
     */


    data class UnlockTask(val taskId: String, val projectId: String) : TasksEvent()

    /**
     * Represents a task locking event.
     *
     * This data class is used to encapsulate the information related to a task
     * being locked. It includes the unique identifier of the task and the
     * project to which the task belongs.
     *
     * @property taskId The unique identifier of the task that was locked.
     * @property projectId The unique identifier of the project to which the locked task belongs.
     */
    data class LockTask(val taskId: String, val projectId: String) : TasksEvent()

    /**
     * Data class representing a request to fetch waypoints or waylines.
     *
     * This class encapsulates the necessary information for triggering the retrieval
     * of either waypoints or waylines associated with a specific task and project.
     * It also includes options for controlling the data retrieval behavior, such as
     * rotation angle, whether to download data, whether to fetch waypoints or waylines,
     * and whether to force a refresh of the data.
     *
     * @property task The task for which waypoints or waylines are being requested.
     * @property rotationAngle The rotation angle (in degrees) to be applied to the waypoints/waylines. Defaults is null.
     * It will be handled on viewmodel.
     * @property download Indicates whether the fetched data should be downloaded. Defaults to false.
     * @property isWayPoints A flag indicating whether waypoints (true) or waylines (false) are being requested.
     * @property takeOffPoint The take-off point (LatLng) associated with the task. Defaults to null.
     * @property rasterDemFilePath The path to the raster DEM file. Defaults to null.
     * @property offline Indicates whether the data should be downloaded offline. Defaults to false.
     */
    data class FetchWayPointsOrWayLines(
        val task: ProjectTask,
        val rotationAngle: Int? = null,
        val download: Boolean = false,
        val isWayPoints: Boolean,
        val takeOffPoint: LatLng? = null,
        val rasterDemFilePath: String? = null,
        val offline: Boolean = false,
    ) : TasksEvent()

    /**
     * Represents an event to rotate waypoints or waylines.
     *
     * This data class encapsulates the parameters required to perform a rotation operation on a
     * collection of waypoints or waylines represented as a [FeatureCollection].
     *
     * @property angle The angle (in degrees) by which to rotate the waypoints or waylines.
     *                 Positive values indicate clockwise rotation, while negative values
     *                 indicate counterclockwise rotation.
     * @property centroid An optional [Centroid] object representing the center point around
     *                    which the rotation should occur. If `null`, the rotation will be
     *                    performed around the geometric center of the waypoints/waylines.
     *                    If it's not provided, it will be computed based on the FeatureCollection.
     * @property onRotatedSuccess A lambda function that will be invoked when the rotation
     *                             operation is successfully completed. It receives the
     *                             rotated [FeatureCollection] as a parameter.
     *
     * @constructor Creates a new instance of RotateWayPointsOrWayLines.
     */
    data class RotateWayPointsOrWayLines(
        val angle: Float,
        val centroid: Centroid? = null,
        val onRotatedSuccess: (FeatureCollection) -> Unit,
        val taskPolygon: ArrayList<ArrayList<ArrayList<Double>>>,
    ) : TasksEvent()

    /**
     * Represents the point where a drag operation starts, along with an action to be performed
     * when the underlying FeatureCollection is updated.
     *
     * This data class encapsulates the geographical location (LatLng) where a user initiates a
     * drag action on a map, such as dragging a marker or a shape. It also includes a callback
     * function (`onFeatureCollectionUpdated`) that will be invoked whenever the FeatureCollection
     * associated with the drag operation is modified. This allows for real-time updates and
     * reactions to changes in the map's data.
     *
     * @property latLng The geographical coordinates (latitude and longitude) of the drag take-off point.
     *           This represents the initial location where the user started dragging.
     * @property onFeatureCollectionUpdated A callback function that will be executed when the
     *           FeatureCollection associated with the drag operation is
     *           updated. This function takes the updated FeatureCollection
     *           as its parameter, allowing for custom processing of the
     *           modified data.
     *
     */
    data class DragTakeOffPoint(
        val latLng: LatLng,
        val onFeatureCollectionUpdated: (FeatureCollection) -> Unit
    ) : TasksEvent()

    /**
     * Represents an event to update the take-off point for a specific task.
     *
     * This data class encapsulates the necessary information to update the take-off
     * location and associated settings for a task within a project. It includes
     * the task's identifier, project identifier, geographical coordinates (latitude and
     * longitude), and optional parameters for rotation angle, download behavior, and
     * whether the location is part of waypoints.
     *
     * @property task The task for which the take-off point is being updated.
     * @property takeOffPoint The latitude coordinate of the take-off point.
     * @property rotationAngle The rotation angle (in degrees) associated with the take-off point. Defaults to 0.
     * @property download A flag indicating whether associated data should be downloaded. Defaults to false.
     * @property isWayPoints A flag indicating whether this take-off point is part of a sequence of waypoints. Defaults to true.
     * @property rasterDemFilePath The path to the raster DEM file. Defaults to null.
     * @property offline A flag indicating whether the take-off point should be downloaded offline. Defaults to false.
     *
     */
    data class UpdateTakeOffPoint(
        val task: ProjectTask,
        val takeOffPoint: LatLng,
        val rotationAngle: Int? = null,
        val download: Boolean = false,
        val isWayPoints: Boolean = true,
        val rasterDemFilePath: String? = null,
        val offline: Boolean = false,
    ) : TasksEvent()

    /**
     * Represents a flight plan for a download task.
     *
     * This data class encapsulates the necessary information to define a download task,
     * specifically related to downloading data related to a flight plan. It includes:
     *
     * @property task The task for which the flight plan is being downloaded.
     * @property isWayPoints Indicates whether the task involves downloading waypoints.
     *                       Defaults to `true`, meaning the task is for waypoints by default.
     *                       Set to `false` if the task is for downloading other data
     *                       associated with the flight plan (e.g., flight path, telemetry).
     * @property rotationAngle The rotation angle (in degrees) associated with the flight plan.
     * @property takeOffPoint The geographical coordinates (latitude and longitude) of the take-off point.
     * @property rasterDemFilePath The path to the raster DEM file.
     * @property offline Indicates whether the flight plan should be downloaded offline.
     *
     * This class extends [TasksEvent], suggesting that it's part of a larger system
     * that manages events related to various tasks.
     */
    data class DownloadTaskFlightPlan(
        val task: ProjectTask,
        val isWayPoints: Boolean? = null,
        val rotationAngle: Int? = null,
        val takeOffPoint: LatLng? = null,
        val rasterDemFilePath: String? = null,
        val offline: Boolean = false,
    ) : TasksEvent()

    /**
     * Represents an event indicating that a FeatureCollection has been restored.
     *
     * This event is used to notify listeners that a previously saved or backed-up
     * FeatureCollection has been successfully restored and is now available.
     *
     * @property onRestored A callback function that is invoked when the FeatureCollection
     *                     is restored. It receives the restored FeatureCollection as a parameter.
     */
    data class RestoreFeatureCollection(val onRestored: (FeatureCollection) -> Unit) : TasksEvent()

    /**
     * Represents an event indicating that a task has been flagged as un-flyable.
     *
     * This event is typically triggered when a task is determined to be impossible
     * or unsafe to execute, for example, due to resource constraints, environmental
     * conditions, or unexpected dependencies.
     *
     * @property taskId The unique identifier of the task that is flagged as un-flyable.
     * @property projectId The unique identifier of the project to which the task belongs.
     * @property comment An optional comment providing further context or explanation
     *                   for why the task was flagged as un-flyable.
     */
    data class FlagTaskAsUnFlyable(
        val taskId: String,
        val projectId: String,
        val comment: String? = null
    ) : TasksEvent()

    /**
     * Represents the state of various reset actions within the application.
     *
     * This data class encapsulates the state of three distinct reset actions:
     * - Locking: Indicates whether a lock operation has been triggered and should be reset.
     * - Unlocking: Indicates whether an unlock operation has been triggered and should be reset.
     * - Task Detail: Indicates whether the task detail view's state should be reset.
     *
     * Each state is represented by a boolean flag, where `true` signifies that the corresponding
     * action's state should be reset, and `false` indicates that it should not.
     *
     * This class inherits from [TasksEvent], suggesting it is used as an event in a larger system
     * related to tasks.
     *
     * @property lockState `true` if the lock state should be reset, `false` otherwise. Defaults to `false`.
     * @property unlockState `true` if the unlock state should be reset, `false` otherwise. Defaults to `false`.
     * @property unFlyableState `true` if the task un-flyable request state should be reset, `false` otherwise. Defaults to `false`.
     * @property taskDetailState `true` if the task detail view's state should be reset, `false` otherwise. Defaults to `false`.
     * @property taskWayPointsOrWayLinesState `true` if the task waypoints state should be reset, `false` otherwise. Defaults to `false`.
     * @property taskFlightPlanDownloadState `true` if the task flight plan download state should be reset, `false` otherwise. Defaults to `false`.
     * @constructor Creates a [ResetState] instance with optional initial values for each state.
     */
    data class ResetState(
        val lockState: Boolean = false,
        val unlockState: Boolean = false,
        val unFlyableState: Boolean = false,
        val taskDetailState: Boolean = false,
        val taskWayPointsOrWayLinesState: Boolean = false,
        val taskFlightPlanDownloadState: Boolean = false,
    ) : TasksEvent()
}