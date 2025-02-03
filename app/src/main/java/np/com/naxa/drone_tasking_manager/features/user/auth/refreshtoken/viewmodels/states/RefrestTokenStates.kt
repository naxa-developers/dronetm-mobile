package np.com.naxa.drone_tasking_manager.features.user.auth.refreshtoken.viewmodels.states

import np.com.naxa.drone_tasking_manager.features.user.auth.refreshtoken.models.RefreshToken

data class RefreshTokenState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String = "",
    val refreshToken: RefreshToken ?= null
)