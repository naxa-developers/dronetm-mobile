package np.com.naxa.drone_tasking_manager.features.tasks.usecases

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import np.com.naxa.drone_tasking_manager.features.tasks.repositories.TasksRepository
import javax.inject.Inject


/**
 * This use case class is responsible for updating the take-off point of a task.
 *
 * It interacts with the [TasksRepository] to persist the updated take-off point information.
 * The take-off point is defined by its latitude, longitude, and optionally a rotation angle.
 * It also allows options like forcing a refresh of the data, downloading related resources, and changing mode.
 *
 * @property tasksRepository The repository responsible for interacting with the data layer
 *                           to update task information. Injected by Hilt.
 */
@Module
@InstallIn(SingletonComponent::class)
class UpdateTakeOffPointUseCase @Inject constructor(private val tasksRepository: TasksRepository) {
    suspend operator fun invoke(
        taskId: String,
        projectId: String,
        rotationAngle: Int = 0,
        download: Boolean = false,
        mode: String = "waypoints",
        forceRefresh: Boolean = true,
        latitude: Double,
        longitude: Double
    ) = tasksRepository.updateTakeOffPoint(
        taskId = taskId,
        projectId = projectId,
        rotationAngle = rotationAngle,
        download = download,
        mode = mode,
        forceRefresh = forceRefresh,
        latitude = latitude,
        longitude = longitude
    )

}