package np.com.naxa.drone_tasking_manager.features.user.profile.viewmodels.states

import np.com.naxa.drone_tasking_manager.features.user.profile.models.UserProfile

data class UserProfileStates(
    val isUserProfileLoading: Boolean = false,
    val isUserProfileSuccess: Boolean = false,
    val userProfileError: String = "",
    val userProfile: UserProfile? = null
)
