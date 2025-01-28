package np.com.naxa.drone_tasking_manager.core.widgets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@Composable
fun CustomExpandable(
    modifier: Modifier = Modifier,
    content: @Composable (onToggle: () -> Unit, isExpanded: Boolean) -> Unit,
    expandable: @Composable () -> Unit,
    below: Boolean = true
) {
    var isExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
    ) {
        if (!below) {
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                expandable()
            }
        }

        content({ isExpanded = !isExpanded }, isExpanded)

        if (below) {
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                expandable()
            }
        }
    }
}