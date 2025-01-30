package np.com.naxa.drone_tasking_manager.features.tasks.dto

import com.google.gson.annotations.SerializedName
import np.com.naxa.drone_tasking_manager.features.projects.dto.project.Outline
import np.com.naxa.drone_tasking_manager.features.projects.dto.projects_centroid.Centroid

data class TaskDto(
    @SerializedName("id") var id: String? = null,
    @SerializedName("project_id") var projectId: String? = null,
    @SerializedName("project_name") var projectName: String? = null,
    @SerializedName("project_task_index") var projectTaskIndex: Int? = null,
    @SerializedName("user_id") var userId: String? = null,
    @SerializedName("name") var userName: String? = null,
    @SerializedName("state") var state: String? = null,
    @SerializedName("front_overlap") var frontOverlap: Int? = null,
    @SerializedName("total_area_sqkm") var totalAreaSqkm: Double? = null,
    @SerializedName("flight_time_minutes") var flightTimeMinutes: Double? = null,
    @SerializedName("flight_distance_km") var flightDistanceKm: Double? = null,
    @SerializedName("total_image_uploaded") var totalImageUploaded: Int? = null,
    @SerializedName("image_count") var imageCount: Int? = null,
    @SerializedName("assets_url") var assetsUrl: String? = null,
    @SerializedName("outline") var outline: Outline? = null,
    @SerializedName("centroid") var centroid: Centroid? = null,
    @SerializedName("side_overlap") var sideOverlap: Int? = null,
    @SerializedName("gsd_cm_px") var gsdCmPx: Int? = null,
    @SerializedName("gimble_angles_degrees") var gimbleAnglesDegrees: Int? = null,
    @SerializedName("created_at") var createdAt: String? = null,
    @SerializedName("updated_at") var updatedAt: String? = null,
)