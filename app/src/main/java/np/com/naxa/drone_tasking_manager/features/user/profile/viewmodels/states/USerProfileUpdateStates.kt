package np.com.naxa.drone_tasking_manager.features.user.profile.viewmodels.states

import np.com.naxa.drone_tasking_manager.features.user.profile.models.UserProfileUpdateDetails

data class USerProfileUpdateStates(
    val isUserProfileUpdateLoading: Boolean = false,
    val isUserProfileUpdateSuccess: Boolean = false,
    val userProfileUpdateError: String = "",
    val userProfileUpdate: UserProfileUpdateDetails? = null
)
