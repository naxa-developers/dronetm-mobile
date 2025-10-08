package np.com.naxa.drone_tasking_manager.features.project_details.views.widgets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import np.com.naxa.drone_tasking_manager.features.projects.models.Project
import np.com.naxa.drone_tasking_manager.features.tasks.models.ProjectTask

@Composable
fun ProjectDetailTabView(
    modifier: Modifier = Modifier,
    selected: Int = 0,
    onSelected: (Int) -> Unit = {},
    project: Project,
    onTaskClick: (ProjectTask) -> Unit = {}
) {

    val primaryColor = MaterialTheme.colorScheme.primary
    val tabTitles = listOf("About", "Available Tasks", "Instructions", "Contributions")

    Column(modifier = modifier) {
        SecondaryScrollableTabRow(selectedTabIndex = selected, edgePadding = 0.dp) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selected == index,
                    onClick = { onSelected.invoke(index) },
                    text = {
                        Text(
                            text = title,
                            fontWeight = FontWeight.SemiBold,
                            color = if (selected == index) primaryColor else Color.Unspecified
                        )
                    }
                )
            }
        }
        when (selected) {
            0 -> ProjectDetailAboutTabBody(
                modifier = Modifier.fillMaxWidth(),
                project = project,
            )

            1 -> ProjectDetailAvailableTasksTabBody(
                modifier = Modifier.fillMaxWidth(),
                project = project,
                onTaskClick = { onTaskClick.invoke(it) }
            )

            2 -> ProjectDetailInstructionsTabBody(
                modifier = Modifier.fillMaxWidth(),
                project = project,
            )

            3 -> ProjectDetailContributionsTabBody(
                modifier = Modifier.fillMaxWidth(),
                project = project,
                onTaskClick = { onTaskClick.invoke(it) }
            )
        }
    }
}
