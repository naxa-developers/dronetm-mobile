package np.com.naxa.drone_tasking_manager.features.user.profile.repositories

import kotlinx.coroutines.flow.Flow
import np.com.naxa.drone_tasking_manager.Resources
import np.com.naxa.drone_tasking_manager.features.login.models.LoginResponse
import np.com.naxa.drone_tasking_manager.features.user.profile.models.UserProfile

interface UserProfileRepository {
    suspend fun fetchMyInfo(): Flow<Resources<UserProfile>>

    suspend fun updateBasicDetails(
        name: String,
        country: String,
        city: String,
        phone: String
    ): Flow<Resources<UserProfile>>


    suspend fun updateOtherDetails(
        certifiedDroneOperator: Boolean,
        droneYouOwn: String,
        experienceYears: Int,
        notifyForProjectsWithinKm: Int,
        registrationCertificateUrl: String,
        registrationFile: String
    ): Flow<Resources<UserProfile>>
}