package np.com.naxa.drone_tasking_manager.features.login.repositories

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import np.com.naxa.drone_tasking_manager.core.services.ApiService
import np.com.naxa.drone_tasking_manager.core.utils.Response
import np.com.naxa.drone_tasking_manager.core.services.storage.MMKVStorageService
import np.com.naxa.drone_tasking_manager.core.services.storage.StorageKeys
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
class LoginRepositoryImpl @Inject constructor(private val apiService: ApiService) :
    LoginRepository {

    // Singleton instance of MMKVStorageService
    // This service is used for storing and retrieving data from persistent storage
    private val storageService = MMKVStorageService.getInstance()

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
        password: String,
        forceRefresh: Boolean
    ): Flow<Response<LoginResponse>> {
        return flow {
            emit(Response.Loading())

            val response =
                try {
                    apiService.login(role, username, password, forceRefresh)
                } catch (e: HttpException) {
                    // Handle HTTP-specific exceptions
                    return@flow emit(Response.Error(e.message()))
                } catch (e: IOException) {
                    // Handle network/IO-related exceptions
                    e.printStackTrace()
                    return@flow emit(Response.Error(e.message ?: "Could not load data"))
                } catch (e: Exception) {
                    // Handle any other unexpected exceptions
                    return@flow emit(Response.Error(e.message ?: "Unknown Error"))
                }

            val loginDetails = response.toLoginResponse()

            saveUserData(loginDetails)

            emit(Response.Success(data = loginDetails))

        }
    }


    override suspend fun googleLogin(
        role: String,
        code: String,
        state: String,
        forceRefresh: Boolean
    ): Flow<Response<LoginResponse>> {
        Log.d("TAG", "googleLogin: Called")


        return flow {
            emit(Response.Loading())

            val response =
                try {
                    apiService.googleLogin(
                        code,
                        "ysFFkmJRMJtxVejpCaq3M1Qdp8J3O7",
                        role,
                        forceRefresh
                    )
                } catch (e: HttpException) {
                    // Handle HTTP-specific exceptions
                    Log.d("TAG", "googleLogin i am here: ${e.message()}")
                    return@flow emit(Response.Error(e.message()))
                } catch (e: IOException) {
                    // Handle network/IO-related exceptions
                    e.printStackTrace()
                    Log.d("TAG", "googleLogin i am here1: ${e.message}")

                    return@flow emit(Response.Error(e.message ?: "Could not load data"))
                } catch (e: Exception) {
                    // Handle any other unexpected exceptions
                    Log.d("TAG", "googleLogin i am here2: ${e.message}")
                    return@flow emit(Response.Error(e.message ?: "Unknown Error"))
                }

            val loginDetails = response.toLoginResponse()

            Log.d("TAG", "googleLogin i am here3: ${loginDetails.detail}")

            saveUserData(loginDetails)

            emit(Response.Success(data = loginDetails))
        }
    }


    private fun saveUserData(loginDetails: LoginResponse) {

        Log.d("TAG", "saveUserData: ${loginDetails.detail}")

        storageService.save(StorageKeys.User.ACCESS_TOKEN, loginDetails.access_token)
        storageService.save(StorageKeys.User.REFRESH_TOKEN, loginDetails.refresh_token)
        storageService.save(StorageKeys.User.ROLE, loginDetails.role)
        storageService.save(StorageKeys.User.TOKEN_TYPE, loginDetails.token_type)
    }
}