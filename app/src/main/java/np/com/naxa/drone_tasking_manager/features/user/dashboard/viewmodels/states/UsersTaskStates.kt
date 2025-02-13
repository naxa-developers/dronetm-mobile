package np.com.naxa.drone_tasking_manager.features.user.dashboard.viewmodels.states

import np.com.naxa.drone_tasking_manager.features.user.dashboard.models.UsersTask

data class UsersTaskStates(
    val isFetching: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String = "",
    val usersTask: UsersTask?= null
)