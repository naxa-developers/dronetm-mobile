package np.com.naxa.drone_tasking_manager.features.download_and_transfer.views.widgets

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import np.com.naxa.drone_tasking_manager.features.download_and_transfer.viewmodels.states.DownloadAndTransferState
import np.com.naxa.drone_tasking_manager.features.file_transfer.views.widgets.SwipeToDoButton
import np.com.naxa.drone_tasking_manager.local_providers.LocalDownloadAndTransferFileViewModel
import java.io.File

@Composable
fun DownloadAndTransferDownloadedStateView(
    modifier: Modifier = Modifier,
    file: File?,
    onSwipe: () -> Unit = {},
) {

    val scope = rememberCoroutineScope()
    val downloadAndTransferViewModel = LocalDownloadAndTransferFileViewModel.current

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "File: ${file?.name}",
                color = MaterialTheme.colorScheme.primary
            )
            FilePreview(
                file = file,
                onDelete = {
                    scope.launch {
                        try {
                            file?.delete()
                        } catch (e: Exception) {
                            // Do Nothing
                        } finally {
                            downloadAndTransferViewModel.update(
                                DownloadAndTransferState.Idle(
                                    false
                                )
                            )
                        }
                    }
                }
            )
            Spacer(modifier = Modifier.height(12.dp))
            SwipeToDoButton {
                onSwipe.invoke()
            }
        }
    }
}