package np.com.naxa.drone_tasking_manager.features.tasks.models

import com.google.gson.GsonBuilder
import np.com.naxa.drone_tasking_manager.features.projects.dto.projects_centroid.Centroid
import np.com.naxa.drone_tasking_manager.features.projects.models.ProjectGeometry
import np.com.naxa.drone_tasking_manager.features.projects.models.toFeatureJsonStr
import np.com.naxa.drone_tasking_manager.features.projects.utils.ProjectGeometryUtils

data class ProjectTask(
    val id: String? = null,
    val projectId: String? = null,
    val projectName: String? = null,
    val projectTaskIndex: Int? = null,
    val userId: String? = null,
    val userName: String? = null,
    val state: ProjectTaskState? = null,
    val frontOverlap: Int? = null,
    val totalAreaSqkm: Double? = null,
    val flightTimeMinutes: Double? = null,
    val flightDistanceKm: Double? = null,
    val totalImageUploaded: Int? = null,
    val imageCount: Int? = null,
    val assetsUrl: String? = null,
    val geometry: ProjectGeometry? = null,
    val centroid: Centroid? = null,
    val sideOverlap: Int? = null,
    val gsdCmPx: Int? = null,
    val gimbleAnglesDegrees: Int? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
) {
    fun toFeatureJsonStr(): String? {
        return geometry?.toFeatureJsonStr(
            properties = mapOf(
                "id" to id,
                "projectId" to projectId,
                "projectTaskIndex" to projectTaskIndex,
                "state" to state?.key?.uppercase(),
                "userId" to userId,
                "name" to userName,
                "imageCount" to imageCount,
                "assetsUrl" to assetsUrl,
                "totalAreaSqkm" to totalAreaSqkm,
                "flightTimeMinutes" to flightTimeMinutes,
                "flightDistanceKm" to flightDistanceKm,
                "totalImageUploaded" to totalImageUploaded,
                "color" to (state?.colorHex ?: "rgba(255, 255, 255, 0.3)"),
            )
        )
    }

    fun toLockedFeatureJsonStr(): String? {
        if (geometry?.properties?.bbox == null) return null
        val coordinates = try {
            ProjectGeometryUtils.calculateCentroidOfBBox(geometry.properties.bbox)
        } catch (e: Exception) {
            return null
        }

        val feature = mapOf(
            "type" to "Feature",
            "properties" to mapOf(
                "id" to id,
                "projectId" to projectId,
                "projectTaskIndex" to projectTaskIndex,
                "state" to state?.key?.uppercase(),
                "userId" to userId,
                "name" to userName,
                "imageCount" to imageCount,
                "assetsUrl" to assetsUrl,
                "totalAreaSqkm" to totalAreaSqkm,
                "flightTimeMinutes" to flightTimeMinutes,
                "flightDistanceKm" to flightDistanceKm,
                "totalImageUploaded" to totalImageUploaded,
                "icon" to if (state == ProjectTaskState.LockedForMapping) "locked-icon--" else null,
            ),
            "geometry" to mapOf(
                "type" to "Point",
                "coordinates" to coordinates
            )
        )

        return GsonBuilder().setPrettyPrinting().create().toJson(feature)
    }
}


