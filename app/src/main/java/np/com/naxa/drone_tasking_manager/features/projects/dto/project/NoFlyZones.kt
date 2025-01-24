package np.com.naxa.drone_tasking_manager.features.projects.dto.project

import com.google.gson.annotations.SerializedName

data class NoFlyZones(
    @SerializedName("type") var type: String? = null,
    @SerializedName("geometry") var geometry: Geometry? = Geometry(),
    @SerializedName("properties") var properties: Properties? = Properties(),
    @SerializedName("id") var id: String? = null
)