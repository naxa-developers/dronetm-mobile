package np.com.naxa.drone_tasking_manager.features.user.profile.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import np.com.naxa.drone_tasking_manager.ApiService
import np.com.naxa.drone_tasking_manager.Resources
import np.com.naxa.drone_tasking_manager.features.login.mapper.toLoginResponse
import np.com.naxa.drone_tasking_manager.features.user.profile.mapper.toUserProfile
import np.com.naxa.drone_tasking_manager.features.user.profile.models.UserProfile
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class UserProfileRepositoryImpl @Inject constructor(private val apiService: ApiService) : UserProfileRepository {
    override suspend fun fetchMyInfo(): Flow<Resources<UserProfile>> {
        return flow {
            emit(Resources.Loading())

            val response =
                try {
                    apiService.fetchMyInfo()
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

            val myInfoDetails = response.toUserProfile()


            emit(Resources.Success(data = myInfoDetails))

        }
    }

    override suspend fun updateBasicDetails(
        name: String,
        country: String,
        city: String,
        phone: String
    ): Flow<Resources<UserProfile>> {
        TODO("Not yet implemented")
    }

    override suspend fun updateOtherDetails(
        certifiedDroneOperator: Boolean,
        droneYouOwn: String,
        experienceYears: Int,
        notifyForProjectsWithinKm: Int,
        registrationCertificateUrl: String,
        registrationFile: String
    ): Flow<Resources<UserProfile>> {
        TODO("Not yet implemented")
    }

}