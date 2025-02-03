package np.com.naxa.drone_tasking_manager.features.project_details.views.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import np.com.naxa.drone_tasking_manager.features.projects.models.Project
import np.com.naxa.drone_tasking_manager.utils.round

@Composable
fun ProjectDetailAboutTabBody(
    modifier: Modifier = Modifier,
    project: Project,
) {

    @Composable
    fun label(title: String, value: String) = Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.weight(1f), text = title,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(modifier = Modifier.weight(0.25f), text = ":")
        Text(
            modifier = Modifier.weight(1f),
            text = value,
            style = MaterialTheme.typography.titleSmall
        )
    }

    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            modifier = Modifier.padding(vertical = 10.dp),
            text = project.description ?: "",
            style = MaterialTheme.typography.bodyMedium
        )
        label(
            title = "Total Project Area",
            value = project.projectArea?.round(4)?.toString() ?: "0.0"
        )

        label(title = "Total Tasks", value = project.tasks.size.toString())

        label(title = "Project Created By", value = project.authorName ?: "")

        label(
            title = "Require Approval to Lock Task",
            value = if (project.requiresApprovalFromManagerForLocking == true) "Yes" else "No"
        )

    }
}