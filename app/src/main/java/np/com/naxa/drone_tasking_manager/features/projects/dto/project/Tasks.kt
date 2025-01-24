package np.com.naxa.drone_tasking_manager.features.projects.dto.project

import com.google.gson.annotations.SerializedName

data class Tasks(
    @SerializedName("id") var id: String? = null,
    @SerializedName("project_id") var projectId: String? = null,
    @SerializedName("project_task_index") var projectTaskIndex: Int? = null,
    @SerializedName("outline") var outline: Outline? = Outline(),
    @SerializedName("state") var state: String? = null,
    @SerializedName("user_id") var userId: String? = null,
    @SerializedName("name") var name: String? = null,
    @SerializedName("image_count") var imageCount: String? = null,
    @SerializedName("assets_url") var assetsUrl: String? = null,
    @SerializedName("total_area_sqkm") var totalAreaSqkm: Double? = null,
    @SerializedName("flight_time_minutes") var flightTimeMinutes: Double? = null,
    @SerializedName("flight_distance_km") var flightDistanceKm: Double? = null,
    @SerializedName("total_image_uploaded") var totalImageUploaded: String? = null
)