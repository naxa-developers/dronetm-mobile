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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import np.com.naxa.drone_tasking_manager.features.user.task_dashboard.models.UsersTask
import np.com.naxa.drone_tasking_manager.features.user.task_dashboard.models.UsersTaskItem
import np.com.naxa.drone_tasking_manager.features.user.task_dashboard.views.widgets.StatisticsCard
import np.com.naxa.drone_tasking_manager.features.user.task_dashboard.views.widgets.TasksTable

@Composable
fun UsersTaskScreen(
    ongoingTasks: Int = 81,
    unfinableTasks: Int = 4,
    completedTasks: Int = 5,
    tasks: UsersTask = UsersTask()
) {
    val taskList = List(50){
        UsersTaskItem(
            certificateUrl = "https://example.com/certificate.pdf",
            createdAt = "2023-08-01 $it",
            flightDistanceKm = 100.0,
            flightTimeMinutes = 60,
            projectId = "123 $it",
            projectName = "Project A $it",
            projectTaskIndex = 1,
            registrationCertificateUrl = "https://example.com/registration_certificate.pdf",
            state = if(it.rem(3) ==0 ) {"IMAGE_PROCESSING_FINISHED"}
            else if(it.rem(5) ==0) {"UNFLYABLE_TASK"}else{"LOCKED_FOR_MAPPING"
            },
            taskId = "456 $it",
            totalAreaSqkm = 500.0,
            updatedAt = "2023-08-01"
        )
    }
   tasks.addAll(taskList)


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
                number = ongoingTasks,
                label = "Ongoing Tasks",
                modifier = Modifier.weight(1f),
                containerColor = Color(0xFFFEF2F2)
            )
            StatisticsCard(
                number = unfinableTasks,
                label = "Unflyable Tasks",
                modifier = Modifier.weight(1f),
                containerColor = Color(0xFFF9FAFB)
            )
            StatisticsCard(
                number = completedTasks,
                label = "Completed Tasks",
                modifier = Modifier.weight(1f),
                containerColor = Color(0xFFF9FAFB)
            )
        }

        // Tasks Table
        Text(
            text = "Ongoing Tasks",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        TasksTable(tasks)
    }
}