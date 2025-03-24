package np.com.naxa.drone_tasking_manager.features.projects.dto.project

import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName

data class NoFlyZones(
    @SerializedName("type") var type: String? = null,
    @SerializedName("geometry") var geometry: Geometry? = Geometry(),
    @SerializedName("properties") var properties: Properties? = Properties(),
    @SerializedName("id") var id: String? = null
)

fun NoFlyZones.toFeatureJsonStr(properties: Map<*, *>? = null): String {
    val feature = mapOf(
        "type" to "Feature",
        "properties" to (properties ?: mapOf(
            "id" to this.properties?.id,
            "bbox" to this.properties?.bbox,
        )),
        "geometry" to mapOf(
            "type" to geometry?.type?.replace("ST_", "")?.trim(),
            "coordinates" to geometry?.coordinates
        )
    )

    return GsonBuilder().setPrettyPrinting().create().toJson(feature)
}

fun NoFlyZones.toGeoJsonStr(properties: Map<*, *>? = null): String {
    val geoJson = mapOf(
        "type" to "FeatureCollection",
        "features" to listOf(
            mapOf(
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
        )
    )

    return GsonBuilder().setPrettyPrinting().create().toJson(geoJson)
}