package np.com.naxa.drone_tasking_manager.features.user.auth.refreshtoken.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import np.com.naxa.drone_tasking_manager.core.services.ApiService
import np.com.naxa.drone_tasking_manager.core.services.storage.MMKVStorageService
import np.com.naxa.drone_tasking_manager.core.services.storage.StorageKeys
import np.com.naxa.drone_tasking_manager.core.utils.Response
import np.com.naxa.drone_tasking_manager.core.utils.responsevalidator.validateResponse
import np.com.naxa.drone_tasking_manager.features.user.auth.refreshtoken.mapper.toRefreshToken
import np.com.naxa.drone_tasking_manager.features.user.auth.refreshtoken.models.RefreshToken
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class RefreshTokenRepositoryImpl @Inject constructor(private val apiService: ApiService) :
    RefreshTokenRepository {

    // Singleton instance of MMKVStorageService
    // This service is used for storing and retrieving data from persistent storage
    private val storageService = MMKVStorageService.getInstance()

    override suspend fun refreshToken(forceRefresh: Boolean): Flow<Response<RefreshToken>> {
        return flow {
            emit(Response.Loading())

            val response = try {
                apiService.refreshToken(forceRefresh = forceRefresh)
            } catch (e: HttpException) {
                e.printStackTrace()

                // Handle HTTP-specific exceptions
                return@flow emit(Response.Error(validateResponse(e)))
            } catch (e: IOException) {
                // Handle network/IO-related exceptions
                e.printStackTrace()
                return@flow emit(Response.Error(e.message ?: "Could not load data"))
            } catch (e: Exception) {
                // Handle any other unexpected exceptions
                e.printStackTrace()
                return@flow emit(Response.Error(e.message ?: "Unknown Error"))
            }

            val refreshedToken = response.toRefreshToken()

            updateTokenData(response = refreshedToken)

            emit(Response.Success(refreshedToken))

        }
    }


    private fun updateTokenData(response: RefreshToken) {

        storageService.save(StorageKeys.User.ACCESS_TOKEN, response.access_token)
        storageService.save(StorageKeys.User.REFRESH_TOKEN, response.refresh_token)
        storageService.save(StorageKeys.User.ROLE, response.role)
        storageService.save(StorageKeys.User.TOKEN_TYPE, response.token_type)
    }
}