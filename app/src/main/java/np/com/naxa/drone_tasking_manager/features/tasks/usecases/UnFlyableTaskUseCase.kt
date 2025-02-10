package np.com.naxa.drone_tasking_manager.features.tasks.usecases

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import np.com.naxa.drone_tasking_manager.features.tasks.repositories.TasksRepository
import javax.inject.Inject


/**
 * [UnFlyableTaskUseCase] is a Use Case responsible for requesting to make task un flyable.
 *
 * This class provides a single public method, [invoke], which interacts with the [TasksRepository]
 * to perform the locking operation. It's designed to be used in the application's higher layers
 * (e.g., ViewModels) to encapsulate the task unlocking logic and maintain separation of concerns.
 *
 * @property tasksRepository The repository that handles task-related data operations.
 */
@Module
@InstallIn(SingletonComponent::class)
class UnFlyableTaskUseCase @Inject constructor(private val tasksRepository: TasksRepository) {
    suspend operator fun invoke(taskId: String, projectId: String, comment: String? = null) =
        tasksRepository.taskUnFlyable(taskId, projectId, comment)
}