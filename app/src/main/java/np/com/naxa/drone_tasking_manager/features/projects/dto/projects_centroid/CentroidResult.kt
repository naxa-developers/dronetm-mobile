package np.com.naxa.drone_tasking_manager.features.projects.dto.projects_centroid

import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName


data class CentroidResult(
    @SerializedName("id") var id: String? = null,
    @SerializedName("slug") var slug: String? = null,
    @SerializedName("name") var name: String? = null,
    @SerializedName("centroid") var centroid: Centroid? = Centroid(),
    @SerializedName("total_task_count") var totalTaskCount: Int? = null,
    @SerializedName("ongoing_task_count") var ongoingTaskCount: Int? = null,
    @SerializedName("completed_task_count") var completedTaskCount: Int? = null,
    @SerializedName("status") var status: String? = null

)

fun CentroidResult.toFeatureJsonStr(): String {
    val feature = mapOf(
        "type" to "Feature",
        "properties" to mapOf(
            "id" to id,
            "slug" to slug,
            "name" to name,
            "total_task_count" to totalTaskCount,
            "ongoing_task_count" to ongoingTaskCount,
            "completed_task_count" to completedTaskCount,
            "status" to status,
            "color" to if (status?.trim()
                    ?.lowercase() == "ongoing"
            ) "#417EC9" else if (status?.trim()
                    ?.lowercase() == "not-started"
            ) "#808080" else if (status?.trim()
                    ?.lowercase() == "completed"
            ) "#028A0F" else "#808080"
        ),
        "geometry" to centroid?.toJson()
    )

    return GsonBuilder().setPrettyPrinting().create().toJson(feature)
}