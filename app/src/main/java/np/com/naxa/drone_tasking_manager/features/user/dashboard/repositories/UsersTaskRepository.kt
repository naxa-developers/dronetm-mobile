package np.com.naxa.drone_tasking_manager.features.user.dashboard.repositories

import kotlinx.coroutines.flow.Flow
import np.com.naxa.drone_tasking_manager.core.utils.Response
import np.com.naxa.drone_tasking_manager.features.user.dashboard.models.UsersTask
import np.com.naxa.drone_tasking_manager.features.user.dashboard.models.UsersTaskStat

interface UsersTaskRepository {

    suspend fun getUsersTask(forceRefresh: Boolean = true): Flow<Response<UsersTask>>

    suspend fun getUsersTaskStat(forceRefresh: Boolean = true): Flow<Response<UsersTaskStat>>
}