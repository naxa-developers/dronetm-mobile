package np.com.naxa.saffileexplorer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import np.com.naxa.saffileexplorer.events.SafAppNavigationEvent


class NavigationEventsViewModel : ViewModel() {
    private val _appEvents = MutableSharedFlow<SafAppNavigationEvent>()
    val appEvents: SharedFlow<SafAppNavigationEvent> = _appEvents.asSharedFlow()


    // Function to send events
    fun sendEvent(event: SafAppNavigationEvent) {
        viewModelScope.launch {
            _appEvents.emit(event)
        }
    }
}