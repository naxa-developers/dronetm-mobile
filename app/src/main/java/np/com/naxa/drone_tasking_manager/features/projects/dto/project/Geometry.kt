package np.com.naxa.drone_tasking_manager.features.projects.dto.project

import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName

data class Geometry(
    @SerializedName("type") var type: String? = null,
    @SerializedName("coordinates") var coordinates: ArrayList<ArrayList<ArrayList<Double>>> = arrayListOf()
)

fun Geometry.toFeatureJson(): String {
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