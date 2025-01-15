package np.com.naxa.drone_tasking_manager.features.login.viewmodels.usecases

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import np.com.naxa.drone_tasking_manager.features.login.repositories.LoginRepository
import javax.inject.Inject

@Module
@InstallIn(SingletonComponent::class)
class GoogleLoginUseCase @Inject constructor(private val loginRepository: LoginRepository) {
    suspend operator fun invoke() = loginRepository.googleLogin()
}