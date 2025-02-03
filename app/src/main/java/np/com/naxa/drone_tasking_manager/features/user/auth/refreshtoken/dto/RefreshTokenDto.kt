package np.com.naxa.drone_tasking_manager.features.user.auth.refreshtoken.dto

import com.google.gson.annotations.SerializedName

data class RefreshTokenDto(
    @SerializedName("access_token") val accessToken: String?,
    @SerializedName("detail") val detail: String?,
    @SerializedName("refresh_token") val refreshToken: String?,
    @SerializedName("role") val role: String?,
    @SerializedName("token_type") val tokenType: String?
)