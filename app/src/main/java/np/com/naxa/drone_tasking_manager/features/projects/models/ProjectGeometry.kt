package np.com.naxa.drone_tasking_manager.features.projects.models

import com.google.gson.GsonBuilder
import np.com.naxa.drone_tasking_manager.features.projects.dto.project.Geometry
import np.com.naxa.drone_tasking_manager.features.projects.dto.project.Properties

data class ProjectGeometry(
    val type: String? = null,
    val geometry: Geometry? = null,
    val properties: Properties? = null,
    val id: String? = null
)

fun ProjectGeometry.toFeatureJsonStr(properties: Map<*, *>? = null): String {
    val feature = mapOf(
        "type" to "Feature",
        "properties" to (properties ?: mapOf(
            "id" to this.properties?.id,
            "bbox" to this.properties?.bbox,
            "project_boundary" to true
        )),
        "geometry" to mapOf(
            "type" to geometry?.type,
            "coordinates" to geometry?.coordinates
        )
    )

    return GsonBuilder().setPrettyPrinting().create().toJson(feature)
}