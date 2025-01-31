package np.com.naxa.drone_tasking_manager.features.login.repositories

import kotlinx.coroutines.flow.Flow
import np.com.naxa.drone_tasking_manager.core.utils.Response
import np.com.naxa.drone_tasking_manager.features.login.models.LoginResponse

interface LoginRepository {

    suspend fun normalLogin(role: String, username: String, password: String, forceRefresh: Boolean = true): Flow<Response<LoginResponse>>
    suspend fun googleLogin(role: String, code: String, state: String, forceRefresh: Boolean = true): Flow<Response<LoginResponse>>
    suspend fun refreshToken(): Flow<Response<LoginResponse>>
}