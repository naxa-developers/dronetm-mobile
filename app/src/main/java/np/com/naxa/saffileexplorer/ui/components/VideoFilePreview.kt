package np.com.naxa.saffileexplorer.ui.components

import android.media.MediaMetadataRetriever
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun VideoFilePreview(
    file: File?,
    modifier: Modifier = Modifier,
    onThumbnailLoadFailedUi: @Composable (() -> Unit)? = null
) {
    val context = LocalContext.current
    var bitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(file) {
        val thumbnail = withContext(Dispatchers.IO) {
            return@withContext try {
                if (file != null) {
                    val retriever = MediaMetadataRetriever()
                    retriever.setDataSource(context, file.toUri())
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                        retriever.getScaledFrameAtTime(
                            0,
                            MediaMetadataRetriever.OPTION_CLOSEST,
                            200,
                            200
                        )?.asImageBitmap()
                    } else {
                        retriever.frameAtTime?.asImageBitmap()
                    }
                } else {
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        bitmap = thumbnail
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap!!,
            contentDescription = "Video Thumbnail",
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    } else {
        onThumbnailLoadFailedUi?.invoke()
    }
}