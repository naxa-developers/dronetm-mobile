package np.com.naxa.drone_tasking_manager.features.user.auth.refreshtoken.viewmodels.usecases

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import np.com.naxa.drone_tasking_manager.features.user.auth.refreshtoken.repositories.RefreshTokenRepository
import javax.inject.Inject

@Module
@InstallIn(SingletonComponent::class)
class RefreshTokenUseCase @Inject constructor(private val refreshTokenRepository: RefreshTokenRepository) {
    suspend operator fun invoke(forceRefresh: Boolean = true) = refreshTokenRepository.refreshToken(forceRefresh)
}