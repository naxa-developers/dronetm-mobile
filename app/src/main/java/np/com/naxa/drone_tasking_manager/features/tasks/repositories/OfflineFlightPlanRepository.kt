package np.com.naxa.drone_tasking_manager.features.tasks.repositories

import kotlinx.coroutines.flow.Flow
import np.com.naxa.drone_tasking_manager.core.utils.DownloadResponse
import np.com.naxa.drone_tasking_manager.core.utils.Response
import np.com.naxa.drone_tasking_manager.features.projects.dto.project.NoFlyZones
import np.com.naxa.drone_tasking_manager.features.tasks.models.ProjectTask
import np.com.naxa.drone_tasking_manager.features.tasks.utils.flight_plan_creator.Mode
import org.maplibre.geojson.FeatureCollection

interface OfflineFlightPlanRepository {
    /**
     * Retrieves the waypoints or way lines associated with a specific task.
     *
     * This function fetches the waypoints related to a given task ID within a specific project.
     * It allows for optional rotation adjustment and force-refreshing of the data.
     *
     * @param task The task for which to retrieve the waypoints.
     * @param noFlyZones The no-fly zones associated with the task, if any.
     * @param rotationAngle An optional integer representing a rotation adjustment to be applied to the waypoints. Defaults to 0 (no rotation).
     * @param takeOffPoint The coordinates of the take-off point.
     * @param mode The mode of the flight plan. Defaults to [Mode.WayPoints].
     * @return A [Flow] emitting [Response] objects containing a [FeatureCollection].
     *         - On success, the [Response] will contain the waypoints data in the [FeatureCollection] within the body.
     *         - On failure, the [Response] will have an error code and potentially an error body.
     *         - The [Flow] allows for asynchronous handling of the response data stream.
     *
     */
    suspend fun taskWayPointsOrLines(
        task: ProjectTask,
        noFlyZones: NoFlyZones? = null,
        rotationAngle: Int = 0,
        takeOffPoint: List<Double>? = null,
        mode: Mode = Mode.WayPoints
    ): Flow<Response<FeatureCollection>>

    /**
     * Downloads a flight plan file associated with a specific task and project.
     *
     * This function retrieves the flight plan data based on the provided task ID and project ID.
     * It offers options to customize the download, such as specifying a rotation angle and
     * controlling whether to download the file or just retrieve the data. The user can also specify
     * the mode of the flight plan (e.g., "waypoints").
     *
     * @param task The task for which to retrieve the flight plan.
     * @param noFlyZones The no-fly zones associated with the task, if any.
     * @param rotationAngle The rotation angle of the take-off point in degrees. Defaults to 0.
     * @param takeOffPoint The coordinates of the take-off point.
     * @param mode The mode of the flight plan. Defaults to [Mode.WayPoints].
     *
     *
     */
    suspend fun generateFlightPlanFile(
        task: ProjectTask,
        noFlyZones: NoFlyZones? = null,
        rotationAngle: Int = 0,
        takeOffPoint: List<Double>? = null,
        mode: Mode = Mode.WayPoints,
    ): Flow<DownloadResponse>
}