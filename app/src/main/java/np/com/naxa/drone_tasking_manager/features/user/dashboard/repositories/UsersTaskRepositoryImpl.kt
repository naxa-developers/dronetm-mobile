package np.com.naxa.drone_tasking_manager.features.user.dashboard.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import np.com.naxa.drone_tasking_manager.core.services.ApiService
import np.com.naxa.drone_tasking_manager.core.utils.Response
import np.com.naxa.drone_tasking_manager.core.utils.responsevalidator.validateResponse
import np.com.naxa.drone_tasking_manager.features.user.auth.refreshtoken.mapper.toRefreshToken
import np.com.naxa.drone_tasking_manager.features.user.dashboard.mapper.toUserTaskStat
import np.com.naxa.drone_tasking_manager.features.user.dashboard.mapper.toUsersTask
import np.com.naxa.drone_tasking_manager.features.user.dashboard.models.UsersTask
import np.com.naxa.drone_tasking_manager.features.user.dashboard.models.UsersTaskStat
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class UsersTaskRepositoryImpl @Inject constructor(private val apiService: ApiService) :
    UsersTaskRepository {

    override suspend fun fetchUsersTask(forceRefresh: Boolean): Flow<Response<UsersTask>> {
        return flow {
            emit(Response.Loading())

            val response = try {
                apiService.getUsersTask(forceRefresh = forceRefresh)
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

            val refreshedToken = response.toUsersTask()

            emit(Response.Success(refreshedToken))

        }
    }

    override suspend fun fetchUsersTaskStat(forceRefresh: Boolean): Flow<Response<UsersTaskStat>> {
        return flow {
            emit(Response.Loading())

            val response = try {
                apiService.getUsersTaskStat(forceRefresh = forceRefresh)
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

            val refreshedToken = response.toUserTaskStat()

            emit(Response.Success(refreshedToken))

        }
    }
}