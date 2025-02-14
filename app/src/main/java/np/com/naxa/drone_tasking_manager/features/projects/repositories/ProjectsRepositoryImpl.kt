package np.com.naxa.drone_tasking_manager.features.projects.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import np.com.naxa.drone_tasking_manager.core.utils.Response
import np.com.naxa.drone_tasking_manager.core.services.ApiService
import np.com.naxa.drone_tasking_manager.core.utils.responsevalidator.ErrorResponse
import np.com.naxa.drone_tasking_manager.core.utils.responsevalidator.getErrorMessage
import np.com.naxa.drone_tasking_manager.features.projects.mapper.toProject
import np.com.naxa.drone_tasking_manager.features.projects.mapper.toProjectResponse
import np.com.naxa.drone_tasking_manager.features.projects.models.Project
import np.com.naxa.drone_tasking_manager.features.projects.models.ProjectsResponse
import org.maplibre.geojson.Feature
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProjectsRepositoryImpl @Inject constructor(private val apiService: ApiService) :
    ProjectsRepository {

    override suspend fun fetchProjects(
        page: Int,
        size: Int,
        query: String?,
        onlyMine: Boolean,
        forceRefresh: Boolean
    ): Flow<Response<ProjectsResponse>> {
        return flow {
            emit(Response.Loading())

            try {
                val response = apiService.fetchProjects(
                    page = page,
                    size = size,
                    query = query,
                    onlyMine = onlyMine,
                    forceRefresh = forceRefresh
                )

                emit(Response.Success(response.toProjectResponse()))

            } catch (e: HttpException) {
                return@flow emit(
                    Response.Error(
                        ErrorResponse.parseErrorBody(
                            e.response()?.errorBody()?.string()
                        ).getErrorMessage()
                    )
                )
            } catch (e: Exception) {
                emit(Response.Error(e.message ?: e.cause?.message ?: "Error fetching projects"))
            }
        }
    }


    override suspend fun fetchProjectById(
        id: String,
        forceRefresh: Boolean
    ): Flow<Response<Project>> {
        return flow {
            emit(Response.Loading())

            try {
                val response = apiService.fetchProjectById(
                    id = id,
                    forceRefresh = forceRefresh
                )

                emit(Response.Success(response.toProject()))

            } catch (e: HttpException) {
                return@flow emit(
                    Response.Error(
                        ErrorResponse.parseErrorBody(
                            e.response()?.errorBody()?.string()
                        ).getErrorMessage()
                    )
                )
            } catch (e: Exception) {
                emit(Response.Error(e.message ?: e.cause?.message ?: "Error fetching projects"))
            }
        }
    }

    override suspend fun fetchProjectsCentroid(forceRefresh: Boolean): Flow<Response<List<Feature>>> {
        return flow {
            emit(Response.Loading())

            try {
                val response = apiService.fetchProjectsCentroid(
                    forceRefresh = forceRefresh
                )

                val features = response.map { Feature.fromJson(it.toFeatureJsonStr()) }

                emit(Response.Success(features))

            } catch (e: HttpException) {
                return@flow emit(
                    Response.Error(
                        ErrorResponse.parseErrorBody(
                            e.response()?.errorBody()?.string()
                        ).getErrorMessage()
                    )
                )
            } catch (e: Exception) {
                emit(
                    Response.Error(
                        e.message ?: e.cause?.message ?: "Error fetching projects centroids"
                    )
                )
            }
        }
    }

}