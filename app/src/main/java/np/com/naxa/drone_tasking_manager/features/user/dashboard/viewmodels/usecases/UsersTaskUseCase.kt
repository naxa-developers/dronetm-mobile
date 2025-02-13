package np.com.naxa.drone_tasking_manager.features.user.dashboard.viewmodels.usecases

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import np.com.naxa.drone_tasking_manager.features.user.dashboard.repositories.UsersTaskRepository
import javax.inject.Inject

@Module
@InstallIn(SingletonComponent::class)
class UsersTaskUseCase @Inject constructor(private val usersTaskRepository: UsersTaskRepository) {
    suspend operator fun invoke(forceRefresh: Boolean = true) =
        usersTaskRepository.fetchUsersTask(forceRefresh)
}