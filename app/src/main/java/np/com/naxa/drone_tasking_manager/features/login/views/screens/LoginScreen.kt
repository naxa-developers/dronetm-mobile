package np.com.naxa.drone_tasking_manager.features.login.views.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import np.com.naxa.drone_tasking_manager.events.DroneTMAppEvent
import np.com.naxa.drone_tasking_manager.features.login.viewmodels.events.LoginEvents
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
    var username by remember { mutableStateOf("testnaxa@gmail.com") }
    var password by remember { mutableStateOf("Naxa@123") }
    var passwordVisibility by remember { mutableStateOf(false) }


    if (state.isLoginSuccess != null) {
        keyboardManager?.hide()

        //trigger to fetch project list
        //and navigate to the project screen
        navigationEventsViewModel.sendEvent(DroneTMAppNavigationEvent.OnNavigateToHome)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        OutlinedTextField(
            placeholder = {
                Text(text = "Enter Username")
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            value = username,
            onValueChange = {
                username = it
            },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done
            ),
        )

        OutlinedTextField(
            placeholder = {
                Text(text = "Enter Password")

            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            value = password, onValueChange = {
                password = it
            },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done
            ),
            visualTransformation = when (passwordVisibility) {
                true -> VisualTransformation.None
                false -> PasswordVisualTransformation()
            },


            trailingIcon = {
                IconButton(onClick = {
                    passwordVisibility = !passwordVisibility
                }) {
                    when (passwordVisibility) {
                        true -> Icon(
                            imageVector = Icons.Default.Visibility,

                            contentDescription = ""
                        )

                        false -> Icon(
                            imageVector = Icons.Default.VisibilityOff,

                            contentDescription = ""
                        )
                    }
                }

            }
        )


        Spacer(modifier = Modifier.height(16.dp))


        Button(
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp),
            shape = RoundedCornerShape(15),

            onClick = {
                viewModel.onEvent(LoginEvents.NormalLogin(role, username, password))
            },

        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (state.isLoginIdle) {
                    Text(
                        text = "Login",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                } else if (state.isLoggingIn) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else if (state.isLoginSuccess != null) {
                    Text(
                        text = "Login Success",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                } else if (state.isLoginError.isNotEmpty()) {
                    Text(
                        text = "Retry Login",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                } else {
                    Text(
                        text = "Login Again",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }


            }
        }

    }

}