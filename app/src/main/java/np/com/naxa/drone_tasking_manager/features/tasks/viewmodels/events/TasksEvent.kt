package np.com.naxa.drone_tasking_manager.features.tasks.viewmodels.events


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
     * @property taskId The unique identifier of the task associated with the waypoints/waylines.
     * @property projectId The unique identifier of the project associated with the task.
     * @property rotationAngle The rotation angle (in degrees) to be applied to the waypoints/waylines. Defaults to 0.
     * @property download Indicates whether the fetched data should be downloaded. Defaults to false.
     * @property isWayPoints A flag indicating whether waypoints (true) or waylines (false) are being requested.
     * @property forceRefresh If true, forces a refresh of the data, bypassing any cached data. Defaults to false.
     */
    data class FetchWayPointsOrWayLines(
        val taskId: String,
        val projectId: String,
        val rotationAngle: Int = 0,
        val download: Boolean = false,
        val isWayPoints: Boolean,
        val forceRefresh: Boolean = true
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
     * @property taskDetailState `true` if the task detail view's state should be reset, `false` otherwise. Defaults to `false`.
     * @constructor Creates a [ResetState] instance with optional initial values for each state.
     */
    data class ResetState(
        val lockState: Boolean = false,
        val unlockState: Boolean = false,
        val taskDetailState: Boolean = false,
        val taskWayPointsOrWayLinesState: Boolean = false
    ) : TasksEvent()
}