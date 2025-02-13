package np.com.naxa.drone_tasking_manager.features.user.task_dashboard.repositories

import kotlinx.coroutines.flow.Flow
import np.com.naxa.drone_tasking_manager.core.utils.Response
import np.com.naxa.drone_tasking_manager.features.user.task_dashboard.models.UsersTask
import np.com.naxa.drone_tasking_manager.features.user.task_dashboard.models.UsersTaskStat

interface UsersTaskRepository {

    suspend fun fetchUsersTask(forceRefresh: Boolean = true): Flow<Response<UsersTask>>

    suspend fun fetchUsersTaskStat(forceRefresh: Boolean = true): Flow<Response<UsersTaskStat>>
}