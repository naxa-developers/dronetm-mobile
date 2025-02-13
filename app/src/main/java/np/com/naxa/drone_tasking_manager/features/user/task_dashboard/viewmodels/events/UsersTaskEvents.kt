package np.com.naxa.drone_tasking_manager.features.user.task_dashboard.viewmodels.events

/**
 * A sealed class representing events for user tasks and user task statistics..
 */
sealed class UsersTaskEvents {
    /**
     * An event representing a request to fetch user tasks.
     *
     * @property forceRefresh Whether to force a refresh of the task list from the server.
     */
    data class fetchUsersTask(val forceRefresh: Boolean) : UsersTaskEvents()


    /**
     * An event triggered to fetch user task statistics.
     *
     * @property forceRefresh Indicates whether to force a refresh of the statistics from the server.
     */
    data class fetchUsersTaskStat(val forceRefresh: Boolean) : UsersTaskEvents()

}