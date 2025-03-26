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
 * [UpdateTakeOffPointUseCase]
 *
 * This class provides use cases for updating the takeoff point of a task, either online or offline.
 * It handles the logic for updating the takeoff point through either the [TasksRepository] for online
 * tasks or the [OfflineFlightPlanRepository] for offline tasks.
 *
 * It provides two `invoke` methods, each tailored to different scenarios:
 *   1. Updating a takeoff point for an online task using [TasksRepository].
 *   2. Updating a takeoff point for an offline task using [OfflineFlightPlanRepository].
 *
 * @property tasksRepository Repository for managing online tasks and their data.
 * @property offlineFlightPlanRepository Repository for managing offline flight plans and their data.
 */
@Module
@InstallIn(SingletonComponent::class)
class UpdateTakeOffPointUseCase @Inject constructor(
    private val tasksRepository: TasksRepository,
    private val offlineFlightPlanRepository: OfflineFlightPlanRepository
) {
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

    suspend operator fun invoke(
        task: ProjectTask,
        noFlyZones: NoFlyZones? = null,
        rotationAngle: Int = 0,
        takeOffPoint: List<Double>,
        rasterDemFilePath: String? = null,
        mode: Mode = Mode.WayPoints,
    ) = offlineFlightPlanRepository.taskWayPointsOrLines(
        task = task,
        noFlyZones = noFlyZones,
        rotationAngle = rotationAngle,
        takeOffPoint = takeOffPoint,
        rasterDemFilePath = rasterDemFilePath,
        mode = mode,
    )
}