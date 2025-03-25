package np.com.naxa.drone_tasking_manager.features.tasks.usecases

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import np.com.naxa.drone_tasking_manager.features.projects.dto.project.NoFlyZones
import np.com.naxa.drone_tasking_manager.features.tasks.models.ProjectTask
import np.com.naxa.drone_tasking_manager.features.tasks.repositories.OfflineFlightPlanRepository
import np.com.naxa.drone_tasking_manager.features.tasks.repositories.TasksRepository
import np.com.naxa.drone_tasking_manager.features.tasks.utils.flight_plan_creator.Mode
import javax.inject.Inject


/**
 * ## DownloadTaskFlightPlanUseCase
 *
 * This use case is responsible for downloading the flight plan file associated with a specific task.
 * It interacts with the [TasksRepository] to retrieve the file.
 *
 * **Functionality:**
 * - Downloads a flight plan file for a given task and project.
 * - Supports specifying the download mode (e.g., "waypoints").
 * - Uses the [TasksRepository] to handle the underlying file retrieval.
 *
 */
@Module
@InstallIn(SingletonComponent::class)
class DownloadTaskFlightPlanUseCase @Inject constructor(
    private val tasksRepository: TasksRepository,
    private val offlineFlightPlanRepository: OfflineFlightPlanRepository
) {
    suspend operator fun invoke(
        taskId: String,
        projectId: String,
        mode: String = "waypoints",
        rotationAngle: Int
    ) = tasksRepository.downloadFlightPlanFile(
        taskId = taskId,
        projectId = projectId,
        mode = mode,
        rotationAngle = rotationAngle
    )

    suspend operator fun invoke(
        task: ProjectTask,
        noFlyZones: NoFlyZones? = null,
        rotationAngle: Int = 0,
        takeOffPoint: List<Double>? = null,
        mode: Mode = Mode.WayPoints,
    ) = offlineFlightPlanRepository.generateFlightPlanFile(
        task = task,
        noFlyZones = noFlyZones,
        rotationAngle = rotationAngle,
        takeOffPoint = takeOffPoint,
        mode = mode,
    )

}