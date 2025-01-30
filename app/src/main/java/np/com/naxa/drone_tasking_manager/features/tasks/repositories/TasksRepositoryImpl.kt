package np.com.naxa.drone_tasking_manager.features.tasks.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import np.com.naxa.drone_tasking_manager.core.services.ApiService
import np.com.naxa.drone_tasking_manager.core.utils.Response
import np.com.naxa.drone_tasking_manager.features.tasks.dto.LockOrUnlockEventRequestBody
import np.com.naxa.drone_tasking_manager.features.tasks.mapper.toProjectTask
import np.com.naxa.drone_tasking_manager.features.tasks.mapper.toTaskLockUnlockResponse
import np.com.naxa.drone_tasking_manager.features.tasks.models.ProjectTask
import np.com.naxa.drone_tasking_manager.features.tasks.models.TaskLockUnlockResponse
import np.com.naxa.drone_tasking_manager.utils.DateUtils
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TasksRepositoryImpl @Inject constructor(private val apiService: ApiService) :
    TasksRepository {
    override suspend fun fetchTaskById(
        id: String,
        forceRefresh: Boolean
    ): Flow<Response<ProjectTask>> {
        return flow {
            emit(Response.Loading())

            try {
                val response = apiService.fetchTaskById(
                    id = id,
                    forceRefresh = forceRefresh
                )

                emit(Response.Success(response.toProjectTask()))

            } catch (e: Exception) {
                emit(Response.Error(e.message ?: "Error fetching task"))
            }
        }
    }


    override suspend fun lockTask(
        taskId: String,
        projectId: String
    ): Flow<Response<TaskLockUnlockResponse>> {
        return flow {
            emit(Response.Loading())

            try {
                val response = apiService.lockOrUnlockTask(
                    projectId = projectId,
                    taskId = taskId,
                    body = LockOrUnlockEventRequestBody(
                        event = "request",
                        updatedAt = DateUtils.currentDateAsStr(),
                    ),
                )

                emit(Response.Success(response.toTaskLockUnlockResponse()))

            } catch (e: Exception) {
                emit(Response.Error(e.cause?.message ?: "Error locking task $taskId"))
            }
        }
    }

    override suspend fun unlockTask(
        taskId: String,
        projectId: String
    ): Flow<Response<TaskLockUnlockResponse>> {
        return flow {
            emit(Response.Loading())

            try {
                val response = apiService.lockOrUnlockTask(
                    projectId = projectId,
                    taskId = taskId,
                    body = LockOrUnlockEventRequestBody(
                        event = "unlock",
                        updatedAt = DateUtils.currentDateAsStr(),
                    ),
                )

                emit(Response.Success(response.toTaskLockUnlockResponse()))

            } catch (e: Exception) {
                emit(Response.Error(e.cause?.message ?: "Error unlocking task $taskId"))
            }
        }
    }


}