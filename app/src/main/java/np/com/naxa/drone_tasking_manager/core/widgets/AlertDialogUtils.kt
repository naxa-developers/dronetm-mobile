package np.com.naxa.drone_tasking_manager.core.widgets

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun SimpleAlertDialog(
    showDialog: Boolean,
    alertTitle: String,
    positiveButtonText: String = "Ok",
    negativeButtonText: String = "Cancel",
    alertMessage: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { onDismiss() },
            title = {
                Text(text = alertTitle)
            },
            text = {
                Text(alertMessage)
            },
            confirmButton = {
                TextButton(onClick = { onConfirm() }) {
                    Text(positiveButtonText)
                }
            },
            dismissButton = {
                TextButton(onClick = { onDismiss() }) {
                    Text(negativeButtonText)
                }
            }
        )
    }
}
