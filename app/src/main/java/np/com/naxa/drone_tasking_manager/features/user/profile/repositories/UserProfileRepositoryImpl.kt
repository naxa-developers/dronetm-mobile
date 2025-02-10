package np.com.naxa.drone_tasking_manager.features.user.profile.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import np.com.naxa.drone_tasking_manager.core.services.ApiService
import np.com.naxa.drone_tasking_manager.core.services.retrofit.utils.MultipartFileUtils
import np.com.naxa.drone_tasking_manager.core.services.storage.MMKVStorageService
import np.com.naxa.drone_tasking_manager.core.services.storage.StorageKeys
import np.com.naxa.drone_tasking_manager.core.utils.Response
import np.com.naxa.drone_tasking_manager.features.user.profile.mapper.toBasicUserProfileUpdateDetails
import np.com.naxa.drone_tasking_manager.features.user.profile.mapper.toOtherUserProfileUpdateDetails
import np.com.naxa.drone_tasking_manager.features.user.profile.mapper.toUserProfile
import np.com.naxa.drone_tasking_manager.features.user.profile.models.UserProfile
import np.com.naxa.drone_tasking_manager.features.user.profile.models.UserProfileUpdateDetails
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.HttpException
import java.io.File
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
        certificateFile: File?,
        registrationFile: File?
    ): Flow<Response<UserProfileUpdateDetails>> {
        return flow {
            emit(Response.Loading())

            val response =
                try {

                    val otherDetailsJson = JSONObject().apply {
                        put("certificate_drone_operator", certifiedDroneOperator)
                        put("drone_you_own", droneYouOwn)
                        put("experience_years", experienceYears)
                        put("notify_for_projects_within_km", notifyForProjectsWithinKm)
                       if(certificateFile != null) put("certificate_file", certificateFile.name)
                        if(registrationFile != null) put("registration_file", registrationFile.name)
                        put("notify_for_projects_within_km", notifyForProjectsWithinKm)
                    }.toString()

                    val otherDetailsRequestBody = otherDetailsJson.toRequestBody("application/json".toMediaTypeOrNull())



                    apiService.updateUserOtherDetails(
                        userId = userId,

//                        certifiedDroneOperator = MultipartFileUtils.createPartFromBoolean(certifiedDroneOperator),
//                        droneYouOwn = MultipartFileUtils.createPartFromString(droneYouOwn),
//                        experienceYears = MultipartFileUtils.createPartFromInt(experienceYears),
//                        notifyForProjectsWithinKm = MultipartFileUtils.createPartFromInt(notifyForProjectsWithinKm),

//                        body = mapOf(
//                            "certificate_drone_operator" to MultipartFileUtils.createPartFromBoolean(certifiedDroneOperator),
//                            "drone_you_own" to MultipartFileUtils.createPartFromString(droneYouOwn),
//                            "experience_years" to MultipartFileUtils.createPartFromInt(experienceYears),
//                            "notify_for_projects_within_km" to MultipartFileUtils.createPartFromInt(notifyForProjectsWithinKm)
//                        ),
                        body = otherDetailsRequestBody,
//                        certificateFile =  MultipartFileUtils.getMultipartBodyPart(file = certificateFile, "certificate_file"),
//                        registrationFile = MultipartFileUtils.getMultipartBodyPart(file = registrationFile, "registration_file")
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
            val myInfoDetails = response.toOtherUserProfileUpdateDetails()

            emit(Response.Success(data = myInfoDetails))

        }
    }


    fun getFormDataPartMap(data: Map<String, String>): Map<String, RequestBody> {
        return data.mapValues {
            it.value.toRequestBody("multipart/form-data".toMediaTypeOrNull())
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