package np.com.naxa.drone_tasking_manager.features.project_details.views.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import np.com.naxa.drone_tasking_manager.core.widgets.SuperscriptText
import np.com.naxa.drone_tasking_manager.features.projects.models.Project
import np.com.naxa.drone_tasking_manager.features.tasks.models.ProjectTask
import np.com.naxa.drone_tasking_manager.features.tasks.models.ProjectTaskState
import np.com.naxa.drone_tasking_manager.utils.round

@Composable
fun ProjectDetailAvailableTasksTabBody(
    modifier: Modifier = Modifier,
    project: Project,
    onTaskClick: (ProjectTask) -> Unit = {}
) {

    Column(modifier = modifier.padding(12.dp)) {
        // Table Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(215, 63, 63))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier.weight(0.75f),
                text = "Id",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White,
            )
            SuperscriptText(
                modifier = Modifier.weight(1f),
                text = "Total Tasks in km",
                superscriptText = "2",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = Color.White,
                ),
                superscriptStyle = MaterialTheme.typography.labelSmall.copy(
                    color = Color.White,
                ).toSpanStyle()
            )
            Text(
                modifier = Modifier.weight(1f),
                text = "Flight Time in Minutes",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = Color.White,
                ),
            )
            Text(
                modifier = Modifier.weight(1f),
                text = "Flight Distance in km",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = Color.White,
                ),
            )
        }

        HorizontalDivider(thickness = 1.dp, color = Color.Gray)

        // Table Rows
        project.tasks.filter {
            it.state != ProjectTaskState.ImageProcessingFinished &&
                    it.state != ProjectTaskState.ImageProcessingStarted &&
                    it.state != ProjectTaskState.ImageProcessingFailed &&
                    it.state != ProjectTaskState.ImageUploaded &&
                    it.state != ProjectTaskState.LockedForMapping
        }
            .mapIndexed { i, task ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (i % 2 == 0) MaterialTheme.colorScheme.surface else Color.LightGray.copy(
                                alpha = 0.35F
                            )
                        )
                        .clickable {
                            onTaskClick.invoke(task)
                        }
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Task #${task.projectTaskIndex ?: ""}",
                        modifier = Modifier.weight(0.75f),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        task.totalAreaSqkm?.round(2)?.toString() ?: "0.0",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        task.flightTimeMinutes?.round(2)?.toString() ?: "0.0",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        task.flightDistanceKm?.round(2)?.toString() ?: "0.0",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
    }

}