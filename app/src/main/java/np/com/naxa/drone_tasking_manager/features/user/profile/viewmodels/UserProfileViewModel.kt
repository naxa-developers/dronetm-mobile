package np.com.naxa.drone_tasking_manager.features.user.profile.viewmodels

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import np.com.naxa.drone_tasking_manager.core.services.storage.MMKVStorageService
import np.com.naxa.drone_tasking_manager.core.utils.Response
import np.com.naxa.drone_tasking_manager.features.user.profile.viewmodels.events.UserProfileEvents
import np.com.naxa.drone_tasking_manager.features.user.profile.viewmodels.states.USerProfileUpdateStates
import np.com.naxa.drone_tasking_manager.features.user.profile.viewmodels.states.UserProfileStates
import np.com.naxa.drone_tasking_manager.features.user.profile.viewmodels.usecases.FetchUserProfileUseCase
import np.com.naxa.drone_tasking_manager.features.user.profile.viewmodels.usecases.UpdateBasicUserDetailsUseCase
import np.com.naxa.drone_tasking_manager.features.user.profile.viewmodels.usecases.UpdateOtherUserDetailsUseCase
import java.io.File
import javax.inject.Inject

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val fetchUserProfileUseCase: FetchUserProfileUseCase,
    private val updateBasicUserDetailsUseCase: UpdateBasicUserDetailsUseCase,
    private val updateOtherUserDetailsUseCase: UpdateOtherUserDetailsUseCase
) : ViewModel() {

    private val _userProfileState = MutableStateFlow(UserProfileStates())
    var userProfileState = _userProfileState.asStateFlow()

    private val _userProfileUpdateState = MutableStateFlow(USerProfileUpdateStates())
    var userProfileUpdateState = _userProfileUpdateState.asStateFlow()


    fun onEvent(event: UserProfileEvents) {
        when (event) {
            is UserProfileEvents.FetchUserProfile -> {
                fetchUserProfile(forceRefresh = event.forceRefresh)
            }

            is UserProfileEvents.UpdateBasicDetails -> {
                updateBasicDetails(event.userId, event.name, event.country, event.city, event.phone)
            }

            is UserProfileEvents.UpdateOtherDetails -> {
                updateOtherDetails(event.userId, event.certifiedDroneOperator, event.droneYouOwn,
                    event.experienceYears, event.notifyForProjectsWithinKm, event.certificateFile, event.registrationFile)
            }
        }
    }

    private fun updateBasicDetails(
        userId: String,
        name: String,
        country: String,
        city: String,
        phone: String
    ) {

        viewModelScope.launch {
            updateBasicUserDetailsUseCase.invoke(userId, name, country, city, phone)
                .collect { result ->
                    when (result) {
                        is Response.Loading -> {
                            _userProfileUpdateState.value = _userProfileUpdateState.value.copy(
                                isUserProfileUpdateLoading = true,
                                isUserProfileUpdateSuccess = false,
                            )
                        }

                        is Response.Success -> {
                            _userProfileUpdateState.value = _userProfileUpdateState.value.copy(
                                isUserProfileUpdateLoading = false,
                                isUserProfileUpdateSuccess = true,
                                userProfileUpdateError = "",
                                userProfileUpdate = result.data
                            )

                            fetchUserProfile(forceRefresh = true)
                        }

                        is Response.Error -> {
                            _userProfileUpdateState.value = _userProfileUpdateState.value.copy(
                                isUserProfileUpdateLoading = false,
                                isUserProfileUpdateSuccess = false,
                                userProfileUpdateError = result.message
                            )
                        }
                    }
                }
        }
    }


    private fun updateOtherDetails(
        userId: String,
        certifiedDroneOperator: Boolean,
        droneYouOwn: String,
        experienceYears: Int,
        notifyForProjectsWithinKm: Int,
        certificateFile: File?,
        registrationFile: File?
    ) {

        viewModelScope.launch {
            updateOtherUserDetailsUseCase.invoke(
                userId,
                certifiedDroneOperator,
                droneYouOwn,
                experienceYears,
                notifyForProjectsWithinKm,
                certificateFile,
                registrationFile
            )
                .collect { result ->
                    when (result) {
                        is Response.Loading -> {
                            _userProfileUpdateState.value = _userProfileUpdateState.value.copy(
                                isUserProfileUpdateLoading = true,
                                isUserProfileUpdateSuccess = false,
                            )
                        }

                        is Response.Success -> {
                            _userProfileUpdateState.value = _userProfileUpdateState.value.copy(
                                isUserProfileUpdateLoading = false,
                                isUserProfileUpdateSuccess = true,
                                userProfileUpdateError = "",
                                userProfileUpdate = result.data
                            )

                            fetchUserProfile(forceRefresh = true)
                        }

                        is Response.Error -> {
                            _userProfileUpdateState.value = _userProfileUpdateState.value.copy(
                                isUserProfileUpdateLoading = false,
                                isUserProfileUpdateSuccess = false,
                                userProfileUpdateError = result.message
                            )
                        }
                    }
                }
        }
    }


    private fun fetchUserProfile(forceRefresh: Boolean) {
        viewModelScope.launch {
            fetchUserProfileUseCase.invoke(forceRefresh = forceRefresh).collect { result ->
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

                }
            }
        }
    }

}