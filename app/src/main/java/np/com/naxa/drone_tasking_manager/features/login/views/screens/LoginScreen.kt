package np.com.naxa.drone_tasking_manager.features.login.views.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import np.com.naxa.drone_tasking_manager.features.login.viewmodels.events.LoginEvents
import np.com.naxa.drone_tasking_manager.features.login.views.widgets.LoginScreenWidget
import np.com.naxa.drone_tasking_manager.local_providers.LocalLoginViewModel
import np.com.naxa.drone_tasking_manager.local_providers.LocalNavigationEventsViewModel
import np.com.naxa.drone_tasking_manager.navigation.viewmodels.events.DroneTMAppNavigationEvent

@Composable
fun LoginScreen(){
    val viewModel = LocalLoginViewModel.current
    val state by viewModel.state.collectAsState()

    val navigationEventsViewModel = LocalNavigationEventsViewModel.current

    val keyboardManager = LocalSoftwareKeyboardController.current

    val role by remember { mutableStateOf("DRONE_PILOT") }

    val enableView by remember { derivedStateOf {
        !state.isLoggingIn
    } }


    if (state.isLoginSuccess != null) {
        keyboardManager?.hide()

        //trigger to fetch project list
        //and navigate to the project screen
        navigationEventsViewModel.sendEvent(DroneTMAppNavigationEvent.OnNavigateToHome)
    }



    LoginScreenWidget(
        onLoginClick = { email, password, rememberMe ->
            // Handle login
            viewModel.onEvent(LoginEvents.NormalLogin(role, email, password))
        },
        onGoogleSignInClick = {
            // Handle Google sign-in
//            launchGmailListPopup()
        },
        onForgetPasswordClick = { email ->
            // Handle forget password
//            viewModel.onEvent(LoginEvents.ForgetPassword(email))
        },
    )


}