package np.com.naxa.drone_tasking_manager.features.project_details.usecases

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import np.com.naxa.drone_tasking_manager.features.projects.repositories.ProjectsRepository
import javax.inject.Inject


/**
 * This use case is responsible for fetching a project by its ID.
 *
 * It utilizes the [ProjectsRepository] to retrieve the project data,
 * optionally allowing for a forced refresh of the data from the remote source.
 *
 * @property projectsRepository The repository responsible for handling project data operations.
 */
@Module
@InstallIn(SingletonComponent::class)
class FetchProjectDetailUseCase @Inject constructor(private val projectsRepository: ProjectsRepository) {
    suspend operator fun invoke(
        id: String,
        forceRefresh: Boolean = true
    ) = projectsRepository.fetchProjectById(
        id = id,
        forceRefresh = forceRefresh,
    )

}