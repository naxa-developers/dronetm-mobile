package np.com.naxa.drone_tasking_manager.features.projects.viewmodels.events


sealed class ProjectsEvent {

    /**
     * Represents an event to fetch a list of projects.
     *
     * This data class encapsulates the parameters required to request a paginated list of projects,
     * optionally filtered by a search query and whether to include only the user's own projects.
     *
     * @property page The page number of the project list to fetch. Defaults to 1 (the first page).
     * @property size The number of projects to include per page. Defaults to 20.
     * @property query An optional search query string. If provided, the server will filter the projects
     *                 based on this query. Can be `null` if no filtering is required.
     * @property onlyMine A boolean indicating whether to only fetch projects belonging to the current user.
     *                    Defaults to `false`.
     *
     * This class is a subclass of [ProjectsEvent], suggesting that it's used within an event-driven
     * architecture for managing project-related actions.
     *
     * Example usage:
     * ```kotlin
     * // Fetch the first page of projects
     * val fetchAll = FetchProjects()
     *
     * // Fetch the second page of projects with a page size of 10
     * val fetchPage2 = FetchProjects(page = 2, size = 10)
     *
     * // Search for projects with the name "My Project"
     * val searchQuery = FetchProjects(query = "My Project")
     *
     * // Fetch only my projects
     * val fetchMyProjects = FetchProjects(onlyMine = true)
     *
     * //Fetch my projects and search by "Test"
     * val fetchMyProjectsAndSearch = FetchProjects(query = "Test", onlyMine = true)
     * ```
     */
    data class FetchProjects(
        val page: Int = 1,
        val size: Int = 20,
        val query: String? = null,
        val onlyMine: Boolean = false
    ) : ProjectsEvent()

    /**
     * Represents an event to fetch the centroid of projects.
     *
     * This event is dispatched to trigger the process of fetching the centroid
     * (geographical center) for a set of projects. It can be used to either
     * perform a regular fetch or a forced fetch, which might bypass caching or
     * other optimizations.
     *
     * @property forceFully If true, forces a complete fetch of the centroid data, potentially
     *                      bypassing any caching or optimizations. If false (default), the
     *                      system may use cached data or other shortcuts to retrieve the
     *                      centroid.
     *
     * @constructor Creates a new FetchProjectsCentroid event.
     */
    data class FetchProjectsCentroid(
        val forceFully: Boolean = false
    ) : ProjectsEvent()
}