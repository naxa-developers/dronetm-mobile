package np.com.naxa.drone_tasking_manager.features.user.auth.refreshtoken.models

data class RefreshToken(
    val access_token: String?,
    val refresh_token: String?,
    val role: String?,
    val token_type: String?
)