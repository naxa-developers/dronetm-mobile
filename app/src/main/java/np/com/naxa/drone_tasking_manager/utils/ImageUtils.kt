package np.com.naxa.drone_tasking_manager.utils

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import np.com.naxa.drone_tasking_manager.R
import androidx.compose.ui.unit.Dp

/**
 * A composable function that displays a circular avatar image from a URL, with optional border.
 *
 * @param imageUrl The URL of the image to display.
 * @param enableBorder Whether to enable a white border around the avatar. Default is false.
 * @param size The size of the avatar. Default is 64.dp.
 * @param shape The shape of the avatar. Default is [CircleShape].
 */
@Composable
fun NetworkImageAvatar(
    imageUrl: String,
    enableBorder: Boolean = false,
    size: Dp = 64.dp,
    shape: Shape = CircleShape
) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(imageUrl)
            .crossfade(true)
            .build(),
        contentDescription = "$imageUrl avatar", // Accessibility description
        modifier = if (enableBorder) {
            Modifier
                .size(size) // Set avatar size
                .border(
                    width = 2.dp,
                    color = Color.White,
                    shape = shape
                )
        } else {
            Modifier
                .size(size) // Set avatar size
                .clip(shape)
        },


        // Clip to circle
        contentScale = ContentScale.Crop, // Crop image to fill the circle
        placeholder = painterResource(R.drawable.ic_image_placeholder), // Loading state
        error = painterResource(R.drawable.ic_image_error_avatar), // Error state
//        fallback = painterResource(R.drawable.ic_image_error_avatar),// Fallback state
    )
}


/**
 * A composable function that displays a circular avatar image from a URL, with optional border.
 *
 * @param imageUrl The URL of the image to display.
 * @param enableBorder Whether to enable a white border around the avatar. Default is false.
 * @param size The size of the avatar. Default is 64.dp.
 * @param shape The shape of the avatar. Default is [CircleShape].
 */
@Composable
fun NetworkImageAvatarRemember(
    imageUrl: String,
    enableBorder: Boolean = false,
    size: Dp = 64.dp,
    shape: Shape = CircleShape
) {
    val painter = rememberAsyncImagePainter(
        ImageRequest.Builder(LocalContext.current)
            .data(imageUrl)
            .size(coil.size.Size.ORIGINAL)
            .build()
    )

    Image(
        painter = painter,
        contentDescription = "$imageUrl avatar", // Accessibility description
        modifier = if (enableBorder) {
            Modifier
                .size(size) // Set avatar size
                .border(
                    width = 2.dp,
                    color = Color.White,
                    shape = shape
                )
        } else {
            Modifier
                .size(size) // Set avatar size
                .clip(shape)
        },
        contentScale = ContentScale.Crop,
    )
}