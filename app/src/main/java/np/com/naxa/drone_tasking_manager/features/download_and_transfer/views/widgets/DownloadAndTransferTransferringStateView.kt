package np.com.naxa.drone_tasking_manager.features.download_and_transfer.views.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DownloadAndTransferTransferringStateView(
    modifier: Modifier = Modifier,
    progress: Float = 0f
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(220.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .fillMaxSize(),
                    progress = { progress },
                    strokeCap = StrokeCap.Round,
                    strokeWidth = 20.dp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = buildAnnotatedString {
                        append(
                            (progress * 100).toString().subSequence(
                                0,
                                if (progress.toString().length >= 4) 4 else 1
                            )
                        )
                        withStyle(
                            style = SpanStyle(
                                fontSize = MaterialTheme.typography.headlineLarge.fontSize * 0.60f,
                                color = MaterialTheme.colorScheme.primary.copy(
                                    alpha = 0.65f
                                )
                            )
                        ) {
                            append("%")
                        }
                    },
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.primary
                )

            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Transferring...",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}