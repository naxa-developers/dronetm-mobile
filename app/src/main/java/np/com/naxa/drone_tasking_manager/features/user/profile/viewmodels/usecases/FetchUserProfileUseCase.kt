package np.com.naxa.drone_tasking_manager.features.user.profile.viewmodels.usecases

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import np.com.naxa.drone_tasking_manager.features.user.profile.repositories.UserProfileRepository
import javax.inject.Inject

@Module
@InstallIn(SingletonComponent::class)
class FetchUserProfileUseCase @Inject constructor(private val profileRepository: UserProfileRepository) {
    suspend operator fun invoke() = profileRepository.fetchMyInfo()

}