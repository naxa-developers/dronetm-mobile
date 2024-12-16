package np.com.naxa.saffileexplorer.ui.screens.usb_devices

import android.hardware.usb.UsbDevice
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import np.com.naxa.saffileexplorer.utils.isMtpDevice

@Composable
fun DeviceItem(device: UsbDevice, onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(
                text = "Device: ${device.productName ?: device.deviceName}",
                fontWeight = FontWeight.Bold
            )
            Text(text = "Vendor ID: ${device.vendorId}")
            Text(text = "Product ID: ${device.productId}")
            Text(text = "isMtpDevice: ${device.isMtpDevice()}")
        }
    }
}