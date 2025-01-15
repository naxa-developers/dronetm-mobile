package np.com.naxa.drone_tasking_manager.features.login.viewmodels.states

import np.com.naxa.drone_tasking_manager.features.login.models.LoginResponse

data class LoginStates(
    val isLoginIdle: Boolean = true,
    val isLoginError : String = "",
    val isLoggingIn : Boolean = false,
    val isLoginSuccess: LoginResponse? = null,
)
