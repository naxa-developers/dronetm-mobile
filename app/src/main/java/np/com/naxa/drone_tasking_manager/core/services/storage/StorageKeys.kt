package np.com.naxa.drone_tasking_manager.core.services.storage

import np.com.naxa.drone_tasking_manager.core.utils.DataEncryptionUtils

object StorageKeys {

    object App {
        const val FIRST_RUN = "first_run"
        const val APP_VERSION = "app_version"
    }

    object User{
        const val IS_LOGGED_IN = "is_logged_in"
        const val ACCESS_TOKEN = "token_type"
        const val REFRESH_TOKEN = "token_type"
        const val ROLE = "token_type"
        const val TOKEN_TYPE = "token_type"

    }

}