package np.com.naxa.drone_tasking_manager.features.tasks.dto

import com.google.gson.annotations.SerializedName

class TakeOffPointUpdateRequestBody(
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
)