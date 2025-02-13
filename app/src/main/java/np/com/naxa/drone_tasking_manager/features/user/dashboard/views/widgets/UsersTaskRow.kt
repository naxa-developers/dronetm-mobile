package np.com.naxa.drone_tasking_manager.features.user.dashboard.views.widgets
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import np.com.naxa.drone_tasking_manager.features.user.dashboard.models.UsersTaskItem

@Composable
fun TaskRow(task: UsersTaskItem, index:Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (index.rem(2) == 0) Color(0xFFF3F4F6) else Color.White)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "Task# ${task.taskId}", modifier = Modifier.weight(0.1f))
        Text(text = "${task.projectName}", modifier = Modifier.weight(0.3f))
        Text(text = "${task.totalAreaSqkm}", modifier = Modifier.weight(0.2f))
        Text(text = "${task.createdAt}", modifier = Modifier.weight(0.2f))
        Row(
            modifier = Modifier.weight(0.2f),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "${task.state}")
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = Color.Gray
            )
        }
    }
}