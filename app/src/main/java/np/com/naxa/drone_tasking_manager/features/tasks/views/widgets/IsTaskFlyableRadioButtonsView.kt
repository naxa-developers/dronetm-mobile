package np.com.naxa.drone_tasking_manager.features.tasks.views.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import np.com.naxa.drone_tasking_manager.features.tasks.models.ProjectTask

@Composable
fun IsTaskFlyableRadioButtonsView(
    modifier: Modifier = Modifier,
    task: ProjectTask,
    onValueChange: (Boolean) -> Unit = {}
) {

    var isTaskFlyable by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .wrapContentHeight()
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                modifier = Modifier.size(24.dp),
                selected = isTaskFlyable,
                onClick = {
                    isTaskFlyable = true
                    onValueChange(true)
                }
            )
            Text("Yes")
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                modifier = Modifier.size(24.dp),
                selected = !isTaskFlyable,
                onClick = {
                    isTaskFlyable = false
                    onValueChange(false)
                }
            )
            Text("No")
        }
    }
}