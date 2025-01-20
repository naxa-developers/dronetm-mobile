package np.com.naxa.drone_tasking_manager.features.login.repositories

import kotlinx.coroutines.flow.Flow
import np.com.naxa.drone_tasking_manager.Resources
import np.com.naxa.drone_tasking_manager.features.login.models.LoginResponse

interface LoginRepository {

    suspend fun normalLogin(role: String, username: String, password: String): Flow<Resources<LoginResponse>>
    suspend fun googleLogin(role: String, code: String, state: String): Flow<Resources<LoginResponse>>
}