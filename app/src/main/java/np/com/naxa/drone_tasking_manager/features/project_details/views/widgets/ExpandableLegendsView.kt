package np.com.naxa.drone_tasking_manager.features.project_details.views.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import np.com.naxa.drone_tasking_manager.core.widgets.CustomExpandable
import np.com.naxa.drone_tasking_manager.features.projects.models.ProjectTaskState

@Composable
fun ExpandableLegendsView(
    modifier: Modifier = Modifier
) {
    CustomExpandable(
        modifier = modifier,
        below = false,
        content = { onToggle, isExpanded ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(interactionSource = null, indication = null) { onToggle.invoke() }
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = "Legend",
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ArrowDropDown else Icons.Default.ArrowDropUp,
                    contentDescription = null
                )
            }
        },
        expandable = {
            Column(
                modifier = Modifier.padding(
                    start = 8.dp,
                    end = 8.dp,
                    top = 8.dp
                ),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                ProjectTaskState.entries.shuffled().forEach { state ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(state.color, RectangleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (state == ProjectTaskState.LockedForMapping) {
                                Icon(
                                    modifier = Modifier.size(8.dp),
                                    imageVector = Icons.Outlined.Lock,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }
                        Text(
                            modifier = Modifier.padding(start = 8.dp),
                            text = state.label,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.W300
                            )
                        )
                    }
                }
            }
        }
    )
}