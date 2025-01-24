package np.com.naxa.drone_tasking_manager.features.projects.dto.projects_centroid

import com.google.gson.annotations.SerializedName


data class Centroid(
    @SerializedName("type") var type: String? = null,
    @SerializedName("coordinates") var coordinates: ArrayList<Double> = arrayListOf()
)

fun Centroid.toJson(): Map<String, Any?> {
    val centroid = mapOf(
        "type" to type,
        "coordinates" to coordinates
    )
    return centroid
}