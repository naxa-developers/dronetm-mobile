package np.com.naxa.drone_tasking_manager.features.tasks.dto

import com.google.gson.annotations.SerializedName

data class TaskEventRequestBody(
    @SerializedName("event") val event: String,
    @SerializedName("comment") val comment: String? = null,
    @SerializedName("updated_at") val updatedAt: String
)