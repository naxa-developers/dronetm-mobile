package np.com.naxa.drone_tasking_manager.features.tasks.views.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import np.com.naxa.drone_tasking_manager.R
import np.com.naxa.drone_tasking_manager.core.widgets.ToggleSwitcher

@Composable
fun WayPointsWayLinesSwitcher(
    modifier: Modifier = Modifier,
    onToggle: (Boolean) -> Unit,
) {
    var toggled by remember { mutableStateOf(false) }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        ToggleSwitcher(
            isToggled = toggled,
            firstDrawable = R.drawable.waypoints_24,
            secondDrawable = R.drawable.polylines_24,
            onToggle = {
                toggled = it
                onToggle(it)
            },
            vertical = false,
            thumbSize = 24.dp
        )
        Text(
            if (toggled) "Waylines" else "Waypoints",
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                letterSpacing = 0.5.sp
            )
        )
    }
}
