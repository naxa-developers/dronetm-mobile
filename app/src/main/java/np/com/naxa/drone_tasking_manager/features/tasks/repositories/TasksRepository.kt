package np.com.naxa.drone_tasking_manager.features.tasks.repositories

import com.google.gson.JsonObject
import kotlinx.coroutines.flow.Flow
import np.com.naxa.drone_tasking_manager.core.utils.Response
import np.com.naxa.drone_tasking_manager.features.tasks.models.ProjectTask
import np.com.naxa.drone_tasking_manager.features.tasks.models.TaskLockUnlockResponse
import org.maplibre.geojson.FeatureCollection

interface TasksRepository {

    /**
     * Fetches a task by its ID.
     *
     * This function retrieves a task from the API service using the provided ID.
     * It supports forced refresh to bypass any potential caching mechanisms.
     * The result is emitted as a Flow of Resources, which can be in the following states:
     *   - Loading: Indicates that the request is in progress.
     *   - Success: Indicates that the request was successful and contains the Project data.
     *   - Error: Indicates that an error occurred during the request and contains an error message.
     *
     * @param id The ID of the task to fetch.
     * @param forceRefresh If true, forces a refresh of the data, bypassing any caching.
     *                     If false, the API service may return cached data if available.
     * @return A Flow of Resources<ProjectTask>, emitting the current state of the request.
     *         The Flow will emit at least one value (Loading) and then either Success or Error.
     */
    suspend fun fetchTaskById(
        id: String,
        forceRefresh: Boolean = true
    ): Flow<Response<ProjectTask>>


    /**
     * Locks a task.
     *
     * This function initiates a request to lock a task on the server.
     * When a task is locked, it typically means that it's being worked on and should not be modified by other users.
     *
     * The function returns a `Flow` that emits `Response` objects.
     * Each `Response` encapsulates the HTTP status and the body of the server's response.
     * The body will be a `TaskLockUnlockResponse` object that contains information about the lock operation's success.
     *
     * This is a suspending function, meaning it must be called from a coroutine or another suspending function.
     *
     * @return A `Flow` emitting `Response<TaskLockUnlockResponse>` objects. Each emitted `Response` represents a server response.
     *
     */
    suspend fun lockTask(taskId: String, projectId: String): Flow<Response<TaskLockUnlockResponse>>

    /**
     * Unlocks a task that was previously locked.
     *
     * This function sends a request to the server to unlock a previously locked task.
     * The specific task to unlock is determined by the server-side logic, potentially
     * based on the user's current session or other contextual information.
     *
     * The function operates as a suspending function, meaning it can be paused and resumed
     * to avoid blocking the main thread during network operations.
     *
     * The function returns a [Flow] that emits [Response] objects. This allows for
     * handling of both success and failure scenarios, as well as potential updates
     * or intermediate states if the unlocking process were to support them.
     *
     * @return A [Flow] emitting [Response] objects containing a [TaskLockUnlockResponse] on success or error information on failure.
     */
    suspend fun unlockTask(
        taskId: String,
        projectId: String
    ): Flow<Response<TaskLockUnlockResponse>>

    /**
     * Retrieves the waypoints associated with a specific task.
     *
     * This function fetches the waypoints related to a given task ID within a specific project.
     * It allows for optional rotation adjustment and force-refreshing of the data.
     *
     * @param taskId The unique identifier of the task. This is a mandatory parameter.
     * @param projectId The unique identifier of the project to which the task belongs. This is a mandatory parameter.
     * @param rotationAngle An optional integer representing a rotation adjustment to be applied to the waypoints. Defaults to 0 (no rotation).
     * @param download Either download or not
     * @param forceRefresh A boolean flag indicating whether to force a refresh of the data from the server, bypassing any cached data. Defaults to `true`.
     * @return A [Flow] emitting [Response] objects containing a [FeatureCollection].
     *         - On success, the [Response] will contain the waypoints data in the [FeatureCollection] within the body.
     *         - On failure, the [Response] will have an error code and potentially an error body.
     *         - The [Flow] allows for asynchronous handling of the response data stream.
     *
     */
    suspend fun taskWayPoints(
        taskId: String,
        projectId: String,
        rotationAngle: Int = 0,
        download: Boolean = false,
        forceRefresh: Boolean = true
    ): Flow<Response<FeatureCollection>>

    /**
     * Retrieves the way lines associated with a specific task.
     *
     * This function fetches the way lines for a given task within a project. Way lines often
     * represent the path or route that the task involves. It supports rotation and forced refresh
     * to ensure the data is up-to-date.
     *
     * @param taskId The unique identifier of the task for which to retrieve way lines.
     *               Must not be null or empty.
     * @param projectId The unique identifier of the project to which the task belongs.
     *                  Must not be null or empty.
     * @param rotationAngle An optional integer representing the rotation applied to the way lines'
     *                 coordinates. Defaults to 0 (no rotation). Common values are 0, 90, 180, 270.
     * @param download Either download or not
     * @param forceRefresh A boolean indicating whether to force a refresh of the data from the
     *                     source, bypassing any cached data. Defaults to true. If set to false, the
     *                     function may return cached data if available.
     * @return A Flow emitting a Response object containing a FeatureCollection representing the way lines.
     *
     */
    suspend fun taskWayLines(
        taskId: String,
        projectId: String,
        rotationAngle: Int = 0,
        download: Boolean = false,
        forceRefresh: Boolean = true
    ): Flow<Response<FeatureCollection>>
}