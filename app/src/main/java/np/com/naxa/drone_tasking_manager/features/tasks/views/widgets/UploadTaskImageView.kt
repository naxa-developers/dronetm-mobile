package np.com.naxa.drone_tasking_manager.features.tasks.views.widgets

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import np.com.naxa.drone_tasking_manager.core.widgets.CustomAlertDialog
import np.com.naxa.drone_tasking_manager.features.tasks.models.ProjectTask

@Composable
fun UploadTaskImageView(
    modifier: Modifier = Modifier,
    task: ProjectTask
) {

    // For not allowed dialog
    var showDialog by remember { mutableStateOf(false) }

    // Allowed File Types For Upload
    val allowedMimeTypes = remember {
        arrayOf(
            "image/jpeg",
            "image/png",
            "text/plain",
            "application/octet-stream"
        )
    }

    // File Picker
    val filePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {

        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(
                shape = RoundedCornerShape(16.dp)
            )
            .drawBehind {
                val borderWidth = 2.dp.toPx()
                val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                drawRoundRect(
                    color = Color.Gray,
                    style = Stroke(width = borderWidth, pathEffect = pathEffect),
                    cornerRadius = CornerRadius(16.dp.toPx())
                )
            }
            .clickable {
                // filePickerLauncher.launch(allowedMimeTypes.joinToString(","))
                showDialog = true
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary,
                imageVector = Icons.Outlined.CloudUpload,
                contentDescription = "Upload image"
            )
            Text(
                "The supported file formats are .jpg, .jpeg, .png.\nThe GCP file should be named gcp_list.txt\nThe align file should be named align.laz",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }

        if (showDialog) {
            CustomAlertDialog(
                title = "Upload Not Allowed",
                message = "Uploading is not allowed in mobile app. Visit web dashboard to upload files.",
                dismissButtonText = "Ok",
                onDismissRequest = { showDialog = false }
            )
        }
    }
}