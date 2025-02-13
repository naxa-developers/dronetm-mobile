package np.com.naxa.drone_tasking_manager.features.user.task_dashboard.views.widgets

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import np.com.naxa.drone_tasking_manager.features.user.task_dashboard.models.UsersTaskItem

@Composable
fun TaskRow(task: UsersTaskItem, index: Int, onTap: (UsersTaskItem) -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (index.rem(2) == 0) MaterialTheme.colorScheme.surface else Color.LightGray.copy(
                    alpha = 0.35F
                )
            )
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
            ) {
                onTap(task)
            }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Task# ${task.taskId}",
            modifier = Modifier.weight(0.1f),
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            text = "${task.projectName}",
            modifier = Modifier.weight(0.3f),
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            text = "${task.totalAreaSqkm}",
            modifier = Modifier.weight(0.2f),
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            text = "${task.createdAt}",
            modifier = Modifier.weight(0.2f),
            style = MaterialTheme.typography.bodySmall
        )
        Row(
            modifier = Modifier.weight(0.2f),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "${task.state}", style = MaterialTheme.typography.bodySmall)
        }
    }
}