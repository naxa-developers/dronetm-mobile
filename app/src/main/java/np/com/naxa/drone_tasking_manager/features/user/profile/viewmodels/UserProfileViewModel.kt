package np.com.naxa.drone_tasking_manager.features.user.profile.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import np.com.naxa.drone_tasking_manager.core.utils.Response
import np.com.naxa.drone_tasking_manager.features.user.profile.viewmodels.events.UserProfileEvents
import np.com.naxa.drone_tasking_manager.features.user.profile.viewmodels.states.UserProfileStates
import np.com.naxa.drone_tasking_manager.features.user.profile.viewmodels.usecases.FetchUserProfileUseCase
import np.com.naxa.drone_tasking_manager.features.user.profile.viewmodels.usecases.UpdateBasicUserDetailsUseCase
import np.com.naxa.drone_tasking_manager.features.user.profile.viewmodels.usecases.UpdateOtherUserDetailsUseCase
import javax.inject.Inject

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val fetchUserProfileUseCase: FetchUserProfileUseCase,
    private val updateBasicUserDetailsUseCase: UpdateBasicUserDetailsUseCase,
    private val updateOtherUserDetailsUseCase: UpdateOtherUserDetailsUseCase
) : ViewModel() {

    private val _userProfileState = MutableStateFlow(UserProfileStates())
    var userProfileState = _userProfileState.asStateFlow()

    private val _userProfileUpdateState = MutableStateFlow(UserProfileStates())
    var userProfileUpdateState = _userProfileUpdateState.asStateFlow()


    fun onEvent(event: UserProfileEvents) {
        when (event) {
            is UserProfileEvents.FetchUserProfile -> {
                fetchUserProfile()
            }

            is UserProfileEvents.UpdateBasicDetails -> {
                TODO()
            }

            is UserProfileEvents.UpdateOtherDetails -> {
                TODO()
            }
        }
    }


    private fun fetchUserProfile() {
        viewModelScope.launch {
            fetchUserProfileUseCase.invoke(forceRefresh = true).collect { result ->
                when (result) {
                    is Response.Loading -> {
                        _userProfileState.value = _userProfileState.value.copy(
                            isUserProfileLoading = true,
                            isUserProfileSuccess = false,
                        )
                    }

                    is Response.Success -> {
                        _userProfileState.value = _userProfileState.value.copy(
                            isUserProfileLoading = false,
                            isUserProfileSuccess = true,
                            userProfile = result.data,
                            userProfileError = ""
                        )
                    }

                    is Response.Error -> {
                        _userProfileState.value = _userProfileState.value.copy(
                            isUserProfileLoading = false,
                            isUserProfileSuccess = false,
                            userProfileError = result.message
                        )
                    }

                    else -> {
                        _userProfileState.value = _userProfileState.value.copy(
                            isUserProfileLoading = false,
                            isUserProfileSuccess = false,
                            userProfileError = "Unknown Error"
                        )
                    }
                }
            }
        }
    }

}