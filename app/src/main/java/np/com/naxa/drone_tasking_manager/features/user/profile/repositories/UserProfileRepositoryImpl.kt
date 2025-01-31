package np.com.naxa.drone_tasking_manager.features.user.profile.repositories

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import np.com.naxa.drone_tasking_manager.core.services.ApiService
import np.com.naxa.drone_tasking_manager.core.utils.Response
import np.com.naxa.drone_tasking_manager.features.user.profile.mapper.toUserProfile
import np.com.naxa.drone_tasking_manager.features.user.profile.models.UserProfile
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class UserProfileRepositoryImpl @Inject constructor(private val apiService: ApiService) :
    UserProfileRepository {
    override suspend fun fetchMyInfo(forceRefresh: Boolean): Flow<Response<UserProfile>> {
        return flow {
            emit(Response.Loading())

            val response =
                try {
                    apiService.fetchMyInfo(forceRefresh)
                } catch (e: HttpException) {
                    e.printStackTrace()
                    // Handle HTTP-specific exceptions
                    return@flow emit(Response.Error(e.message()))
                } catch (e: IOException) {
                    // Handle network/IO-related exceptions
                    e.printStackTrace()
                    return@flow emit(Response.Error(e.message ?: "Could not load data"))
                } catch (e: Exception) {
                    // Handle any other unexpected exceptions
                    e.printStackTrace()
                    return@flow emit(Response.Error(e.message ?: "Unknown Error"))
                }
            val myInfoDetails = response.toUserProfile()

            emit(Response.Success(data = myInfoDetails))

        }
    }

    override suspend fun updateBasicDetails(
        name: String,
        country: String,
        city: String,
        phone: String
    ): Flow<Response<UserProfile>> {
        TODO("Not yet implemented")
    }

    override suspend fun updateOtherDetails(
        certifiedDroneOperator: Boolean,
        droneYouOwn: String,
        experienceYears: Int,
        notifyForProjectsWithinKm: Int,
        registrationCertificateUrl: String,
        registrationFile: String
    ): Flow<Response<UserProfile>> {
        TODO("Not yet implemented")
    }

}