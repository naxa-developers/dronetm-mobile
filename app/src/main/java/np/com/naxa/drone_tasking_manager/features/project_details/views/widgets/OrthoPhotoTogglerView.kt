package np.com.naxa.drone_tasking_manager.features.project_details.views.widgets

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun OrthoPhotoTogglerView(
    modifier: Modifier = Modifier,
    onToggle: (Boolean) -> Unit
) {

    var visible by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .size(24.dp)
            .clip(CircleShape)
            .clickable {
                visible = !visible
                onToggle.invoke(visible)
            },
        contentAlignment = Alignment.Center
    ) {
        Crossfade(
            targetState = visible
        ) { visible ->
            Icon(
                if (visible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                contentDescription = "Orthophoto visibility toggle icon",
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }

}