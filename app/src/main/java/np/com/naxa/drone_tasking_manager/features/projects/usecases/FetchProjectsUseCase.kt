package np.com.naxa.drone_tasking_manager.features.projects.usecases

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import np.com.naxa.drone_tasking_manager.features.projects.repositories.ProjectsRepository
import javax.inject.Inject


/**
 * A Use Case class responsible for fetching projects from the repository.
 *
 * This class encapsulates the logic for retrieving project data, allowing for pagination,
 * filtering, and refreshing. It acts as an intermediary between the data layer (repository)
 * and the presentation layer (e.g., ViewModels).
 *
 * This class is designed to be used with Hilt for dependency injection and is installed in
 * the [SingletonComponent], meaning a single instance will be provided throughout the
 * application's lifecycle.
 *
 * @property projectsRepository The repository responsible for data access related to projects.
 */
@Module
@InstallIn(SingletonComponent::class)
class FetchProjectsUseCase @Inject constructor(private val projectsRepository: ProjectsRepository) {
    suspend operator fun invoke(
        page: Int = 1,
        size: Int = 20,
        query: String? = null,
        onlyMine: Boolean = false,
        forceRefresh: Boolean = true
    ) = projectsRepository.fetchProjects(
        page = page,
        size = size,
        query = query,
        onlyMine = onlyMine,
        forceRefresh = forceRefresh,
    )

}