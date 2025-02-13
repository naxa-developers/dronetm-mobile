package np.com.naxa.drone_tasking_manager.features.user.dashboard.viewmodels.usecases

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import np.com.naxa.drone_tasking_manager.features.user.dashboard.repositories.UsersTaskRepository
import javax.inject.Inject

@Module
@InstallIn(SingletonComponent::class)
class UsersTaskStatUseCase @Inject constructor(private val usersTaskRepository: UsersTaskRepository) {
    /**
     * Fetches user task statistics from the repository.
     *
     * @param forceRefresh Whether to force a refresh of the data, even if it is cached. Defaults to true.
     * @return The user task statistics.
     */
    suspend operator fun invoke(forceRefresh: Boolean = true) =
        usersTaskRepository.fetchUsersTaskStat(forceRefresh)

}