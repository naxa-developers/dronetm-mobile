package np.com.naxa.drone_tasking_manager.ui.screens.download_and_transfer

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.DialogProperties
import java.io.File

@Composable
fun SafDirectoryAccessDialog(
    onDismissRequest: () -> Unit,
    onRequest: (File?) -> Unit,
    onCancel: (File?) -> Unit,
    file: File? = null
) {

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text("Storage Access Required")
        },
        text = {
            Text(
                "This app needs access to the DJI controller's storage to transfer files. " +
                        "Please select the 'Android/data/com.itheamc.djiapp/files' directory in the next screen."
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onRequest(file)
                }
            ) {
                Text("Grant Access")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onCancel(file)
                }
            ) {
                Text("Cancel")
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    )

}
