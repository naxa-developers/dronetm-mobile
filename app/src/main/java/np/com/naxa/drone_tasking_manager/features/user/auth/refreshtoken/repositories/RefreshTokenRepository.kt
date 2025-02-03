package np.com.naxa.drone_tasking_manager.features.user.auth.refreshtoken.repositories

import kotlinx.coroutines.flow.Flow
import np.com.naxa.drone_tasking_manager.core.utils.Response
import np.com.naxa.drone_tasking_manager.features.user.auth.refreshtoken.models.RefreshToken

interface RefreshTokenRepository {
    suspend fun refreshToken(forceRefresh: Boolean = true): Flow<Response<RefreshToken>>
}