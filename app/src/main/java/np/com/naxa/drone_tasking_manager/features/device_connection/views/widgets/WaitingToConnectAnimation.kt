package np.com.naxa.drone_tasking_manager.features.device_connection.views.widgets

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import np.com.naxa.drone_tasking_manager.R

@Composable
fun WaitingToConnectAnimation(
    modifier: Modifier = Modifier.fillMaxSize(),
    label: String = "Waiting to connect..."
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Image(
            modifier = Modifier.align(Alignment.BottomCenter),
            painter = painterResource(id = R.drawable.drone_controller),
            contentDescription = "Drone controller image",
            alignment = Alignment.BottomCenter,
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9F)
                )
        )
        WaveAnimation()
        AnimatedText(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(24.dp),
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(
                color = MaterialTheme.colorScheme.primary
            ),
            type = TextAnimationType.Scaled
        )
    }
}