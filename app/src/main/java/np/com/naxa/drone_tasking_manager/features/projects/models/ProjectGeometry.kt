package np.com.naxa.drone_tasking_manager.features.projects.models

import com.google.gson.GsonBuilder

data class ProjectGeometry(
    val type: String? = null,
    val geometry: Geometry? = null,
    val properties: Properties? = null,
    val id: String? = null
) {
    data class Geometry(
        val type: String? = null,
        val coordinates: ArrayList<ArrayList<ArrayList<Double>>> = arrayListOf()
    )

    data class Properties(
        val id: String? = null,
        val bbox: ArrayList<Double> = arrayListOf()
    )
}

fun ProjectGeometry.toFeatureJsonStr(properties: Map<*, *>? = null): String {
    val feature = mapOf(
        "type" to "Feature",
        "properties" to (properties ?: mapOf(
            "id" to this.properties?.id,
            "bbox" to this.properties?.bbox,
            "project_boundary" to true
        )),
        "geometry" to mapOf(
            "type" to geometry?.type?.replace("ST_", "")?.trim(),
            "coordinates" to geometry?.coordinates
        )
    )

    return GsonBuilder().setPrettyPrinting().create().toJson(feature)
}

fun ProjectGeometry.Geometry.toFeatureJson(): String {
    val feature = mapOf(
        "type" to "Feature",
        "properties" to mapOf(
            "id" to null
        ),
        "geometry" to mapOf(
            "type" to type,
            "coordinates" to coordinates
        )
    )

    return GsonBuilder().setPrettyPrinting().create().toJson(feature)
}