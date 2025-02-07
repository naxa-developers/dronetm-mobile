package np.com.naxa.drone_tasking_manager.features.user.profile.viewmodels.events

import java.io.File

sealed class UserProfileEvents {
    //fetch my info
    data class FetchUserProfile(val forceRefresh: Boolean) : UserProfileEvents()


    data class UpdateBasicDetails(
        val userId: String,
        val name: String,
        val country: String,
        val city: String,
        val phone: String
    ) : UserProfileEvents()

    data class UpdateOtherDetails(
        val userId: String,
        val certifiedDroneOperator: Boolean,
        val droneYouOwn: String,
        val experienceYears: Int,
        val notifyForProjectsWithinKm: Int,
        val certificateFile: File?,
        val registrationFile: File?,
    ) : UserProfileEvents()


}