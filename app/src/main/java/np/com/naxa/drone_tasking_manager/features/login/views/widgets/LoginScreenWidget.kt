package np.com.naxa.drone_tasking_manager.features.login.views.widgets

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import np.com.naxa.drone_tasking_manager.R
import np.com.naxa.drone_tasking_manager.local_providers.LocalLoginViewModel

@Composable
fun LoginScreenWidget(
    onLoginClick: (String, String, Boolean) -> Unit,
    onGoogleSignInClick: () -> Unit,
    onForgetPasswordClick: (String) -> Unit,
) {

    val viewModel = LocalLoginViewModel.current
    val state by viewModel.state.collectAsState()

    var email by remember { mutableStateOf("testnaxa@gmail.com") }
    var password by remember { mutableStateOf("Naxa@123") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(false) }

    val enableView by remember { derivedStateOf {
        !state.isLoggingIn
    } }

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
            modifier = Modifier.padding(bottom = 24.dp, top = 16.dp)
        )

        // Google Sign In Button
        Button(
            onClick = {
                if (!state.isLoggingIn) {
                    onGoogleSignInClick()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .padding(0.dp),
            enabled = enableView,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            if(state.isLoggingIn) CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp
            ) else Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp)
                    .padding(0.dp)
                    .clip(shape = RoundedCornerShape(16))
                    .border(
                        BorderStroke(1.dp, SolidColor(Color.LightGray)),
                        shape = RoundedCornerShape(16)
                    )

            ) {
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
            TextButton(onClick = {
                if (email.isNotEmpty()) {
                    onForgetPasswordClick(email)
                }
            },
                enabled = enableView,) {
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
                containerColor = Color.Red
            ),
            shape = RoundedCornerShape(16)
        ) {
            if(state.isLoggingIn) CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    ) else if(state.isLoggingIn) Text("Retry Log In") else Text("Log In")
        }
    }
}

//@Preview
//@Composable
//fun PreviewWidget(){
//    LoginScreenWidget(
//        onLoginClick = { email, password, rememberMe -> },
//        onGoogleSignInClick = { },
//    )
//}