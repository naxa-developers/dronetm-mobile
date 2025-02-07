package np.com.naxa.drone_tasking_manager.features.tasks.views.widgets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Swipe
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

enum class TakeOffPointChangeOptions(val label: String, val icon: ImageVector) {
    CurrentLocation("Current Location", Icons.Default.MyLocation),
    Drag("Drag Take Off Point", Icons.Default.Swipe)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TakeOffPointChangeOptionsBottomSheet(
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false),
    show: Boolean,
    onDismiss: () -> Unit = {},
    onOptionSelected: (TakeOffPointChangeOptions) -> Unit
) {
    if (show) {
        ModalBottomSheet(
            modifier = modifier
                .wrapContentHeight(),
            sheetState = sheetState,
            onDismissRequest = {
                onDismiss.invoke()
            }
        ) {
            Column {
                TakeOffPointChangeOptions.entries.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onOptionSelected.invoke(option)
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            modifier = Modifier.size(14.dp),
                            imageVector = option.icon,
                            contentDescription = null,
                        )
                        Text(
                            modifier = Modifier.weight(1f),
                            text = option.label,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}