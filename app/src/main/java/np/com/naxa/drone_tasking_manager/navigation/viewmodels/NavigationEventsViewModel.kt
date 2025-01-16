package np.com.naxa.drone_tasking_manager.navigation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import np.com.naxa.drone_tasking_manager.navigation.viewmodels.events.DroneTMAppNavigationEvent


class NavigationEventsViewModel : ViewModel() {
    private val _appEvents = MutableSharedFlow<DroneTMAppNavigationEvent>()
    val appEvents: SharedFlow<DroneTMAppNavigationEvent> = _appEvents.asSharedFlow()


    // Function to send events
    fun sendEvent(event: DroneTMAppNavigationEvent) {
        viewModelScope.launch {
            _appEvents.emit(event)
        }
    }
}