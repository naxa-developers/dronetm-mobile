package np.com.naxa.saffileexplorer.viewmodel

import android.hardware.usb.UsbDevice
import androidx.lifecycle.ViewModel
import np.com.naxa.saffileexplorer.states.UsbDeviceListState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UsbDeviceListViewModel : ViewModel() {
    private val _deviceListState = MutableStateFlow<UsbDeviceListState>(UsbDeviceListState.Loading)
    val deviceListState: StateFlow<UsbDeviceListState> = _deviceListState.asStateFlow()

    fun updateDevices(lst: List<UsbDevice>) {
        _deviceListState.value = UsbDeviceListState.Success(lst)
    }

    fun update(state: UsbDeviceListState) {
        _deviceListState.value = state
    }

    fun deviceOf(id: Int): UsbDevice? {
        return (_deviceListState.value as? UsbDeviceListState.Success)?.devices?.find {
            it.deviceId == id
        }
    }
}
