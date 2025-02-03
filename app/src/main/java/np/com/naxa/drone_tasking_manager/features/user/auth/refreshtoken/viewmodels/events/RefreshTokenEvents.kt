package np.com.naxa.drone_tasking_manager.features.user.auth.refreshtoken.viewmodels.events

sealed class RefreshTokenEvents {
    data class RefreshToken(val forceRefresh: Boolean) : RefreshTokenEvents()

}