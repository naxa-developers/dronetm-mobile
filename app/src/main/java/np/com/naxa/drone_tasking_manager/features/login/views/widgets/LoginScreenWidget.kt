package np.com.naxa.drone_tasking_manager.features.login.views.widgets

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberBasicTooltipState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RichTooltip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TooltipDefaults.caretSize
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import np.com.naxa.drone_tasking_manager.R
import np.com.naxa.drone_tasking_manager.core.theme.PrimaryColor
import np.com.naxa.drone_tasking_manager.local_providers.LocalLoginViewModel
import np.com.naxa.drone_tasking_manager.local_providers.LocalNavigationEventsViewModel
import np.com.naxa.drone_tasking_manager.navigation.viewmodels.events.DroneTMAppNavigationEvent
import np.com.naxa.drone_tasking_manager.utils.widgets.NavigateToTransferFileWidget

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun LoginScreenWidget(
    onLoginClick: (String, String, Boolean) -> Unit,
    onGoogleSignInClick: (Boolean) -> Unit,
    onForgetPasswordClick: (String) -> Unit,
) {

    val viewModel = LocalLoginViewModel.current
    val state by viewModel.state.collectAsState()

    val navigationEventsViewModel = LocalNavigationEventsViewModel.current

    var email by remember { mutableStateOf("testnaxa@gmail.com") }
    var password by remember { mutableStateOf("Naxa@123") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(false) }

    val tooltipPosition = TooltipDefaults.rememberPlainTooltipPositionProvider()
    val tooltipState = rememberTooltipState(isPersistent = true)
    val scope = rememberCoroutineScope()

    val enableView by remember {
        derivedStateOf {
            !state.isLoggingIn
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize(),
    ) {

//        Box(
//            modifier = Modifier
//                .fillMaxWidth()
//                .align(Alignment.TopEnd)
//                .padding(end = 16.dp, top = 32.dp),
//            contentAlignment = Alignment.TopEnd
//        ) {
//            TooltipBox(
//                modifier = Modifier
//                    .align(Alignment.TopEnd),
//                positionProvider = tooltipPosition,
//                tooltip = {
//                    RichTooltip(
//                        title = { Text("Transfer File?") },
//                        caretSize = caretSize,
//                        action = {
//                            TextButton(onClick = {
//                                scope.launch {
//                                    tooltipState.dismiss()
//                                    tooltipState.onDispose()
//                                }
//                            }) {
//                                Text("Dismiss")
//                            }
//                        }
//                    ) {
//                        Text("Navigate to the Transfer File screen? Connect your controller to this device using a C-to-C cable, and wait for a stable connection before transferring the file to the controller.")
//                    }
//                },
//                state = tooltipState
//            ) {
//                IconButton(
//                    onClick = {
//                        navigationEventsViewModel.sendEvent(DroneTMAppNavigationEvent.OnNavigateToHome)
//                    },
//                    modifier = Modifier
//                        .align(Alignment.TopEnd)
//                        .size(48.dp)
//                        .padding(8.dp)
//                ) {
//                    Image(
//                        painter = painterResource(id = R.drawable.ic_file_move_outline_24), // Your drawable
//                        contentDescription = "Transfer file to device",
//                        modifier = Modifier
//                            .fillMaxSize()
//                    )
//                }
//            }
//        }

        NavigateToTransferFileWidget(
            boxModifier =  Modifier
                .fillMaxWidth()
                .align(Alignment.TopEnd)
                .padding(end = 16.dp, top = 32.dp),
            toolTipModifier = Modifier
                .align(Alignment.TopEnd)
        )
//    }



        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Drone Operator Icon
            Surface(
                shape = CircleShape,
                modifier = Modifier
                    .size(100.dp)
                    .padding(bottom = 0.dp),
                color = Color.Red
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_drone_operator_icon_24),
                    contentDescription = "Drone Operator",
                    tint = Color.White,
                    modifier = Modifier.padding(16.dp),
                )
            }

            Text(
                text = "Drone Operator",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp, top = 16.dp)
            )

            // Google Sign In Button
            OutlinedButton(
                onClick = {
                    if (!state.isLoggingIn) {
                        onGoogleSignInClick(rememberMe)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(),
                enabled = enableView,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                shape = RoundedCornerShape(16)
            ) {
                if (state.isLoggingIn) CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier
                        .size(32.dp),
                    strokeWidth = 2.dp
                ) else {
                    Image(
                        painter = painterResource(id = R.drawable.ic_google_logo),
                        contentDescription = "Google Icon",
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Continue with Google", style = MaterialTheme.typography.labelLarge)
                }
            }

            Text(
                text = "or",
                modifier = Modifier.padding(vertical = 16.dp),
                style = MaterialTheme.typography.bodyMedium
            )

            // Email TextField
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                enabled = enableView,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            // Password TextField
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                enabled = enableView,
                visualTransformation = if (isPasswordVisible)
                    VisualTransformation.None
                else
                    PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(
                            imageVector = if (isPasswordVisible)
                                Icons.Default.Visibility
                            else
                                Icons.Default.VisibilityOff,
                            contentDescription = if (isPasswordVisible) "Hide password" else "Show password"
                        )
                    }
                }
            )

            // Remember Me and Forgot Password Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = rememberMe,
                        enabled = enableView,
                        onCheckedChange = { rememberMe = it }
                    )
                    Text("Remember Me")
                }
                TextButton(
                    onClick = {
                        if (email.isNotEmpty()) {
                            onForgetPasswordClick(email)
                        }
                    },
                    enabled = enableView,
                ) {
                    Text(
                        "Forgot Your Password?",
                        color = Color.Red
                    )
                }
            }

            // Login Button
            Button(
                onClick = {
                    if (!state.isLoggingIn) {
                        onLoginClick(email, password, rememberMe)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = enableView,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(16)
            ) {
                if (state.isLoggingIn) CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(32.dp),
                    strokeWidth = 2.dp
                ) else if (state.isLoggingIn) Text(
                    "Retry Log In",
                    color = Color.White
                ) else Text("Log In", color = Color.White)
            }
        }
    }
}

//@Preview
//@Composable
//fun PreviewWidget(){
//    LoginScreenWidget(
//        onLoginClick = { email, password, rememberMe -> },
//        onGoogleSignInClick = { },
//        onForgetPasswordClick = {  }
//    )
//}