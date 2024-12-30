package np.com.naxa.saffileexplorer.ui.local_providers

import androidx.compose.runtime.compositionLocalOf
import np.com.naxa.saffileexplorer.viewmodel.DownloadAndTransferFileViewModel
import np.com.naxa.saffileexplorer.viewmodel.EventsViewModel
import np.com.naxa.saffileexplorer.viewmodel.NavigationEventsViewModel
import np.com.naxa.saffileexplorer.viewmodel.UsbDeviceListViewModel

val LocalUsbDeviceListViewModel = compositionLocalOf<UsbDeviceListViewModel> {
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