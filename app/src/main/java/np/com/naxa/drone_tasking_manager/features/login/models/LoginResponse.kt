package np.com.naxa.drone_tasking_manager.features.login.models

data class LoginResponse(
    val access_token: String?,
    val detail: String?,
    val refresh_token: String?,
    val role: String?,
    val token_type: String?
)