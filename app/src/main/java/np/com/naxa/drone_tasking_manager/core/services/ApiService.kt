package np.com.naxa.drone_tasking_manager
import np.com.naxa.drone_tasking_manager.features.login.dto.LoginResponseDto
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {

    /**
     * Logs in a user.
     *
     * @param role The role of the user.
     * @param username The username of the user.
     * @param password The password of the user.
     * @return A [LoginResponseDto] containing the user's access token and refresh token.
     */
    @POST("api/users/login")
    @FormUrlEncoded
    suspend fun login(@Field("role") role: String,
                      @Field("username") username: String,
                      @Field("password") password: String): LoginResponseDto

    @POST("api/users/login")
    @FormUrlEncoded
    suspend fun googleLogin(@Query("role") role: String,
                      @Query("code") username: String,
                      @Query("state") password: String): LoginResponseDto


}