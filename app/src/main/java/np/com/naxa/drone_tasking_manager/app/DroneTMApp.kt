package np.com.naxa.drone_tasking_manager.app

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LineStyle
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import np.com.naxa.drone_tasking_manager.R
import np.com.naxa.drone_tasking_manager.core.services.storage.MMKVStorageService
import np.com.naxa.drone_tasking_manager.core.theme.DroneTMAppTheme
import np.com.naxa.drone_tasking_manager.core.utils.clearAllViewModels
import np.com.naxa.drone_tasking_manager.features.file_transfer.viewmodels.FileTransferViewModel
import np.com.naxa.drone_tasking_manager.features.login.viewmodels.LoginViewModel
import np.com.naxa.drone_tasking_manager.features.project_details.viewmodels.ProjectDetailViewModel
import np.com.naxa.drone_tasking_manager.features.projects.viewmodels.ProjectsViewModel
import np.com.naxa.drone_tasking_manager.features.tasks.viewmodels.TasksViewModel
import np.com.naxa.drone_tasking_manager.features.user.auth.refreshtoken.viewmodels.RefreshTokenViewModel
import np.com.naxa.drone_tasking_manager.features.user.profile.viewmodels.UserProfileViewModel
import np.com.naxa.drone_tasking_manager.local_providers.LocalDownloadAndTransferFileViewModel
import np.com.naxa.drone_tasking_manager.local_providers.LocalEventsViewModel
import np.com.naxa.drone_tasking_manager.local_providers.LocalFileTransferViewModel
import np.com.naxa.drone_tasking_manager.local_providers.LocalLoginViewModel
import np.com.naxa.drone_tasking_manager.local_providers.LocalNavigationEventsViewModel
import np.com.naxa.drone_tasking_manager.local_providers.LocalProjectDetailViewModel
import np.com.naxa.drone_tasking_manager.local_providers.LocalProjectsViewModel
import np.com.naxa.drone_tasking_manager.local_providers.LocalRefreshTokenViewModel
import np.com.naxa.drone_tasking_manager.local_providers.LocalTasksViewModel
import np.com.naxa.drone_tasking_manager.local_providers.LocalUsbDeviceViewModel
import np.com.naxa.drone_tasking_manager.local_providers.LocalUserProfileViewModel
import np.com.naxa.drone_tasking_manager.navigation.DroneTMAppNavHost
import np.com.naxa.drone_tasking_manager.navigation.routes.Routes
import np.com.naxa.drone_tasking_manager.navigation.viewmodels.NavigationEventsViewModel
import np.com.naxa.drone_tasking_manager.navigation.viewmodels.events.DroneTMAppNavigationEvent
import np.com.naxa.drone_tasking_manager.states.DownloadAndTransferState
import np.com.naxa.drone_tasking_manager.utils.route
import np.com.naxa.drone_tasking_manager.viewmodel.DownloadAndTransferFileViewModel
import np.com.naxa.drone_tasking_manager.viewmodel.EventsViewModel
import np.com.naxa.drone_tasking_manager.viewmodel.UsbDeviceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DroneTMApp(
    deviceViewModel: UsbDeviceViewModel,
    eventsViewModel: EventsViewModel,
    downloadAndTransferViewModel: DownloadAndTransferFileViewModel,
    navigationEventsViewModel: NavigationEventsViewModel
) {

    val navController = rememberNavController()

    val snackBarHostState = remember { SnackbarHostState() }

    var menuExpanded by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    val backStackEntry by navController.currentBackStackEntryFlow.collectAsState(initial = null)
    val downloadAndTransferState by downloadAndTransferViewModel.downloadAndTransferState.collectAsState()

    val currentRoute by remember {
        derivedStateOf {
            backStackEntry?.destination?.route?.route()
        }
    }

    val viewModels = LocalViewModelStoreOwner.current!!
    val context = LocalContext.current

    val loginViewModel = hiltViewModel<LoginViewModel>()
    val projectsViewModel = hiltViewModel<ProjectsViewModel>()
    val projectDetailViewModel = hiltViewModel<ProjectDetailViewModel>()
    val tasksViewModel = hiltViewModel<TasksViewModel>()
    val userProfileViewModel = hiltViewModel<UserProfileViewModel>()
    val refreshTokenViewModel = hiltViewModel<RefreshTokenViewModel>()
    val fileTransferViewModel = hiltViewModel<FileTransferViewModel>()


    val topBarTitle by remember {
        derivedStateOf {
            if (currentRoute == Routes.DownloadAndTransfer) {
                when (downloadAndTransferState) {
                    is DownloadAndTransferState.DownloadCompleted -> "Swipe to Transfer"
                    is DownloadAndTransferState.DownloadError -> "Download Error"
                    is DownloadAndTransferState.Downloading -> "Downloading"
                    is DownloadAndTransferState.Idle -> "Download and Transfer"
                    is DownloadAndTransferState.SafDirectorySelected -> "Select Directory"
                    is DownloadAndTransferState.TransferCompleted -> "Transferred"
                    is DownloadAndTransferState.TransferError -> "TransferError"
                    is DownloadAndTransferState.Transferring -> "Transferring"
                }
            } else {
                currentRoute?.label ?: ""
            }
        }
    }

    LaunchedEffect(Unit) {
        navigationEventsViewModel.appEvents.collect { event ->
            when (event) {

                is DroneTMAppNavigationEvent.OnNavigateToDownloadAndTransfer -> {
                    navController.navigate(
                        Routes.DownloadAndTransfer.path.replace(
                            "{deviceId}",
                            event.device?.deviceId.toString()
                        )
                    )
                }

                DroneTMAppNavigationEvent.OnNavigateToHome -> {
                    navController.navigate(Routes.Home.path) {
                        popUpTo(Routes.Splash.path) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }

                is DroneTMAppNavigationEvent.OnSnackBarShow -> {
                    scope.launch {
                        val result = snackBarHostState.showSnackbar(
                            message = event.message,
                            actionLabel = event.actionLabel,
                            withDismissAction = event.withDismissAction,
                            duration = event.duration,
                        )
                        when (result) {
                            SnackbarResult.ActionPerformed -> {
                                event.onActionPerformed()
                            }

                            SnackbarResult.Dismissed -> {
                                event.onDismissed()
                            }
                        }
                    }
                }

                DroneTMAppNavigationEvent.OnPopBackStack -> {
                    navController.popBackStack()
                }

                DroneTMAppNavigationEvent.OnNavigateToLogin -> {
                    navController.navigate(Routes.Login.path) {
                        popUpTo(Routes.Splash.path) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }

                DroneTMAppNavigationEvent.OnNavigateToProjects -> {
                    navController.navigate(Routes.Projects.path) {
                        popUpTo(Routes.Login.path) {
                            inclusive = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }

                is DroneTMAppNavigationEvent.OnNavigateToProjectDetail -> {

                    if (currentRoute?.path != Routes.ProjectDetails.path) {
                        navController.navigate(
                            Routes.ProjectDetails.path.replace(
                                "{id}",
                                event.id
                            )
                        )
                    }
                }

                is DroneTMAppNavigationEvent.OnNavigateToTaskDetail -> {
                    if (currentRoute?.path != Routes.TaskDetails.path) {
                        navController.navigate(
                            Routes.TaskDetails.path
                                .replace(
                                    "{id}",
                                    event.id
                                )
                                .replace(
                                    "{project}",
                                    event.project ?: ""
                                )
                        )
                    }
                }

                is DroneTMAppNavigationEvent.OnNavigateToFileTransfer -> {
                    navController.navigate(
                        Routes.FileTransfer.path.replace(
                            "{filePath}",
                            Uri.encode(event.filePath)
                        )
                    )
                }

                DroneTMAppNavigationEvent.OnNavigateToProfileScreen -> {

                    navController.navigate(Routes.ProfileScreen.path)
                }
            }
        }
    }

    CompositionLocalProvider(
        LocalUsbDeviceViewModel provides deviceViewModel,
        LocalEventsViewModel provides eventsViewModel,
        LocalDownloadAndTransferFileViewModel provides downloadAndTransferViewModel,
        LocalNavigationEventsViewModel provides navigationEventsViewModel,
        LocalLoginViewModel provides loginViewModel,
        LocalProjectsViewModel provides projectsViewModel,
        LocalProjectDetailViewModel provides projectDetailViewModel,
        LocalTasksViewModel provides tasksViewModel,
        LocalUserProfileViewModel provides userProfileViewModel,
        LocalRefreshTokenViewModel provides refreshTokenViewModel,
        LocalFileTransferViewModel provides fileTransferViewModel
    ) {
        DroneTMAppTheme {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                snackbarHost = {
                    SnackbarHost(snackBarHostState) {
                        Snackbar(
                            it,
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                topBar = {
                    if (currentRoute != Routes.Splash
                        && currentRoute != Routes.Home
                        && currentRoute != Routes.Login
                        && currentRoute != Routes.ProjectsMap
                        && currentRoute != Routes.ProjectDetails
                        && currentRoute != Routes.TaskDetails
                    ) {
                        CenterAlignedTopAppBar(
                            title = {
                                Text(topBarTitle)
                            },
                            navigationIcon = {
                                if (currentRoute != null && currentRoute != Routes.ProjectsList) {
                                    IconButton(
                                        onClick = {
                                            if (currentRoute == Routes.DownloadAndTransfer) {
                                                if (downloadAndTransferViewModel.downloadAndTransferState.value !is DownloadAndTransferState.Idle) {
                                                    if (downloadAndTransferViewModel.downloadAndTransferState.value is DownloadAndTransferState.SafDirectorySelected) {
                                                        if (downloadAndTransferViewModel.file != null) {
                                                            downloadAndTransferViewModel.update(
                                                                DownloadAndTransferState.DownloadCompleted(
                                                                    downloadAndTransferViewModel.file
                                                                )
                                                            )
                                                            return@IconButton
                                                        }
                                                    }

                                                    downloadAndTransferViewModel.update(
                                                        DownloadAndTransferState.Idle(false)
                                                    )
                                                    return@IconButton
                                                }

                                                val isShowingUrlInputBox =
                                                    (downloadAndTransferViewModel.downloadAndTransferState.value as DownloadAndTransferState.Idle).showUrlInputBox

                                                if (isShowingUrlInputBox) {
                                                    downloadAndTransferViewModel.update(
                                                        DownloadAndTransferState.Idle(false)
                                                    )
                                                    return@IconButton
                                                }

                                                navigationEventsViewModel.sendEvent(
                                                    DroneTMAppNavigationEvent.OnPopBackStack
                                                )


                                            } else {
                                                navController.popBackStack()
                                            }
                                        }) {
                                        Icon(
                                            Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = null
                                        )
                                    }
                                }
                            },

                            actions = {
                                if (currentRoute == Routes.ProjectsList) {
                                    IconButton(onClick = {
                                        menuExpanded = !menuExpanded
                                    }) {
                                        Surface(
                                            shape = CircleShape,
                                            modifier = Modifier
                                                .size(24.dp)
                                                .padding(bottom = 0.dp),
                                            color = Color.Red
                                        ) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.ic_drone_operator_icon_24),
                                                contentDescription = "Profile",
                                                tint = Color.White,
                                                modifier = Modifier.padding(4.dp),
                                            )
                                        }
                                    }
                                }


                                // Dropdown menu
                                DropdownMenu(
                                    expanded = menuExpanded,
                                    onDismissRequest = { menuExpanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Profile") },
                                        onClick = {
                                            onProfileClick(navigationEventsViewModel)
                                            menuExpanded = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Logout") },
                                        onClick = {

                                            onLogout(navigationEventsViewModel, viewModels, context)
                                            menuExpanded = false
                                        }
                                    )
                                }

                            },
                        )
                    }
                },
                bottomBar = {
                    if (currentRoute == Routes.Projects || currentRoute == Routes.ProjectsList || currentRoute == Routes.ProjectsMap) {
                        NavigationBar {
                            listOf(Routes.ProjectsList, Routes.ProjectsMap).forEach { route ->
                                NavigationBarItem(
                                    selected = currentRoute == route,
                                    onClick = {
                                        navController.navigate(route.path) {
                                            // Pop up to the parent navigation (Projects)
                                            popUpTo(Routes.Projects.path) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = {
                                        Icon(
                                            if (route == Routes.ProjectsMap) Icons.Filled.Map else Icons.Filled.LineStyle,
                                            contentDescription = null
                                        )
                                    },
                                    label = { Text(route.label) },
                                    alwaysShowLabel = true,
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                DroneTMAppNavHost(
                    modifier = Modifier.padding(
                        if (currentRoute != Routes.Splash &&
                            currentRoute != Routes.ProjectsMap &&
                            currentRoute != Routes.ProjectDetails &&
                            currentRoute != Routes.TaskDetails
                        ) innerPadding else PaddingValues(
                            0.dp
                        )
                    ),
                    navHostController = navController,
                )
            }
        }

    }
}

fun onLogout(
    navigationEventsViewModel: NavigationEventsViewModel,
    viewModels: ViewModelStoreOwner,
    context: Context
) {

    MMKVStorageService.getInstance().clear()

    clearAllViewModels(viewModels)

    //restart the app
    val packageManager: PackageManager = context.packageManager
    val intent: Intent = packageManager.getLaunchIntentForPackage(context.packageName)!!
    val componentName: ComponentName = intent.component!!
    val restartIntent: Intent = Intent.makeRestartActivityTask(componentName)
    context.startActivity(restartIntent)
    Runtime.getRuntime().exit(0)

//    navigationEventsViewModel.sendEvent(
//        DroneTMAppNavigationEvent.OnNavigateToLogin
//    )

}

fun onProfileClick(navigationEventsViewModel: NavigationEventsViewModel) {
    navigationEventsViewModel.sendEvent(
        DroneTMAppNavigationEvent.OnNavigateToProfileScreen
    )
}
