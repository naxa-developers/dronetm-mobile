package np.com.naxa.drone_tasking_manager.features.tasks.usecases

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import np.com.naxa.drone_tasking_manager.features.tasks.repositories.TasksRepository
import javax.inject.Inject


/**
 * [FetchTaskDetailUseCase] is a Use Case responsible for locking the currently active task.
 *
 * This class provides a single public method, [invoke], which interacts with the [TasksRepository]
 * to perform the locking operation. It's designed to be used in the application's higher layers
 * (e.g., ViewModels) to encapsulate the task locking logic and maintain separation of concerns.
 *
 * @property tasksRepository The repository that handles task-related data operations.
 */
@Module
@InstallIn(SingletonComponent::class)
class FetchTaskDetailUseCase @Inject constructor(private val tasksRepository: TasksRepository) {
    suspend operator fun invoke(taskId: String, forceRefresh: Boolean = false) =
        tasksRepository.fetchTaskById(taskId, forceRefresh)

}