package np.com.naxa.drone_tasking_manager.features.projects.models

import androidx.compose.ui.graphics.Color
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import np.com.naxa.drone_tasking_manager.features.projects.utils.ProjectGeometryUtils

data class ProjectTask(
    val id: String? = null,
    val projectId: String? = null,
    val projectTaskIndex: Int? = null,
    val geometry: ProjectGeometry? = null,
    val state: ProjectTaskState? = null,
    val userId: String? = null,
    val name: String? = null,
    val imageCount: String? = null,
    val assetsUrl: String? = null,
    val totalAreaSqkm: Double? = null,
    val flightTimeMinutes: Double? = null,
    val flightDistanceKm: Double? = null,
    val totalImageUploaded: String? = null
)

fun ProjectTask.toFeatureJsonStr(): String? {
    return geometry?.toFeatureJsonStr(
        properties = mapOf(
            "id" to id,
            "projectId" to projectId,
            "projectTaskIndex" to projectTaskIndex,
            "state" to state?.key?.uppercase(),
            "userId" to userId,
            "name" to name,
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

fun ProjectTask.toLockedFeatureJsonStr(): String? {
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
            "name" to name,
            "icon" to if (state == ProjectTaskState.LockedForMapping) "locked-icon--" else null,
        ),
        "geometry" to mapOf(
            "type" to "Point",
            "coordinates" to coordinates
        )
    )

    return GsonBuilder().setPrettyPrinting().create().toJson(feature)
}

enum class ProjectTaskState(
    val colorHex: String,
    val color: Color,
    val label: String,
    val key: String
) {
    LockedForMapping("#98BBC8", Color(0xFF98BBC8), "Locked Tasks", "locked_for_mapping"),
    UnlockedToMap(
        "rgba(255, 255, 255, 0.3)",
        Color.White.copy(alpha = 0.3f),
        "Remaining Tasks",
        "unlocked_to_map"
    ),
    ImageProcessingStarted(
        "#9C77B2",
        Color(0xFF9C77B2),
        "Image Processing Started",
        "image_processing_started"
    ),
    ImageProcessingFinished(
        "#ACD2C4",
        Color(0xFFACD2C4),
        "Finished Tasks",
        "image_processing_finished"
    ),
    ImageProcessingFailed(
        "#D73F3F",
        Color(0xFFD73F3F),
        "Image Processing Failed",
        "image_processing_failed"
    );

    companion object {
        fun fromString(state: String?): ProjectTaskState? {
            return when (state?.trim()?.lowercase()) {
                LockedForMapping.key -> LockedForMapping
                UnlockedToMap.key -> UnlockedToMap
                ImageProcessingStarted.key -> ImageProcessingStarted
                ImageProcessingFinished.key -> ImageProcessingFinished
                ImageProcessingFailed.key -> ImageProcessingFailed
                else -> null
            }

        }
    }
}