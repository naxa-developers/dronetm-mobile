package np.com.naxa.drone_tasking_manager.core.services
import np.com.naxa.drone_tasking_manager.features.login.dto.LoginResponseDto
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
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
    suspend fun login(@Field("role") role: String = "DRONE_PILOT",
                      @Field("username") username: String,
                      @Field("password") password: String,
                      @Query("force_refresh") forceRefresh: Boolean = false): LoginResponseDto


    @GET("api/users/callback/")
    suspend fun googleLogin(
                      @Query("code") code: String,
                      @Query("state") state: String,
                      @Query("role") role: String = "DRONE_PILOT",
                            @Query("force_refresh") forceRefresh: Boolean = false): LoginResponseDto


}