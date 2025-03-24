package np.com.naxa.drone_tasking_manager.features.tasks.repositories

import android.content.Context
import com.fasterxml.jackson.databind.util.JSONPObject
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import np.com.naxa.drone_tasking_manager.core.utils.DownloadResponse
import np.com.naxa.drone_tasking_manager.core.utils.Response
import np.com.naxa.drone_tasking_manager.core.utils.responsevalidator.ErrorResponse
import np.com.naxa.drone_tasking_manager.core.utils.responsevalidator.getErrorMessage
import np.com.naxa.drone_tasking_manager.features.projects.dto.project.NoFlyZones
import np.com.naxa.drone_tasking_manager.features.projects.dto.project.toGeoJsonStr
import np.com.naxa.drone_tasking_manager.features.projects.models.ProjectGeometry
import np.com.naxa.drone_tasking_manager.features.projects.models.toFeatureJsonStr
import np.com.naxa.drone_tasking_manager.features.projects.models.toGeoJsonStr
import np.com.naxa.drone_tasking_manager.features.tasks.utils.flight_plan_creator.Mode
import np.com.naxa.drone_tasking_manager.features.tasks.utils.flight_plan_creator.Parameters
import np.com.naxa.drone_tasking_manager.features.tasks.utils.flight_plan_creator.WaypointsOrLines
import org.json.JSONObject
import org.maplibre.geojson.FeatureCollection
import retrofit2.HttpException
import javax.inject.Inject

class OfflineFlightPlanRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) :
    OfflineFlightPlanRepository {

    override suspend fun taskWayPointsOrLines(
        taskId: String,
        taskGeometry: ProjectGeometry,
        noFlyZones: NoFlyZones?,
        rotationAngle: Int,
        mode: Mode
    ): Flow<Response<FeatureCollection>> {
        return flow {
            emit(Response.Loading())
            try {
                val parameters = Parameters.calculate(
                    forwardOverlap = 75.0,
                    sideOverlap = 70.0,
                    agl = 118.8,
                    gsd = 4.0,
                    imageInterval = 2
                )

                val response = WaypointsOrLines.create(
                    projectArea = taskGeometry.toGeoJsonStr(),
                    noFlyZones = noFlyZones?.toGeoJsonStr(),
                    parameters = parameters,
                    rotationAngle = rotationAngle.toDouble(),
                    mode = mode,
                )

                emit(Response.Success(FeatureCollection.fromJson(response)))

            } catch (e: Exception) {
                emit(
                    Response.Error(
                        e.message ?: e.cause?.message ?: "Error generating waypoints."
                    )
                )
            }
        }
    }


    override suspend fun updateTakeOffPoint(
        taskId: String,
        taskGeometry: ProjectGeometry,
        noFlyZones: NoFlyZones?,
        rotationAngle: Int,
        latitude: Double,
        longitude: Double,
        mode: Mode,
    ): Flow<Response<FeatureCollection>> {
        return flow {
            emit(Response.Loading())
            try {
                val parameters = Parameters.calculate(
                    forwardOverlap = 75.0,
                    sideOverlap = 70.0,
                    agl = 118.8,
                    gsd = 4.0,
                    imageInterval = 2
                )

                val response = WaypointsOrLines.create(
                    projectArea = taskGeometry.toGeoJsonStr(),
                    noFlyZones = noFlyZones?.toGeoJsonStr(),
                    parameters = parameters,
                    rotationAngle = rotationAngle.toDouble(),
                    takeOffPoint = listOf(
                        longitude,
                        latitude,
                    ),
                    mode = mode,
                )

                emit(Response.Success(FeatureCollection.fromJson(response)))

            } catch (e: Exception) {
                emit(
                    Response.Error(
                        e.message ?: e.cause?.message ?: "Error generating waypoints."
                    )
                )
            }
        }
    }


    override suspend fun downloadFlightPlanFile(
        taskId: String,
        taskGeometry: ProjectGeometry,
        noFlyZones: NoFlyZones?,
        rotationAngle: Int,
        latitude: Double,
        longitude: Double,
        mode: Mode,
    ): Flow<DownloadResponse> {
        throw IllegalArgumentException()
    }
}