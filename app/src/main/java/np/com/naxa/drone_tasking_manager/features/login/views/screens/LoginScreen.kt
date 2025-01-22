package np.com.naxa.drone_tasking_manager.features.login.views.screens

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import com.google.android.gms.common.api.ApiException
import np.com.naxa.drone_tasking_manager.core.services.storage.MMKVStorageService
import np.com.naxa.drone_tasking_manager.core.services.storage.StorageKeys
import np.com.naxa.drone_tasking_manager.features.login.utils.AuthResultContract
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

    val storageService = MMKVStorageService.getInstance()

    val role by remember { mutableStateOf("DRONE_PILOT") }
    var rememberMeChecked by remember { mutableStateOf(false) }

    val enableView by remember { derivedStateOf {
        !state.isLoggingIn
    } }


    if (state.isLoginSuccess != null) {
        keyboardManager?.hide()

        //trigger to fetch project list
        //and navigate to the project screen

            storageService.save(StorageKeys.User.IS_LOGGED_IN, rememberMeChecked)

        Log.d("TAG", "LoginScreen Access Token: ${storageService.get(StorageKeys.User.ACCESS_TOKEN, "")}")
        navigationEventsViewModel.sendEvent(DroneTMAppNavigationEvent.OnNavigateToHome)
    }


    val googleLoginActivityResult = rememberLauncherForActivityResult(AuthResultContract()) {
        task ->
        try {
            val account = task?.getResult(Exception::class.java)



            if (account != null) {

                val code = account.serverAuthCode
                Log.d("TAG", "googleLoginActivityResult: $code")

                viewModel.onEvent(LoginEvents.GoogleLogin(role, code!!, account.idToken!!))
            }
        }catch (exception: ApiException){
            Log.d("TAG", "googleLoginActivityResult: ${exception.message}")
        }


    }



    LoginScreenWidget(
        onLoginClick = { email, password, rememberMe ->
            // Handle login
            rememberMeChecked = rememberMe
            viewModel.onEvent(LoginEvents.NormalLogin(role, email, password))
        },
        onGoogleSignInClick = {rememberMe ->
            // Handle Google sign-in
            rememberMeChecked = rememberMe
            googleLoginActivityResult.launch(0)
        },
        onForgetPasswordClick = { email ->
            // Handle forget password
//            viewModel.onEvent(LoginEvents.ForgetPassword(email))
        },
    )


}