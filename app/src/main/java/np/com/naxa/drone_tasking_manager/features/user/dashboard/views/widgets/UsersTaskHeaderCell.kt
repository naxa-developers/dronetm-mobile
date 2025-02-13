package np.com.naxa.drone_tasking_manager.features.user.dashboard.views.widgets

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun TableHeaderCell(text: String, weight: Float) {
    Text(
        text = text,
        color = Color.White,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(8.dp)
    )
}