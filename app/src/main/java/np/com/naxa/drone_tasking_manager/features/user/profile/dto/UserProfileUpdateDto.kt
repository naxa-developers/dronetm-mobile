package np.com.naxa.drone_tasking_manager.features.user.profile.dto

import com.google.gson.annotations.SerializedName

class UserProfileUpdateDto (
    @SerializedName("message") val message: String?,
    @SerializedName("results") val results: UserProfileDto?,
)