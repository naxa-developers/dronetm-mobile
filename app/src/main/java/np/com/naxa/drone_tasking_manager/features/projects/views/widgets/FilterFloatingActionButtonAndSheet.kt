package np.com.naxa.drone_tasking_manager.features.projects.views.widgets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

enum class ProjectFilterItem(val label: String) {
    All("All"),
    OnlyMine("Only Mine")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterFloatingActionButtonAndSheet(
    modifier: Modifier = Modifier,
    selected: ProjectFilterItem = ProjectFilterItem.All,
    visible: Boolean = true,
    onSelected: (ProjectFilterItem) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }

    AnimatedVisibility(
        modifier = modifier,
        visible = visible,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut()
    ) {
        SmallFloatingActionButton(
            onClick = {
                showBottomSheet = true
                scope.launch { sheetState.show() }
            },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(
                Icons.Default.FilterList,
                contentDescription = "Filter Projects"
            )
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            sheetState = sheetState,
            onDismissRequest = {
                showBottomSheet = false
            },
            contentWindowInsets = { WindowInsets(0.dp) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
            ) {
                Text(
                    text = "Filter Projects",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                ProjectFilterItem.entries.forEach { item ->
                    FilterOption(
                        item.label,
                        selected == item
                    ) {
                        scope.launch {
                            sheetState.hide()
                            showBottomSheet = false
                        }
                        onSelected(item)
                    }
                }
            }
        }
    }
}

@Composable
fun FilterOption(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, style = MaterialTheme.typography.bodyLarge)
    }
}