package np.com.naxa.drone_tasking_manager.features.user.auth.refreshtoken.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import np.com.naxa.drone_tasking_manager.core.utils.Response
import np.com.naxa.drone_tasking_manager.features.user.auth.refreshtoken.viewmodels.events.RefreshTokenEvents
import np.com.naxa.drone_tasking_manager.features.user.auth.refreshtoken.viewmodels.states.RefreshTokenState
import np.com.naxa.drone_tasking_manager.features.user.auth.refreshtoken.viewmodels.usecases.RefreshTokenUseCase
import javax.inject.Inject

@HiltViewModel
class RefreshTokenViewModel @Inject constructor(private val refreshTokenUseCase: RefreshTokenUseCase) :
    ViewModel() {

    private val _state = MutableStateFlow(RefreshTokenState())
    val state = _state.asStateFlow()


    fun onEvent(event: RefreshTokenEvents) {
        when (event) {
            is RefreshTokenEvents.RefreshToken -> {
                fetchNewToken(forceRefresh = event.forceRefresh)
            }
        }
    }

    private fun fetchNewToken(forceRefresh: Boolean = true) {
        viewModelScope.launch {
            refreshTokenUseCase.invoke(forceRefresh = forceRefresh).collect { result ->

                when (result) {
                    is Response.Loading -> {
                        _state.value = _state.value.copy(
                            isLoading = true,
                            isSuccess = false,
                            errorMessage = ""
                        )
                    }

                    is Response.Error -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            isSuccess = false,
                            errorMessage = result.message
                        )
                    }

                    is Response.Success -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            isSuccess = true,
                            refreshToken = result.data
                        )
                    }
                }
            }
        }
    }

}