package np.com.naxa.drone_tasking_manager.features.user.profile.views.screens

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import np.com.naxa.drone_tasking_manager.local_providers.LocalUserProfileViewModel

@Composable
fun OtherDetailsScreen() {

    val viewModel = LocalUserProfileViewModel.current
    val state by viewModel.userProfileState.collectAsState()

    var notifyDistance by remember { mutableStateOf("0") }
    var experience by remember { mutableStateOf("") }
    var droneOwned by remember { mutableStateOf("") }
    var isCertified by remember { mutableStateOf(false) }
    var certificateFile by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(state.userProfile) {
        state.userProfile?.let {
            notifyDistance = "${it.notify_for_projects_within_km ?: 0}"
            experience = it.country ?: ""
            droneOwned = it.drone_you_own ?: ""
            isCertified = it.certified_drone_operator ?: false
            certificateFile = it.certificate_file ?: ""
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Other Details",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = notifyDistance,
            onValueChange = { notifyDistance = it },
            label = { Text("Notify for projects within Distance (in km)") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = experience,
            onValueChange = { experience = it },
            label = { Text("Experience") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = droneOwned,
            onValueChange = { droneOwned = it },
            label = { Text("Drone you own") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Certified Drone Operator?")
            Spacer(modifier = Modifier.width(16.dp))
            Switch(
                checked = isCertified,
                onCheckedChange = { isCertified = it }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            onClick = { /* Handle file upload */ }
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = certificateFile ?: "Upload Drone Registration Certificate",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "The supported file formats are pdf, jpeg, png",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { /* Handle save */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(0.dp),
//            enabled = enableView,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Red,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),

            shape = RoundedCornerShape(16)
        ) {
            Text("Save")
        }
    }
}