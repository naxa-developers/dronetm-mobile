package np.com.naxa.drone_tasking_manager.features.user.profile.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import np.com.naxa.drone_tasking_manager.core.services.ApiService
import np.com.naxa.drone_tasking_manager.core.services.retrofit.utils.FileToMultipartFileUtils
import np.com.naxa.drone_tasking_manager.core.services.storage.MMKVStorageService
import np.com.naxa.drone_tasking_manager.core.services.storage.StorageKeys
import np.com.naxa.drone_tasking_manager.core.utils.Response
import np.com.naxa.drone_tasking_manager.features.user.profile.mapper.toBasicUserProfileUpdateDetails
import np.com.naxa.drone_tasking_manager.features.user.profile.mapper.toUserProfile
import np.com.naxa.drone_tasking_manager.features.user.profile.models.UserProfile
import np.com.naxa.drone_tasking_manager.features.user.profile.models.UserProfileUpdateDetails
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class UserProfileRepositoryImpl @Inject constructor(private val apiService: ApiService) :
    UserProfileRepository {

    private val storageService = MMKVStorageService.getInstance()

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

            storeUserData(myInfoDetails)

            emit(Response.Success(data = myInfoDetails))

        }
    }

    override suspend fun updateBasicDetails(
        userId: String,
        name: String,
        country: String,
        city: String,
        phone: String
    ): Flow<Response<UserProfileUpdateDetails>> {
        return flow {
            emit(Response.Loading())

            val response =
                try {
                    apiService.updateUser(
                        userId = userId, body = mapOf(
                            "name" to name,
                            "country" to country,
                            "city" to city,
                            "phone" to phone
                        )
                    )
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
            val myInfoDetails = response.toBasicUserProfileUpdateDetails()

            emit(Response.Success(data = myInfoDetails))

        }
    }

    override suspend fun updateOtherDetails(
        userId: String,
        certifiedDroneOperator: Boolean,
        droneYouOwn: String,
        experienceYears: Int,
        notifyForProjectsWithinKm: Int,
        certificateFile: String?,
        registrationFile: String?
    ): Flow<Response<UserProfileUpdateDetails>> {
        return flow {
            emit(Response.Loading())

            val response =
                try {
                    apiService.updateUserOtherDetails(
                        userId = userId, body = mapOf(
                            "certificate_drone_operator" to "$certifiedDroneOperator",
                            "drone_you_own" to droneYouOwn,
                            "experience_years" to "$experienceYears",
                            "notify_for_projects_within_km" to "$notifyForProjectsWithinKm"
                        ),
                        FileToMultipartFileUtils.getMultipartBodyPart(filePath = certificateFile, "certificate_file"),
                        FileToMultipartFileUtils.getMultipartBodyPart(filePath = registrationFile, "registration_file")
                    )
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
            val myInfoDetails = response.toBasicUserProfileUpdateDetails()

            emit(Response.Success(data = myInfoDetails))

        }
    }





    private fun storeUserData(myInfoDetails: UserProfile) {
        try {
            storageService.save(StorageKeys.User.ID, myInfoDetails.id)
            storageService.save(StorageKeys.User.USER_NAME, myInfoDetails.name)
        } catch (e: Exception) {
            // Handle any exceptions that might occur during data storage
            e.printStackTrace()
        }
    }

}