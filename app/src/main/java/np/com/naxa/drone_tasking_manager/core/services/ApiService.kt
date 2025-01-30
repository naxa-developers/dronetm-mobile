package np.com.naxa.drone_tasking_manager.core.services

import np.com.naxa.drone_tasking_manager.features.login.dto.LoginResponseDto
import np.com.naxa.drone_tasking_manager.features.projects.dto.project.ProjectDto
import np.com.naxa.drone_tasking_manager.features.projects.dto.projects.ProjectsResponseDto
import np.com.naxa.drone_tasking_manager.features.projects.dto.projects_centroid.CentroidResult
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    // @param force_refresh to control the request and cache

    /**
     * Logs in a user.
     *
     * @param role The role of the user.
     * @param username The username of the user.
     * @param password The password of the user.
     * @return A [LoginResponseDto] containing the user's access token and refresh token.
     *
     */
    @POST("api/users/login/")
    @FormUrlEncoded
    suspend fun login(
        @Field("role") role: String = "DRONE_PILOT",
        @Field("username") username: String,
        @Field("password") password: String,
        @Query("force_refresh") forceRefresh: Boolean = false
    ): LoginResponseDto


    /**
     * Data class representing the response from the Google login API.
     */
    @GET("api/users/callback/")
    suspend fun googleLogin(
        @Query("code") code: String,
        @Query("state") state: String,
        @Query("role") role: String = "DRONE_PILOT",
        @Query("force_refresh") forceRefresh: Boolean = false
    ): LoginResponseDto

    /**
     * This interface defines the API endpoints for interacting with project data.
     */
    @GET("api/projects/")
    suspend fun fetchProjects(
        @Query("filter_by_owner") onlyMine: Boolean,
        @Query("search") query: String? = null,
        @Query("page") page: Int,
        @Query("results_per_page") size: Int,
        @Query("force_refresh") forceRefresh: Boolean = false
    ): ProjectsResponseDto

    /**
     * Interface defining API calls related to projects.
     */
    @GET("api/projects/{project_id}")
    suspend fun fetchProjectById(
        @Path("project_id") id: String,
        @Query("force_refresh") forceRefresh: Boolean = false
    ): ProjectDto

    /**
     * Interface defining API calls related to projects.
     */
    @GET("api/projects/centroids")
    suspend fun fetchProjectsCentroid(
        @Query("force_refresh") forceRefresh: Boolean = false
    ): List<CentroidResult>

}