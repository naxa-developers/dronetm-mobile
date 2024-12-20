package np.com.naxa.saffileexplorer.ui.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import np.com.naxa.saffileexplorer.utils.asImageBitmap
import java.io.File

@Composable
fun ImageFilePreview(
    file: File?,
    modifier: Modifier = Modifier,
    onImageLoadFailedUi: @Composable (() -> Unit)? = null
) {
    var bitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(file) {
        val preview = withContext(Dispatchers.IO) {
            file?.asImageBitmap()
        }

        bitmap = preview
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap!!,
            contentDescription = null,
            modifier = modifier,
            contentScale = ContentScale.FillHeight
        )

    } else {
        onImageLoadFailedUi?.invoke()
    }
}