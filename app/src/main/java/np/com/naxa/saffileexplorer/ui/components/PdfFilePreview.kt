package np.com.naxa.saffileexplorer.ui.components

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
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
fun PdfFilePreview(
    file: File?,
    modifier: Modifier = Modifier,
    onPreviewLoadFailedUi: @Composable (() -> Unit)? = null
) {
    val context = LocalContext.current
    var bitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(file) {
        val thumbnail = withContext(Dispatchers.IO) {
            return@withContext try {
                if (file != null) {
                    val fileDescriptor =
                        context.contentResolver.openFileDescriptor(file.toUri(), "r")
                    val pdfRenderer =
                        PdfRenderer(ParcelFileDescriptor.dup(fileDescriptor?.fileDescriptor))
                    val pageCount = pdfRenderer.pageCount

                    val loadedBitmap = if (pageCount > 0) {
                        val page = pdfRenderer.openPage(0)
                        val pageBitmap =
                            Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
                        page.render(
                            pageBitmap,
                            null,
                            null,
                            PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
                        )
                        page.close()

                        pageBitmap
                    } else null

                    pdfRenderer.close()
                    fileDescriptor?.close()

                    loadedBitmap?.asImageBitmap()
                } else null
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
            contentDescription = null,
            modifier = modifier,
            contentScale = ContentScale.FillHeight
        )
    } else {
        onPreviewLoadFailedUi?.invoke()
    }
}