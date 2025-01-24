package np.com.naxa.drone_tasking_manager.features.login.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import np.com.naxa.drone_tasking_manager.core.utils.Resources
import np.com.naxa.drone_tasking_manager.features.login.mapper.clientId
import np.com.naxa.drone_tasking_manager.features.login.mapper.scopes
import np.com.naxa.drone_tasking_manager.features.login.mapper.state
import np.com.naxa.drone_tasking_manager.features.login.models.GoogleLoginLinkResponse
import np.com.naxa.drone_tasking_manager.features.login.viewmodels.events.LoginEvents
import np.com.naxa.drone_tasking_manager.features.login.viewmodels.states.LoginStates
import np.com.naxa.drone_tasking_manager.features.login.viewmodels.usecases.GoogleLoginLinkUseCase
import np.com.naxa.drone_tasking_manager.features.login.viewmodels.usecases.GoogleLoginUseCase
import np.com.naxa.drone_tasking_manager.features.login.viewmodels.usecases.NormalLoginUseCase
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val normalLoginUseCase: NormalLoginUseCase,
    private val googleLoginLinkUseCase: GoogleLoginLinkUseCase,
    private val googleLoginUseCase: GoogleLoginUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(LoginStates())
    var state = _state.asStateFlow()

    var _googleLoginState: String? = null

    fun onEvent(event: LoginEvents) {
        when (event) {
            is LoginEvents.NormalLogin -> {
                normalLogin(event.role, event.username, event.password)
            }

            is LoginEvents.GoogleLogin -> {
                googleLogin(event.role, event.code, _googleLoginState ?: event.state)
            }

            is LoginEvents.GoogleLoginLinkUrl -> {
                initiateGoogleLogin(event.onLinkReceived)
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
                            _state.value = _state.value.copy(
                                isLoginSuccess = loginResponse,
                                isLoggingIn = false
                            )
                        }
                    }

                    is Resources.Error -> {
                        _state.value = _state.value.copy(
                            isLoginError = result.message!!,
                            isLoggingIn = false,
                            isLoginSuccess = null
                        )
                    }

                    else -> {
                        _state.value = _state.value.copy(
                            isLoginError = "Unknown Error",
                            isLoggingIn = false,
                            isLoginSuccess = null
                        )
                    }
                }
            }
        }
    }

    private fun initiateGoogleLogin(onLinkReceived: (clientId: String, scopes: List<String>) -> Unit) {
        _googleLoginState = null
        viewModelScope.launch {
            getGoogleLoginLink(
                onLoading = {
                    _state.value =
                        _state.value.copy(isLoggingIn = true, isLoginIdle = false)
                },
                onSuccess = { response ->
                    val clientId = response.clientId()
                    val state = response.state()
                    val scopes = response.scopes()

                    Log.d("AMIT", """initiateGoogleLogin: 
                        clientId: $clientId,
                        state: $state,
                        scopes: $scopes
                    """.trimMargin())

                    if (clientId != null && state != null && scopes != null) {
                        _googleLoginState = state
                        onLinkReceived.invoke(clientId, scopes)
                    } else {
                        _state.value = _state.value.copy(
                            isLoginError = "Unable to get google login link",
                            isLoggingIn = false,
                            isLoginSuccess = null
                        )
                    }
                },
                onError = {
                    _state.value = _state.value.copy(
                        isLoginError = "Error getting google login link",
                        isLoggingIn = false,
                        isLoginSuccess = null
                    )
                }
            )
        }
    }

    private fun googleLogin(role: String, code: String, state: String) {
        viewModelScope.launch {
            googleLoginUseCase.invoke(role, code, state)
                .collect { result ->
                    when (result) {
                        is Resources.Loading -> {
                            _state.value =
                                _state.value.copy(isLoggingIn = true, isLoginIdle = false)
                        }

                        is Resources.Success -> {
                            result.data?.let { loginResponse ->
                                _state.value = _state.value.copy(
                                    isLoginSuccess = loginResponse,
                                    isLoggingIn = false
                                )
                            }
                        }

                        is Resources.Error -> {
                            _state.value = _state.value.copy(
                                isLoginError = result.message!!,
                                isLoggingIn = false,
                                isLoginSuccess = null
                            )
                        }

                        else -> {
                            _state.value = _state.value.copy(
                                isLoginError = "Unknown Error",
                                isLoggingIn = false,
                                isLoginSuccess = null
                            )
                        }
                    }
                }
        }
    }

    private fun getGoogleLoginLink(
        onLoading: suspend () -> Unit,
        onSuccess: suspend (GoogleLoginLinkResponse) -> Unit,
        onError: suspend (String) -> Unit
    ) {
        viewModelScope.launch {
            googleLoginLinkUseCase.invoke()
                .collect { result ->
                    when (result) {
                        is Resources.Loading -> {
                            onLoading.invoke()
                        }

                        is Resources.Success -> {
                            onSuccess.invoke(result.data!!)
                        }

                        is Resources.Error -> {
                            onError.invoke(result.message!!)
                        }

                    }
                }
        }
    }

}