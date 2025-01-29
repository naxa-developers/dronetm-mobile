package np.com.naxa.drone_tasking_manager.features.project_details.views.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import np.com.naxa.drone_tasking_manager.features.projects.models.Project

@Composable
fun ProjectDetailInstructionsTabBody(
    modifier: Modifier = Modifier,
    project: Project,
) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            modifier = Modifier.padding(vertical = 10.dp),
            text = project.perTaskInstructions ?: "",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}