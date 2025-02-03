package np.com.naxa.drone_tasking_manager.features.user.profile.views.widgets

sealed class ProfileNavData(val route: String, val title: String) {
    data object BasicScreen : ProfileNavData("basic_details", "Basic Details")
    data object OtherScreen : ProfileNavData("other_details", "Other Details")
    data object PassScreen : ProfileNavData("password", "Password")
}