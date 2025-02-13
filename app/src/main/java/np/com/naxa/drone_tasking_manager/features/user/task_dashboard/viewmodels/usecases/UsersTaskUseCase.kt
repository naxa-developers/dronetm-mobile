package np.com.naxa.drone_tasking_manager.features.user.task_dashboard.viewmodels.usecases

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import np.com.naxa.drone_tasking_manager.features.user.task_dashboard.repositories.UsersTaskRepository
import javax.inject.Inject

@Module
@InstallIn(SingletonComponent::class)
class UsersTaskUseCase @Inject constructor(private val usersTaskRepository: UsersTaskRepository) {
    /**
     * Executes the use case to fetch user tasks.
     *
     * @param forceRefresh Whether to force refresh the data from the repository. Defaults to `true`.
     * @return A list of user tasks.
     */
    suspend operator fun invoke(forceRefresh: Boolean = true) =
        usersTaskRepository.fetchUsersTask(forceRefresh)
}