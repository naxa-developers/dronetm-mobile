package np.com.naxa.drone_tasking_manager.features.project_details.viewmodels.events

sealed class ProjectDetailEvent {
    /**
     * Represents an event to fetch a specific project by its ID.
     *
     * This event is used to trigger the process of retrieving a project's details
     * from a data source, using its unique identifier.
     *
     * @property id The unique identifier of the project to be fetched.
     *               This ID is used to locate the project within the data source.
     * @constructor Creates a [FetchProjectById] event with the specified project ID.
     * @see ProjectDetailEvent
     */
    data class FetchProjectById(val id: String, val forceRefresh: Boolean = false) : ProjectDetailEvent()
}