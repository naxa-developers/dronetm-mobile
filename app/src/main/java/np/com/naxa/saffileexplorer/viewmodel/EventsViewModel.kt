package np.com.naxa.saffileexplorer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import np.com.naxa.saffileexplorer.events.SafFileExplorerAppEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch


class EventsViewModel : ViewModel() {
    private val _appEvents = MutableSharedFlow<SafFileExplorerAppEvent>()
    val appEvents: SharedFlow<SafFileExplorerAppEvent> = _appEvents.asSharedFlow()


    // Function to send events
    fun sendEvent(event: SafFileExplorerAppEvent) {
        viewModelScope.launch {
            _appEvents.emit(event)
        }
    }
}