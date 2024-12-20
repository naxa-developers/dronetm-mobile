package np.com.naxa.saffileexplorer.ui.components

import androidx.compose.foundation.Image
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import np.com.naxa.saffileexplorer.utils.folderTreeBitmapIfKmzFile
import java.io.File

@Composable
fun KmzFilePreview(
    file: File?,
    modifier: Modifier = Modifier,
    onKmzPreviewLoadFailedUi: @Composable (() -> Unit)? = null
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    var bitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(file) {
        val preview = file?.folderTreeBitmapIfKmzFile(
            textColor = primaryColor,
            backgroundColor = Color.Transparent
        )
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
        onKmzPreviewLoadFailedUi?.invoke()
    }
}