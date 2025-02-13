package np.com.naxa.drone_tasking_manager.features.user.task_dashboard.views.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import np.com.naxa.drone_tasking_manager.features.user.task_dashboard.mapper.toUsersTaskCompleted
import np.com.naxa.drone_tasking_manager.features.user.task_dashboard.mapper.toUsersTaskOnGoing
import np.com.naxa.drone_tasking_manager.features.user.task_dashboard.mapper.toUsersTaskOnUnFlyable
import np.com.naxa.drone_tasking_manager.features.user.task_dashboard.models.UsersTaskItem
import np.com.naxa.drone_tasking_manager.features.user.task_dashboard.views.widgets.StatisticsCard
import np.com.naxa.drone_tasking_manager.features.user.task_dashboard.views.widgets.TasksTable
import np.com.naxa.drone_tasking_manager.local_providers.LocalUsersTaskViewModel

@Composable
fun UsersTaskScreen(
//    ongoingTasks: Int = 81,
//    unfinableTasks: Int = 4,
//    completedTasks: Int = 5,
//    tasks: UsersTask = UsersTask()
) {

    val viewModel = LocalUsersTaskViewModel.current
    val usersTaskState by viewModel.usersTaskState.collectAsState()
    val usersTaskStatState by viewModel.usersTaskStatState.collectAsState()


//    LaunchedEffect {
//        viewModel.onEvent(UsersTaskEvents.fetchUsersTaskStat(forceRefresh = false))
//        viewModel.onEvent(UsersTaskEvents.fetchUsersTask(forceRefresh = false))
//
//    }


    val selectedColor = Color(0xFFFEF2F2)
    val unselectedColor = Color(0xFFF9FAFB)

//    val taskList = List(50) {
//        UsersTaskItem(
//            certificateUrl = "https://example.com/certificate.pdf",
//            createdAt = "2023-08-01 $it",
//            flightDistanceKm = 100.0,
//            flightTimeMinutes = 60,
//            projectId = "123 $it",
//            projectName = "Project A $it",
//            projectTaskIndex = 1,
//            registrationCertificateUrl = "https://example.com/registration_certificate.pdf",
//            state = if (it.rem(3) == 0) {
//                "IMAGE_PROCESSING_FINISHED"
//            } else if (it.rem(5) == 0) {
//                "UNFLYABLE_TASK"
//            } else {
//                "LOCKED_FOR_MAPPING"
//            },
//            taskId = "456 $it",
//            totalAreaSqkm = 500.0,
//            updatedAt = "2023-08-01"
//        )
//    }
//    tasks.addAll(taskList)

    val taskCategoryIndex = remember { mutableIntStateOf(0) }

    val ongoingTasksCount = remember { mutableIntStateOf(0) }
    val unflyableTasksCount = remember { mutableIntStateOf(0) }
    val completedTasksCount = remember { mutableIntStateOf(0) }

    var tasks = remember { mutableStateOf(listOf<UsersTaskItem>()) }

    val ongoingTasks = remember { mutableStateOf(listOf<UsersTaskItem>()) }
    val unflyableTasks = remember { mutableStateOf(listOf<UsersTaskItem>()) }
    val completedTasks = remember { mutableStateOf(listOf<UsersTaskItem>()) }


    LaunchedEffect(usersTaskStatState.usersTaskStat) {
        usersTaskStatState.usersTaskStat?.let {
            ongoingTasksCount.intValue = it.ongoingTasks ?: 0
            unflyableTasksCount.intValue = it.unflyableTasks ?: 0
            completedTasksCount.intValue = it.completedTasks ?: 0
        }
    }

    LaunchedEffect(usersTaskState.usersTask) {
        usersTaskState.usersTask.let {
            ongoingTasks.value = it?.toUsersTaskOnGoing() ?: listOf()
            unflyableTasks.value = it?.toUsersTaskOnUnFlyable() ?: listOf()
            completedTasks.value = it?.toUsersTaskCompleted() ?: listOf()

        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Statistics Cards Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp, top = 90.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatisticsCard(
                number = ongoingTasksCount.intValue,
                label = "Ongoing Tasks",
                modifier = Modifier.weight(1f),
                containerColor = if (taskCategoryIndex.intValue == 0) selectedColor else unselectedColor,
                onTap = {
                    taskCategoryIndex.intValue = 0
                },
                isLoading = usersTaskStatState.isFetching
            )
            StatisticsCard(
                number = unflyableTasksCount.intValue,
                label = "Unflyable Tasks",
                modifier = Modifier.weight(1f),
                containerColor = if (taskCategoryIndex.intValue == 1) selectedColor else unselectedColor,
                onTap = {
                    taskCategoryIndex.intValue = 1
                },
                isLoading = usersTaskStatState.isFetching
            )
            StatisticsCard(
                number = completedTasksCount.intValue,
                label = "Completed Tasks",
                modifier = Modifier.weight(1f),
                containerColor = if (taskCategoryIndex.intValue == 2) selectedColor else unselectedColor,
                onTap = {
                    taskCategoryIndex.intValue = 2
                },
                isLoading = usersTaskStatState.isFetching
            )
        }

        // Tasks Table
        Text(
            text = if (taskCategoryIndex.intValue == 0) "Ongoing Tasks"
            else if (taskCategoryIndex.intValue == 1) "Unflyable Tasks"
            else "Completed Tasks",

            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        TasksTable(
            tasks = if (taskCategoryIndex.intValue == 0) ongoingTasks.value
            else if (taskCategoryIndex.intValue == 1) unflyableTasks.value
            else completedTasks.value,

            isLoading = usersTaskState.isFetching
        )
    }
}