package np.com.naxa.drone_tasking_manager.core.services.storage


object StorageKeys {

    object App {
        const val FIRST_RUN = "first_run"
        const val APP_VERSION = "app_version"
    }

    object User{
        const val IS_LOGGED_IN = "is_logged_in"
        const val ACCESS_TOKEN = "access_token"
        const val REFRESH_TOKEN = "refresh_token"
        const val ROLE = "user_role"
        const val TOKEN_TYPE = "token_type"
        const val ID = "user_id"
        const val USER_NAME = "user_name"

    }

}