package np.com.naxa.drone_tasking_manager.features.projects.repositories

import kotlinx.coroutines.flow.Flow
import np.com.naxa.drone_tasking_manager.core.utils.Response
import np.com.naxa.drone_tasking_manager.features.projects.models.Project
import np.com.naxa.drone_tasking_manager.features.projects.models.ProjectsResponse
import org.maplibre.geojson.Feature

interface ProjectsRepository {
    /**
     * Fetches a list of projects from the server.
     *
     * This function retrieves a paginated list of projects based on the provided parameters.
     * It supports filtering by a search query, retrieving only the user's own projects,
     * and forcing a refresh from the server.
     *
     * @param page The page number to retrieve (default: 1).
     * @param size The number of projects to retrieve per page (default: 20).
     * @param query An optional search query string to filter projects by name or description.
     *              If null, no filtering is applied.
     * @param onlyMine If true, only the projects belonging to the current user are returned.
     *                 If false (default), all projects are considered.
     * @param forceRefresh If true, the function will always attempt to fetch the latest data
     *                     from the server, bypassing any local cache. If false, the function may
     *                     return cached data if available. (default: true).
     * @return A [Flow] emitting [Response] wrapping a [ProjectsResponse].
     *         - [Resources.Loading]: Emitted when the data is being fetched.
     *         - [Resources.Success]: Emitted when the data is successfully fetched,
     *                                 containing the [ProjectsResponse].
     *         - [Resources.Error]: Emitted when an error occurs during the fetching process,
     *                               containing an error message.
     *         - [Resources.Idle]: Emitted when function is not performing any action (may not be used in this case).
     *
     * @see Response
     * @see ProjectsResponse
     */
    suspend fun fetchProjects(
        page: Int = 1,
        size: Int = 20,
        query: String? = null,
        onlyMine: Boolean = false,
        forceRefresh: Boolean = true
    ): Flow<Response<ProjectsResponse>>


    /**
     * Fetches a project by its ID.
     *
     * This function retrieves a project from the API service using the provided ID.
     * It supports forced refresh to bypass any potential caching mechanisms.
     * The result is emitted as a Flow of Resources, which can be in the following states:
     *   - Loading: Indicates that the request is in progress.
     *   - Success: Indicates that the request was successful and contains the Project data.
     *   - Error: Indicates that an error occurred during the request and contains an error message.
     *
     * @param id The ID of the project to fetch.
     * @param forceRefresh If true, forces a refresh of the data, bypassing any caching.
     *                     If false, the API service may return cached data if available.
     * @return A Flow of Resources<Project>, emitting the current state of the request.
     *         The Flow will emit at least one value (Loading) and then either Success or Error.
     */
    suspend fun fetchProjectById(
        id: String,
        forceRefresh: Boolean = true
    ): Flow<Response<Project>>


    /**
     * Fetches the centroid of projects, represented as a list of GeoJSON Feature objects.
     *
     * This function retrieves the geographical center point (centroid) for each project.
     * The centroid is represented as a GeoJSON Feature object, which includes the
     * geographical coordinates (geometry) and potentially other project-related
     * information (properties).
     *
     * @param forceRefresh If true, the data will be fetched from the remote source,
     *                     bypassing any cached data. If false, the function may
     *                     return cached data if available, potentially leading to
     *                     faster responses but possibly stale information. Defaults to true.
     *
     * @return A Flow emitting a Response object containing a list of Feature objects.
     *         - The Response object encapsulates the result of the network request,
     *           allowing for handling of success and failure states.
     *         - The list of Feature objects contains the centroid data for each
     *           project.
     *         - The Flow allows for asynchronous data delivery and can handle
     *           multiple emissions over time (e.g., updates).
     *         - Potential error cases within the Response can be:
     *           - `Response.Error` when something went wrong during the network request.
     *           - `Response.Success` but with empty list, if there are no project to display.
     *           - `Response.Loading` when the function is performing network request.
     *
     * @see Response
     * @see Feature
     * @see Flow
     */
    suspend fun fetchProjectsCentroid(
        forceRefresh: Boolean = true
    ): Flow<Response<List<Feature>>>
}