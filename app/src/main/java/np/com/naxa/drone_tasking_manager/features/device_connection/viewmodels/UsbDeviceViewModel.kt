package np.com.naxa.drone_tasking_manager.features.device_connection.viewmodels

import android.hardware.usb.UsbDevice
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import np.com.naxa.drone_tasking_manager.features.device_connection.viewmodels.states.UsbDeviceState

class UsbDeviceViewModel : ViewModel() {
    private val _deviceState = MutableStateFlow<UsbDeviceState>(UsbDeviceState.WaitingToConnect)
    val deviceState: StateFlow<UsbDeviceState> = _deviceState.asStateFlow()

    /**
     * Sets the device list state to connected.
     *
     * @param device The device that was connected.
     * @param stable Whether the connection is stable.
     */
    fun connected(device: UsbDevice, stable: Boolean = false) {
        _deviceState.value = UsbDeviceState.Connected(device, stable)
    }

    /**
     * Sets the device list state to an error.
     *
     * @param message The error message.
     */
    fun error(message: String = "Error connecting to device") {
        _deviceState.value = UsbDeviceState.Error(message)
    }


    /**
     * Sets the device list state to waiting to connect.
     */
    fun waitingToConnect() {
        _deviceState.value = UsbDeviceState.WaitingToConnect
    }

    /**
     * Updates the device list state.
     *
     * @param state The new state of the device list.
     */
    fun update(state: UsbDeviceState) {
        _deviceState.value = state
    }

    /**
     * Gets the device with the specified ID, if it is connected.
     *
     * @param id The ID of the device to get.
     * @return The device with the specified ID, or null if it is not connected.
     */
    fun deviceWithId(id: Int): UsbDevice? {
        if (deviceState.value is UsbDeviceState.Connected) {
            val device = (deviceState.value as UsbDeviceState.Connected).device
            if (device.deviceId == id) return device
        }
        return null
    }
}
