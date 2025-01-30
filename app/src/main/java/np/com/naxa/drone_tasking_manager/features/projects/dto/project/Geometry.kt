package np.com.naxa.drone_tasking_manager.features.projects.dto.project

import com.google.gson.annotations.SerializedName

data class Geometry(
    @SerializedName("type") var type: String? = null,
    @SerializedName("coordinates") var coordinates: ArrayList<ArrayList<ArrayList<Double>>> = arrayListOf()
)