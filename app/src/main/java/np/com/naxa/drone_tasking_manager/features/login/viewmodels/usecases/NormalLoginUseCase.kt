package np.com.naxa.drone_tasking_manager.features.login.viewmodels.usecases

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import np.com.naxa.drone_tasking_manager.features.login.repositories.LoginRepository
import javax.inject.Inject


@Module
@InstallIn(SingletonComponent::class)
class NormalLoginUseCase @Inject constructor(private val loginRepository: LoginRepository) {
    suspend operator fun invoke(role: String, username: String, password: String) = loginRepository.normalLogin(role, username, password)

}