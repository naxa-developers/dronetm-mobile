package np.com.naxa.drone_tasking_manager.features.download_and_transfer.views.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.documentfile.provider.DocumentFile
import np.com.naxa.drone_tasking_manager.features.file_transfer.views.widgets.DirectoryItem

@Composable
fun DownloadAndTransferSafDirectorySelectedStateView(
    modifier: Modifier = Modifier,
    directory: DocumentFile,
    onDirectorySelected: (DocumentFile) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            modifier = Modifier.padding(
                top = 16.dp,
                start = 16.dp,
                end = 16.dp
            ),
            text = ".../.../${directory.name}",
            style = MaterialTheme.typography.labelLarge
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(16.dp),
        ) {
            items(directory.listFiles().filter { it.isDirectory }) { directory ->
                DirectoryItem(directory) {
                    onDirectorySelected.invoke(it)
                }
            }
        }
    }
}