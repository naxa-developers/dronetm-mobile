package np.com.naxa.drone_tasking_manager.features.tasks.repositories

import kotlinx.coroutines.flow.Flow
import np.com.naxa.drone_tasking_manager.core.utils.Response
import np.com.naxa.drone_tasking_manager.features.tasks.models.TaskLockUnlockResponse

interface TasksRepository {

//    /**
//     * Fetches a task by its ID.
//     *
//     * This function retrieves a task from the API service using the provided ID.
//     * It supports forced refresh to bypass any potential caching mechanisms.
//     * The result is emitted as a Flow of Resources, which can be in the following states:
//     *   - Loading: Indicates that the request is in progress.
//     *   - Success: Indicates that the request was successful and contains the Project data.
//     *   - Error: Indicates that an error occurred during the request and contains an error message.
//     *
//     * @param id The ID of the task to fetch.
//     * @param forceRefresh If true, forces a refresh of the data, bypassing any caching.
//     *                     If false, the API service may return cached data if available.
//     * @return A Flow of Resources<ProjectTask>, emitting the current state of the request.
//     *         The Flow will emit at least one value (Loading) and then either Success or Error.
//     */
//    suspend fun fetchTaskById(
//        id: String,
//        forceRefresh: Boolean = true
//    ): Flow<Response<ProjectTask>>


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
    suspend fun unlockTask(taskId: String, projectId: String): Flow<Response<TaskLockUnlockResponse>>
}