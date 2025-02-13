package np.com.naxa.drone_tasking_manager.features.user.task_dashboard.viewmodels.states

import np.com.naxa.drone_tasking_manager.features.user.task_dashboard.models.UsersTask

data class UsersTaskStates(
    /**
     * Flag indicating if users task data is currently being fetched.
     */
    val isFetching: Boolean = false,
    /**
     * Flag indicating if users task data fetching was successful.
     */
    val isSuccess: Boolean = false,
    /**
     * Error message if users task data fetching failed.
     */
    val errorMessage: String = "",
    /**
     * The fetched UsersTask data.
     */
    val usersTask: UsersTask? = null
)