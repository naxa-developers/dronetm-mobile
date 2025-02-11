package np.com.naxa.drone_tasking_manager.features.download_and_transfer.views.widgets

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DownloadAndTransferDownloadErrorStateView(
    modifier: Modifier = Modifier,
    url: String? = null,
    error: String,
    onRetry: () -> Unit = {}
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("DownloadError: $error")
            Spacer(modifier = Modifier.height(12.dp))
            if (url != null) {
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp),
                    shape = RoundedCornerShape(4.dp),
                    onClick = {
                        onRetry.invoke()
                    }
                ) {
                    Text("Retry Download")
                }
            }
        }
    }
}