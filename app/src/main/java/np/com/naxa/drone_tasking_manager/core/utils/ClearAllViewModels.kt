package np.com.naxa.drone_tasking_manager.core.utils

import androidx.lifecycle.ViewModelStoreOwner

fun clearAllViewModels( viewModelStoreOwner: ViewModelStoreOwner){
    val viewModelStore = viewModelStoreOwner.viewModelStore
    viewModelStore.clear()
}