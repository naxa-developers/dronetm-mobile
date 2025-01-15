package np.com.naxa.drone_tasking_manager
import np.com.naxa.drone_tasking_manager.features.login.dto.LoginResponseDto
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

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
    suspend fun googleLogin(@Field("role") role: String,
                      @Field("username") username: String,
                      @Field("password") password: String): LoginResponseDto


}