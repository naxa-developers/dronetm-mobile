package np.com.naxa.drone_tasking_manager.features.file_transfer.views.widgets

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilePresent
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import np.com.naxa.drone_tasking_manager.core.widgets.KmzFilePreview
import np.com.naxa.drone_tasking_manager.ui.screens.download_and_transfer.SwipeToDoButton
import java.io.File

@Composable
fun FileTransferIdleStateView(
    modifier: Modifier = Modifier,
    file: File? = null,
    onSwipeToTransfer: () -> Unit
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "File: ${file?.name ?: ""}",
                color = MaterialTheme.colorScheme.primary
            )
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 2.dp
                ),
                shape = RoundedCornerShape(8.dp),
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    KmzFilePreview(
                        file = file,
                        modifier = Modifier.fillMaxSize(),
                        onKmzPreviewLoadFailedUi = {
                            Image(
                                imageVector = Icons.Default.FilePresent,
                                contentDescription = "Icon preview",
                                modifier = Modifier
                                    .fillMaxSize(0.5f)
                                    .align(Alignment.Center)
                            )
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            SwipeToDoButton {
                if (file != null) {
                    onSwipeToTransfer.invoke()
                    return@SwipeToDoButton
                }
            }
        }
    }
}