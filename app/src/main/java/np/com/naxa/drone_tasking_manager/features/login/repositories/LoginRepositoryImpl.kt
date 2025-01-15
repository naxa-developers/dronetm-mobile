package np.com.naxa.drone_tasking_manager.features.login.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import np.com.naxa.drone_tasking_manager.ApiService
import np.com.naxa.drone_tasking_manager.Resources
import np.com.naxa.drone_tasking_manager.features.login.mapper.toLoginResponse
import np.com.naxa.drone_tasking_manager.features.login.models.LoginResponse
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton


/**
 * Implementation of LoginRepository that handles user authentication operations.
 * This singleton class uses dependency injection to receive an ApiService instance.
 *
 * @property apiService The service responsible for making API calls related to authentication
 */
@Singleton
class LoginRepositoryImpl @Inject constructor(private val apiService: ApiService) : LoginRepository {

    /**
     * Attempts to log in a user with the provided credentials.
     * Returns a Flow that emits the current state of the login operation.
     *
     * @param username The user's username/email
     * @param password The user's password
     * @return Flow<Resources<LoginResponse>> A flow that emits loading states, success with login data, or error messages
     *
     * The flow emits the following states:
     * - Loading: When the login request starts
     * - Error: When an exception occurs during the login process
     * - Success: When login is successful, containing the user's login data
     * - Loading(false): When the operation completes (success or failure)
     */
    override suspend fun normalLogin(
        role: String,
        username: String,
        password: String
    ): Flow<Resources<LoginResponse>> {
        return flow {
            emit(Resources.Loading())

            val loginDetails =
                try {
                    apiService.login(role, username, password)
                } catch (e: HttpException) {
                    // Handle HTTP-specific exceptions
                    return@flow emit(Resources.Error(e.message()))
                } catch (e: IOException) {
                    // Handle network/IO-related exceptions
                    e.printStackTrace()
                    return@flow emit(Resources.Error(e.message ?: "Could not load data"))
                } catch (e: Exception) {
                    // Handle any other unexpected exceptions
                    return@flow emit(Resources.Error(e.message ?: "Unknown Error"))
                }

            emit(Resources.Success(data = loginDetails.toLoginResponse()))

        }
    }



    override suspend fun googleLogin(): Flow<Resources<LoginResponse>> {
        TODO("Not yet implemented")
    }
}