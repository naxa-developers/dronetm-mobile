package np.com.naxa.drone_tasking_manager.features.tasks.repositories

import kotlinx.coroutines.flow.Flow
import np.com.naxa.drone_tasking_manager.core.utils.DownloadResponse
import np.com.naxa.drone_tasking_manager.core.utils.Response
import np.com.naxa.drone_tasking_manager.features.projects.dto.project.NoFlyZones
import np.com.naxa.drone_tasking_manager.features.projects.models.ProjectGeometry
import np.com.naxa.drone_tasking_manager.features.tasks.utils.flight_plan_creator.Mode
import org.maplibre.geojson.FeatureCollection

interface OfflineFlightPlanRepository {
    /**
     * Retrieves the waypoints or way lines associated with a specific task.
     *
     * This function fetches the waypoints related to a given task ID within a specific project.
     * It allows for optional rotation adjustment and force-refreshing of the data.
     *
     * @param taskId The unique identifier of the task. This is a mandatory parameter.
     * @param taskGeometry The geometry of the task. This is a mandatory parameter.
     * @param noFlyZones The no-fly zones associated with the task, if any.
     * @param rotationAngle An optional integer representing a rotation adjustment to be applied to the waypoints. Defaults to 0 (no rotation).
     * @param mode The mode of the flight plan. Defaults to [Mode.WayPoints].
     * @return A [Flow] emitting [Response] objects containing a [FeatureCollection].
     *         - On success, the [Response] will contain the waypoints data in the [FeatureCollection] within the body.
     *         - On failure, the [Response] will have an error code and potentially an error body.
     *         - The [Flow] allows for asynchronous handling of the response data stream.
     *
     */
    suspend fun taskWayPointsOrLines(
        taskId: String,
        taskGeometry: ProjectGeometry,
        noFlyZones: NoFlyZones? = null,
        rotationAngle: Int = 0,
        mode: Mode = Mode.WayPoints
    ): Flow<Response<FeatureCollection>>


    /**
     * Updates the take-off point for a given task and project.
     *
     * This function updates the take-off point's location (latitude, longitude) and optionally its
     * rotation angle. It also allows specifying whether to download data related to the updated point and whether to force a refresh.
     *
     * @param taskId The ID of the task for which to update the take-off point.
     * @param taskGeometry The geometry of the task. This is a mandatory parameter.
     * @param noFlyZones The no-fly zones associated with the task, if any.
     * @param rotationAngle The rotation angle of the take-off point in degrees. Defaults to 0.
     * @param latitude The latitude of the new take-off point location.
     * @param longitude The longitude of the new take-off point location.
     * @param mode The mode of the flight plan. Defaults to [Mode.WayPoints].
     * @return A Flow emitting a Response object containing a FeatureCollection.
     *         The Response will represent the result of the update operation, and the FeatureCollection may contain relevant data
     *         related to the updated take-off point.
     *         The Flow can emit multiple Responses, representing different states of the update and data retrieval process.
     *         It can contain errors if any network or other issues happen.
     * @throws Exception if there is an error during the update process.
     */
    suspend fun updateTakeOffPoint(
        taskId: String,
        taskGeometry: ProjectGeometry,
        noFlyZones: NoFlyZones? = null,
        rotationAngle: Int = 0,
        latitude: Double,
        longitude: Double,
        mode: Mode = Mode.WayPoints,
    ): Flow<Response<FeatureCollection>>

    /**
     * Downloads a flight plan file associated with a specific task and project.
     *
     * This function retrieves the flight plan data based on the provided task ID and project ID.
     * It offers options to customize the download, such as specifying a rotation angle and
     * controlling whether to download the file or just retrieve the data. The user can also specify
     * the mode of the flight plan (e.g., "waypoints").
     *
     * @param taskId The ID of the task for which to update the take-off point.
     * @param taskGeometry The geometry of the task. This is a mandatory parameter.
     * @param noFlyZones The no-fly zones associated with the task, if any.
     * @param rotationAngle The rotation angle of the take-off point in degrees. Defaults to 0.
     * @param latitude The latitude of the new take-off point location.
     * @param longitude The longitude of the new take-off point location.
     * @param mode The mode of the flight plan. Defaults to [Mode.WayPoints].
     *
     *
     */
    suspend fun downloadFlightPlanFile(
        taskId: String,
        taskGeometry: ProjectGeometry,
        noFlyZones: NoFlyZones? = null,
        rotationAngle: Int = 0,
        latitude: Double,
        longitude: Double,
        mode: Mode = Mode.WayPoints,
    ): Flow<DownloadResponse>
}