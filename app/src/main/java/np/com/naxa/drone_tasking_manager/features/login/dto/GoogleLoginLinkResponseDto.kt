package np.com.naxa.drone_tasking_manager.features.login.dto

import com.google.gson.annotations.SerializedName

data class GoogleLoginLinkResponseDto(
    @SerializedName("login_url")
    var loginUrl: String? = null
)