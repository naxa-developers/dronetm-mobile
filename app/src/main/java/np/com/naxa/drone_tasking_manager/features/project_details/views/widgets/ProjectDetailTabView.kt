package np.com.naxa.drone_tasking_manager.features.project_details.views.widgets

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import np.com.naxa.drone_tasking_manager.features.projects.models.Project

@Composable
fun ProjectDetailTabView(
    modifier: Modifier = Modifier,
    project: Project,
) {

    val primaryColor = MaterialTheme.colorScheme.primary
    var selected by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("About", "Available Tasks", "Instructions", "Contributions")

    Column(modifier = modifier) {
        ScrollableTabRow(selectedTabIndex = selected, edgePadding = 0.dp) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selected == index,
                    onClick = { selected = index },
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
            )
            2 -> ProjectDetailInstructionsTabBody(
                modifier = Modifier.fillMaxWidth(),
                project = project,
            )
            3 -> ProjectDetailContributionsTabBody(
                modifier = Modifier.fillMaxWidth(),
                project = project,
            )
        }
    }
}
