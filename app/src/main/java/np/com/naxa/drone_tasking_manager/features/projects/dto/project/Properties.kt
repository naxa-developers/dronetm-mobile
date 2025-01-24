package np.com.naxa.drone_tasking_manager.features.projects.dto.project

import com.google.gson.annotations.SerializedName

data class Properties(
    @SerializedName("id") var id: String? = null,
    @SerializedName("bbox") var bbox: ArrayList<Double> = arrayListOf()
)