package np.com.naxa.drone_tasking_manager.features.user.profile.repositories

import kotlinx.coroutines.flow.Flow
import np.com.naxa.drone_tasking_manager.core.utils.Response
import np.com.naxa.drone_tasking_manager.features.user.profile.models.UserProfile

interface UserProfileRepository {
    suspend fun fetchMyInfo(forceRefresh: Boolean = true): Flow<Response<UserProfile>>

    suspend fun updateBasicDetails(
        name: String,
        country: String,
        city: String,
        phone: String
    ): Flow<Response<UserProfile>>


    suspend fun updateOtherDetails(
        certifiedDroneOperator: Boolean,
        droneYouOwn: String,
        experienceYears: Int,
        notifyForProjectsWithinKm: Int,
        registrationCertificateUrl: String,
        registrationFile: String
    ): Flow<Response<UserProfile>>
}