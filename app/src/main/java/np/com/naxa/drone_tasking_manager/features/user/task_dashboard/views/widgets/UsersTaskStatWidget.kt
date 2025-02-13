package np.com.naxa.drone_tasking_manager.features.user.task_dashboard.views.widgets

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import np.com.naxa.drone_tasking_manager.core.widgets.ShimmerItemPlaceHolder

@Composable
fun StatisticsCard(
    number: Int,
    label: String,
    modifier: Modifier = Modifier,
    containerColor: Color,
    isLoading: Boolean = false,
    onTap: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Card(
        modifier = modifier
            .height(80.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
            ) {
                onTap()
            },
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        if (isLoading) {
            ShimmerItemPlaceHolder()
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    modifier = Modifier
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp)
                        .fillMaxWidth(),
                    text = number.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    modifier = Modifier
                        .padding(start = 8.dp, end = 8.dp)
                        .fillMaxWidth(),
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }
    }
}