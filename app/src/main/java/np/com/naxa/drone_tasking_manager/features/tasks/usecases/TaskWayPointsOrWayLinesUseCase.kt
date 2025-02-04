package np.com.naxa.drone_tasking_manager.features.tasks.usecases

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import np.com.naxa.drone_tasking_manager.features.tasks.repositories.TasksRepository
import javax.inject.Inject

/**
 * This UseCase class is responsible for fetching and managing task waypoints or way lines.
 * It interacts with the [TasksRepository] to retrieve waypoint data based on
 * a given task ID and project ID. It also handles optional parameters like
 * rotation angle, download status, and force refresh.
 *
 * This class is designed to be used as a singleton within the application,
 * providing a central point of access for task waypoint data.
 *
 * @property tasksRepository The repository used to fetch task waypoint data.
 */
@Module
@InstallIn(SingletonComponent::class)
class TaskWayPointsOrWayLinesUseCase @Inject constructor(private val tasksRepository: TasksRepository) {
    suspend operator fun invoke(
        taskId: String,
        projectId: String,
        rotationAngle: Int = 0,
        download: Boolean = false,
        isWayPoints: Boolean,
        forceRefresh: Boolean = true
    ) = if (isWayPoints)
        tasksRepository.taskWayPoints(taskId, projectId, rotationAngle, download, forceRefresh) else
        tasksRepository.taskWayLines(taskId, projectId, rotationAngle, download, forceRefresh)

}