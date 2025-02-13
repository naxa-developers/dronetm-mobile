package np.com.naxa.drone_tasking_manager.features.user.task_dashboard.views.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import np.com.naxa.drone_tasking_manager.features.user.task_dashboard.models.UsersTask

@Composable
fun TasksTable(tasks: UsersTask) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 1.dp
    ) {
        Column {
            // Table Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFDC2626))
                    .padding(16.dp)
            ) {
                TableHeaderCell("ID", Modifier.padding(4.dp)
                    .weight(0.1f))
                TableHeaderCell("Project Name", Modifier.padding(4.dp)
                    .weight(0.2f))
                TableHeaderCell("Total task area in km²", Modifier.padding(4.dp)
                    .weight(0.3f))
                TableHeaderCell("Created Date", Modifier.padding(4.dp)
                    .weight(0.2f))
                TableHeaderCell("Status", Modifier.padding(4.dp)
                    .weight(0.2f))
            }

            // Table Content
            LazyColumn {
                items(tasks.size) { index ->
                    TaskRow(tasks.get(index), index, onTap = {

                    })
                }
            }
        }
    }
}