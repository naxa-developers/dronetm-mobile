package np.com.naxa.drone_tasking_manager.features.tasks.views.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Swipe
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TakeOffPointDraggableUnDraggableToggle(
    modifier: Modifier = Modifier,
    draggable: Boolean = false,
    onToggled: ((Boolean) -> Unit)? = null
) {
    Column(
        modifier = modifier
            .wrapContentHeight()
            .wrapContentWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Box(
            modifier = modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(
                    if (draggable) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(
                        alpha = 0.2f
                    )
                )
                .clickable {
                    onToggled?.invoke(!draggable)
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                modifier = Modifier.size((24 / 1.75).dp),
                imageVector = Icons.Default.Swipe,
                contentDescription = null,
                tint = Color.White,
            )
        }
        Text(
            "Takeoff Drag",
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 9.sp
            )
        )
    }
}