package np.com.naxa.drone_tasking_manager.features.projects.usecases

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import np.com.naxa.drone_tasking_manager.features.projects.repositories.ProjectsRepository
import javax.inject.Inject

/**
 * Use case responsible for fetching a list of projects (or a single project).
 * This use case interacts with the [ProjectsRepository] to retrieve project data,
 * optionally allowing filtering, pagination, and forcing a network refresh.
 *
 * @property projectsRepository The repository responsible for fetching project data.
 */
@Module
@InstallIn(SingletonComponent::class)
class FetchProjectsCentroidUseCase @Inject constructor(private val projectsRepository: ProjectsRepository) {
    suspend operator fun invoke(
        forceRefresh: Boolean = true
    ) = projectsRepository.fetchProjectsCentroid(
        forceRefresh = forceRefresh,
    )

}