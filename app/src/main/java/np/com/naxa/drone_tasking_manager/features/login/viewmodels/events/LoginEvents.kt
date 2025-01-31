package np.com.naxa.drone_tasking_manager.features.login.viewmodels.events

sealed class LoginEvents {

    data class NormalLogin(val role: String, val username: String, val password: String) :
        LoginEvents()

    data class GoogleLogin(val role: String, val code: String, val state: String) : LoginEvents()

    data class RefreshToken(val onRefreshed: (Boolean) -> Unit) : LoginEvents()
}