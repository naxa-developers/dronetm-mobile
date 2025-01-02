package np.com.naxa.drone_tasking_manager.ui.local_providers

import androidx.compose.runtime.compositionLocalOf
import np.com.naxa.drone_tasking_manager.viewmodel.DownloadAndTransferFileViewModel
import np.com.naxa.drone_tasking_manager.viewmodel.EventsViewModel
import np.com.naxa.drone_tasking_manager.viewmodel.NavigationEventsViewModel
import np.com.naxa.drone_tasking_manager.viewmodel.UsbDeviceViewModel

val LocalUsbDeviceViewModel = compositionLocalOf<UsbDeviceViewModel> {
    error("No ViewModel provided")
}

val LocalEventsViewModel = compositionLocalOf<EventsViewModel> {
    error("No ViewModel provided")
}

val LocalDownloadAndTransferFileViewModel = compositionLocalOf<DownloadAndTransferFileViewModel> {
    error("No ViewModel provided")
}

val LocalNavigationEventsViewModel = compositionLocalOf<NavigationEventsViewModel> {
    error("No ViewModel provided")
}