package np.com.naxa.drone_tasking_manager.features.user.profile.viewmodels.events

sealed class UserProfileEvents {
    //fetch my info
    data object FetchUserProfile : UserProfileEvents()


    data class UpdateBasicDetails(
        val name: String, val country: String, val city: String, val phone: String
    ) : UserProfileEvents()

    data class UpdateOtherDetails(
        val certifiedDroneOperator: Boolean,
        val droneYouOwn: String,
        val experienceYears: Int,
        val notifyForProjectsWithinKm: Int
    ) : UserProfileEvents()


}