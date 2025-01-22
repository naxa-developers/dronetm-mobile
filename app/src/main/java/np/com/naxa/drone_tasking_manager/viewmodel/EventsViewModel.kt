package np.com.naxa.drone_tasking_manager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import np.com.naxa.drone_tasking_manager.events.DroneTMAppEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch


class EventsViewModel : ViewModel() {
    private val _appEvents = MutableSharedFlow<DroneTMAppEvent>()
    val appEvents: SharedFlow<DroneTMAppEvent> = _appEvents.asSharedFlow()


    // Function to send events
    fun sendEvent(event: DroneTMAppEvent) {
        viewModelScope.launch {
            _appEvents.emit(event)
        }
    }
}