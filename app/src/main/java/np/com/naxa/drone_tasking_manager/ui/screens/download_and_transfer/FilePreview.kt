package np.com.naxa.drone_tasking_manager.ui.screens.download_and_transfer

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FilePresent
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import np.com.naxa.drone_tasking_manager.ui.components.ImageFilePreview
import np.com.naxa.drone_tasking_manager.ui.components.KmzFilePreview
import np.com.naxa.drone_tasking_manager.ui.components.PdfFilePreview
import np.com.naxa.drone_tasking_manager.ui.components.VideoFilePreview
import java.io.File

/**
 * A composable function that displays a preview of a file within a card.
 *
 * @param modifier The [Modifier] to be applied to the card.
 * @param file The [File] to be displayed as a preview.
 */
@Composable
fun FilePreview(
    modifier: Modifier = Modifier,
    file: File?,
    onDelete: () -> Unit = {}
) {
    val fileExtension = file?.extension?.lowercase()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        shape = RoundedCornerShape(8.dp),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            when (fileExtension) {
                "jpg", "jpeg", "png", "gif", "bmp", "tiff" -> {
                    ImageFilePreview(
                        file = file,
                        modifier = Modifier.fillMaxSize(),
                        onImageLoadFailedUi = {
                            IconPreview(Icons.Default.Image)
                        }
                    )
                }

                "pdf" -> {
                    PdfFilePreview(
                        file = file,
                        modifier = Modifier.fillMaxSize(),
                        onPreviewLoadFailedUi = {
                            IconPreview(Icons.Default.PictureAsPdf)
                        }
                    )
                }

                "mp4", "avi", "mkv", "mov", "wmv", "flv", "webm" -> {
                    VideoFilePreview(
                        file = file,
                        modifier = Modifier.fillMaxSize(),
                        onThumbnailLoadFailedUi = {
                            IconPreview(Icons.Default.VideoFile)
                        }
                    )
                }

                "kmz" -> {
                    KmzFilePreview(
                        file = file,
                        modifier = Modifier.fillMaxSize(),
                        onKmzPreviewLoadFailedUi = {
                            IconPreview(Icons.Default.FilePresent)
                        }
                    )
                }

                "apk" -> {
                    IconPreview(Icons.Default.Android)
                }

                "mp3", "m4a", "wav", "aac", "ogg", "flac" -> {
                    IconPreview(Icons.Default.AudioFile)
                }

                "zip" -> {
                    IconPreview(Icons.Default.FolderZip)
                }

                null -> {
                    IconPreview(Icons.Default.ErrorOutline)
                }

                else -> {
                    IconPreview(Icons.Default.FilePresent)
                }
            }

            // Reset / Remove file ui
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .clip(RoundedCornerShape(bottomStart = 8.dp, topEnd = 8.dp))
                    .background(MaterialTheme.colorScheme.inversePrimary)
                    .clickable { onDelete.invoke() }
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    modifier = Modifier.size(18.dp),
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(
                        color = Color.White
                    ),
                )
            }
        }
    }
}


/**
 * A composable function that renders an icon preview within a [BoxScope].
 *
 * @param imageVector The [ImageVector] to be displayed as the icon preview.
 */
@Composable
private fun BoxScope.IconPreview(
    imageVector: ImageVector
) {
    Image(
        imageVector = imageVector,
        contentDescription = "Icon preview",
        modifier = Modifier
            .fillMaxSize(0.5f)
            .align(Alignment.Center)
    )
}