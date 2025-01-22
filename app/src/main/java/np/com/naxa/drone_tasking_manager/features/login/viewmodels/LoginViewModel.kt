package np.com.naxa.drone_tasking_manager.features.login.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import np.com.naxa.drone_tasking_manager.Resources
import np.com.naxa.drone_tasking_manager.features.login.viewmodels.events.LoginEvents
import np.com.naxa.drone_tasking_manager.features.login.viewmodels.states.LoginStates
import np.com.naxa.drone_tasking_manager.features.login.viewmodels.usecases.GoogleLoginUseCase
import np.com.naxa.drone_tasking_manager.features.login.viewmodels.usecases.NormalLoginUseCase
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val normalLoginUseCase: NormalLoginUseCase,
    private val googleLoginUseCase: GoogleLoginUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(LoginStates())
    var state = _state.asStateFlow()

    fun onEvent(event: LoginEvents) {
        when (event) {
            is LoginEvents.NormalLogin -> {
                normalLogin(event.role, event.username, event.password)
            }

            is LoginEvents.GoogleLogin -> {
                googleLogin(event.role, event.code, event.state)
            }
        }
    }


    private fun normalLogin(role: String, username: String, password: String) {
        viewModelScope.launch {
            normalLoginUseCase.invoke(role, username, password).collect { result ->
                when (result) {
                    is Resources.Loading -> {
                        _state.value = _state.value.copy(isLoggingIn = true, isLoginIdle = false)
                    }

                    is Resources.Success -> {
                        result.data?.let { loginResponse ->
                            _state.value = _state.value.copy(isLoginSuccess = loginResponse, isLoggingIn = false)
                        }
                    }

                    is Resources.Error -> {
                        _state.value = _state.value.copy(isLoginError = result.message!!, isLoggingIn = false, isLoginSuccess = null)
                    }

                    else -> {
                        _state.value = _state.value.copy(isLoginError = "Unknown Error", isLoggingIn = false, isLoginSuccess = null)
                    }
                }
            }
        }
    }


    private fun googleLogin(role: String, code: String, state: String) {
        viewModelScope.launch {
            googleLoginUseCase.invoke(role, code, state)
                .collect { result ->
                when (result) {
                    is Resources.Loading -> {
                        _state.value = _state.value.copy(isLoggingIn = true, isLoginIdle = false)
                    }

                    is Resources.Success -> {
                        result.data?.let { loginResponse ->
                            _state.value = _state.value.copy(isLoginSuccess = loginResponse, isLoggingIn = false)
                        }
                    }

                    is Resources.Error -> {
                        _state.value = _state.value.copy(isLoginError = result.message!!, isLoggingIn = false, isLoginSuccess = null)
                    }

                    else -> {
                        _state.value = _state.value.copy(isLoginError = "Unknown Error", isLoggingIn = false, isLoginSuccess = null)
                    }
                }
            }
        }
    }


}