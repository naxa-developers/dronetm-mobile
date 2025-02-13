package np.com.naxa.drone_tasking_manager.features.user.dashboard.viewmodels.states

import np.com.naxa.drone_tasking_manager.features.user.dashboard.models.UsersTaskStat

data class UserTaskStatStates(
    val isFetching: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String = "",
    val usersTaskStat: UsersTaskStat? = null
)