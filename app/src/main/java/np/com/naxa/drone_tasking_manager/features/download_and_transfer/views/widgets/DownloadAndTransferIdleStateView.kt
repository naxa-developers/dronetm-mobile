package np.com.naxa.drone_tasking_manager.features.download_and_transfer.views.widgets

import android.hardware.usb.UsbDevice
import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLink
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DownloadAndTransferIdleStateView(
    modifier: Modifier = Modifier,
    device: UsbDevice? = null,
    onDownloadClicked: (String) -> Unit,
    onPickFileClicked: () -> Unit,
    onShowUrlInputBox: () -> Unit,
    showUrlInputBox: Boolean = false
) {

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var downloadUrl by rememberSaveable {
        mutableStateOf("")
    }


    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Connected",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.LightGray.copy(
                    alpha = 0.8f
                )
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "to",
                fontSize = 14.sp,
                color = Color.LightGray.copy(
                    alpha = 0.9f
                )
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = if (device != null) "${(device.manufacturerName ?: device.deviceName)} - ${device.productName ?: ""}" else "Unknown Device",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color.LightGray
            )
            Spacer(modifier = Modifier.height(75.dp))

            when (showUrlInputBox) {
                true -> {
                    TextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = downloadUrl,
                        onValueChange = { downloadUrl = it },
                        label = { Text("Download URL") },
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            errorIndicatorColor = Color.Transparent
                        ),
                        shape = OutlinedTextFieldDefaults.shape,
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    if (clipboardManager.hasText()) {
                                        val pastedText =
                                            clipboardManager.getText().toString()
                                        if (Patterns.WEB_URL.matcher(pastedText)
                                                .matches()
                                        ) {
                                            downloadUrl = pastedText
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "Clipboard content is not a valid URL",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Nothing to paste",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            ) {
                                Icon(
                                    Icons.Default.ContentPaste,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .padding(start = 8.dp)
                                )
                            }

                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp),
                        shape = RoundedCornerShape(50),
                        onClick = {
                            onDownloadClicked.invoke(downloadUrl)
                        },
                        enabled = downloadUrl.trim()
                            .isNotBlank() && Patterns.WEB_URL.matcher(downloadUrl).matches()
                    ) {
                        Text("Download", color = Color.White)
                        Icon(
                            Icons.Default.Download,
                            contentDescription = null,
                            modifier = Modifier
                                .size(24.dp)
                                .padding(start = 8.dp),
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp),
                        shape = RoundedCornerShape(50),
                        onClick = {
                            onPickFileClicked.invoke()
                        }
                    ) {
                        Text("Pick File From Storage", color = Color.White)
                        Icon(
                            Icons.Default.UploadFile,
                            contentDescription = null,
                            modifier = Modifier
                                .size(24.dp)
                                .padding(start = 8.dp)
                        )
                    }
                }

                false -> {
                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp),
                        shape = RoundedCornerShape(50),
                        onClick = {
                            onPickFileClicked.invoke()
                        }
                    ) {
                        Text("Pick File From Storage", color = Color.White)
                        Icon(
                            Icons.Default.UploadFile,
                            contentDescription = null,
                            modifier = Modifier
                                .size(24.dp)
                                .padding(start = 8.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp),
                        shape = RoundedCornerShape(50),
                        onClick = {
                            onShowUrlInputBox.invoke()
                        }
                    ) {
                        Text("Download with Url", color = Color.White)
                        Icon(
                            Icons.Default.AddLink,
                            contentDescription = null,
                            modifier = Modifier
                                .size(24.dp)
                                .padding(start = 8.dp)
                        )
                    }
                }
            }
        }
    }
}