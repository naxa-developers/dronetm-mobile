package np.com.naxa.drone_tasking_manager.features.user.task_dashboard.viewmodels.states

import np.com.naxa.drone_tasking_manager.features.user.task_dashboard.models.UsersTaskStat

data class UserTaskStatStates(
    /**
     * Flag indicating whether the data is currently being fetched.
     */
    val isFetching: Boolean = false,
    /**
     * Flag indicating whether the data fetch was successful.
     */
    val isSuccess: Boolean = false,
    /**
     * Error message if the data fetch failed.
     */
    val errorMessage: String = "",
    /**
     * The fetched user task statistics, if available.
     */
    val usersTaskStat: UsersTaskStat? = null
)