package np.com.naxa.drone_tasking_manager.features.tasks.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import np.com.naxa.drone_tasking_manager.core.services.ApiService
import np.com.naxa.drone_tasking_manager.core.utils.Response
import np.com.naxa.drone_tasking_manager.features.tasks.dto.TaskEventRequestBody
import np.com.naxa.drone_tasking_manager.features.tasks.mapper.toProjectTask
import np.com.naxa.drone_tasking_manager.features.tasks.mapper.toTaskLockUnlockResponse
import np.com.naxa.drone_tasking_manager.features.tasks.mapper.toTaskUnFlyableResponse
import np.com.naxa.drone_tasking_manager.features.tasks.models.ProjectTask
import np.com.naxa.drone_tasking_manager.features.tasks.models.TaskLockUnlockResponse
import np.com.naxa.drone_tasking_manager.features.tasks.models.TaskUnFlyableResponse
import np.com.naxa.drone_tasking_manager.utils.DateUtils
import org.maplibre.geojson.FeatureCollection
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

                emit(
                    Response.Success(
                        response.toProjectTask().copy(
                            id = id,
                        )
                    )
                )

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
                    body = TaskEventRequestBody(
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
                    body = TaskEventRequestBody(
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

    override suspend fun taskUnFlyable(
        taskId: String,
        projectId: String,
        comment: String?
    ): Flow<Response<TaskUnFlyableResponse>> {
        return flow {
            emit(Response.Loading())

            try {
                val response = apiService.unFlyableTask(
                    projectId = projectId,
                    taskId = taskId,
                    body = TaskEventRequestBody(
                        event = "comment",
                        comment = comment,
                        updatedAt = DateUtils.currentDateAsStr(),
                    ),
                )

                emit(Response.Success(response.toTaskUnFlyableResponse()))

            } catch (e: Exception) {
                emit(Response.Error(e.cause?.message ?: "Error requesting task $taskId un-flyable"))
            }
        }
    }

    override suspend fun taskWayPoints(
        taskId: String,
        projectId: String,
        rotationAngle: Int,
        download: Boolean,
        forceRefresh: Boolean
    ): Flow<Response<FeatureCollection>> {
        return flow {
            emit(Response.Loading())
            try {
                val response = apiService.taskWayPointsOrWayLines(
                    projectId = projectId,
                    taskId = taskId,
                    rotationAngle = rotationAngle,
                    download = download,
                    mode = "waypoints",
                    forceRefresh = forceRefresh
                )

                emit(Response.Success(response))

            } catch (e: Exception) {
                emit(Response.Error(e.cause?.message ?: "Error fetching task: $taskId waypoints"))
            }
        }
    }

    override suspend fun taskWayLines(
        taskId: String,
        projectId: String,
        rotationAngle: Int,
        download: Boolean,
        forceRefresh: Boolean
    ): Flow<Response<FeatureCollection>> {
        return flow {
            emit(Response.Loading())

            try {
                val response = apiService.taskWayPointsOrWayLines(
                    projectId = projectId,
                    taskId = taskId,
                    rotationAngle = rotationAngle,
                    download = download,
                    mode = "waylines",
                    forceRefresh = forceRefresh
                )

                emit(Response.Success(response))

            } catch (e: Exception) {
                emit(Response.Error(e.cause?.message ?: "Error fetching task: $taskId waylines"))
            }
        }
    }


}