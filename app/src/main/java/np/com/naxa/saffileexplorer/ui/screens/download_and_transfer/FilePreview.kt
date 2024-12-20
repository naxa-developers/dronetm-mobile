package np.com.naxa.saffileexplorer.ui.screens.download_and_transfer

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FilePresent
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import np.com.naxa.saffileexplorer.ui.components.ImageFilePreview
import np.com.naxa.saffileexplorer.ui.components.KmzFilePreview
import np.com.naxa.saffileexplorer.ui.components.PdfFilePreview
import np.com.naxa.saffileexplorer.ui.components.VideoFilePreview
import java.io.File

/**
 * A composable function that displays a preview of a file within a card.
 *
 * @param modifier The [Modifier] to be applied to the card.
 * @param file The [File] to be displayed as a preview.
 */
@Composable
fun FilePreview(modifier: Modifier = Modifier, file: File?) {
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