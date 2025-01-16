package np.com.naxa.drone_tasking_manager.features.login.viewmodels.events

sealed class LoginEvents {

    data class NormalLogin(val role: String, val username: String, val password: String) : LoginEvents()

    data object GoogleLogin : LoginEvents()
}