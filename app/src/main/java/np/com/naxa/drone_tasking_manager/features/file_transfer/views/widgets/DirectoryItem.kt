package np.com.naxa.drone_tasking_manager.features.file_transfer.views.widgets


import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.documentfile.provider.DocumentFile
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale


@Composable
fun DirectoryItem(
    directory: DocumentFile,
    onItemClick: (DocumentFile) -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .clickable { onItemClick(directory) },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(
                alpha = 0.05F
            )
        )
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = 12.dp,
                vertical = 12.dp
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Folder,
                contentDescription = "Directory Icon",
                modifier = Modifier.size(36.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = directory.name ?: "Unknown",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${directory.listFiles().size} files",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "  |  ",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                    val lastModifiedDate = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val instant = Instant.ofEpochMilli(directory.lastModified())
                        val zdt = instant.atZone(ZoneId.systemDefault())
                        DateTimeFormatter.ofPattern("MMM dd yyyy, hh:mm a").format(zdt)
                    } else {
                        SimpleDateFormat("MMM dd yyyy, hh:mm a", Locale.getDefault()).format(
                            Date(directory.lastModified())
                        )
                    }
                    Text(
                        text = lastModifiedDate,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}