package np.com.naxa.drone_tasking_manager.local_providers

import androidx.compose.runtime.compositionLocalOf
import np.com.naxa.drone_tasking_manager.features.login.viewmodels.LoginViewModel
import np.com.naxa.drone_tasking_manager.features.project_details.viewmodels.ProjectDetailViewModel
import np.com.naxa.drone_tasking_manager.features.projects.viewmodels.ProjectsViewModel
import np.com.naxa.drone_tasking_manager.features.tasks.viewmodels.TasksViewModel
import np.com.naxa.drone_tasking_manager.features.user.auth.refreshtoken.viewmodels.RefreshTokenViewModel
import np.com.naxa.drone_tasking_manager.features.user.profile.viewmodels.UserProfileViewModel
import np.com.naxa.drone_tasking_manager.navigation.viewmodels.NavigationEventsViewModel
import np.com.naxa.drone_tasking_manager.viewmodel.DownloadAndTransferFileViewModel
import np.com.naxa.drone_tasking_manager.viewmodel.EventsViewModel
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

val LocalLoginViewModel = compositionLocalOf<LoginViewModel> {
    error("No ViewModel provided")
}

val LocalProjectsViewModel = compositionLocalOf<ProjectsViewModel> {
    error("No ViewModel provided")
}

val LocalProjectDetailViewModel = compositionLocalOf<ProjectDetailViewModel> {
    error("No ViewModel provided")
}

val LocalTasksViewModel = compositionLocalOf<TasksViewModel> {
    error("No ViewModel provided")
}

val LocalUserProfileViewModel = compositionLocalOf<UserProfileViewModel> {
    error("No ViewModel provided")
}
val LocalRefreshTokenViewModel = compositionLocalOf<RefreshTokenViewModel> {
    error("No ViewModel provided")
}
