package np.com.naxa.drone_tasking_manager.features.tasks.dto

import com.google.gson.annotations.SerializedName

data class LockOrUnlockEventRequestBody(
    @SerializedName("event") val event: String,
    @SerializedName("updated_at") val updatedAt: String
)