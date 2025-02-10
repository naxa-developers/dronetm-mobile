package np.com.naxa.drone_tasking_manager.features.user.profile.repositories

import kotlinx.coroutines.flow.Flow
import np.com.naxa.drone_tasking_manager.core.utils.Response
import np.com.naxa.drone_tasking_manager.features.user.profile.models.UserProfile
import np.com.naxa.drone_tasking_manager.features.user.profile.models.UserProfileUpdateDetails
import java.io.File

interface UserProfileRepository {
    suspend fun fetchMyInfo(forceRefresh: Boolean = true): Flow<Response<UserProfile>>

    suspend fun updateBasicDetails(
        userId: String,
        name: String,
        country: String,
        city: String,
        phone: String
    ): Flow<Response<UserProfileUpdateDetails>>


    suspend fun updateOtherDetails(
        userId: String,
        certifiedDroneOperator: Boolean,
        droneYouOwn: String,
        experienceYears: Int,
        notifyForProjectsWithinKm: Int,
        certificateFile: File?,
        registrationFile: File?
    ): Flow<Response<UserProfileUpdateDetails>>


    suspend fun updateUserPassword(
        userId: String,
        oldPassword: String,
        newPassword: String,
        confirmPassword: String,
    ): Flow<Response<UserProfileUpdateDetails>>
}