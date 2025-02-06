package np.com.naxa.drone_tasking_manager.features.user.profile.views.screens

import android.Manifest
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import np.com.naxa.drone_tasking_manager.features.user.profile.viewmodels.events.UserProfileEvents
import np.com.naxa.drone_tasking_manager.local_providers.LocalUserProfileViewModel
import np.com.naxa.drone_tasking_manager.utils.PermissionUtils
import np.com.naxa.drone_tasking_manager.utils.getFileName
import np.com.naxa.drone_tasking_manager.utils.getFileNameFromUrl

@Composable
fun OtherDetailsScreen() {

    val context = LocalContext.current
    var permissionGranted by remember { mutableStateOf(false) }


    val viewModel = LocalUserProfileViewModel.current
    val state by viewModel.userProfileState.collectAsState()

    val profileUpdateState by viewModel.userProfileUpdateState.collectAsState()


    var userId by rememberSaveable { mutableStateOf("") }
    var notifyDistance by remember { mutableStateOf("0") }
    var experience by remember { mutableStateOf("0") }
    var droneOwned by remember { mutableStateOf("") }
    var isCertified by remember { mutableStateOf(false) }
    var certificateFileUrl by remember { mutableStateOf("") }
    var registrationFileUrl by remember { mutableStateOf("") }

    var isPickingRegistrationFile by remember { mutableStateOf(true) }
    var certificateFileUri by remember { mutableStateOf("") }
    var registrationFileUri by remember { mutableStateOf("") }

    LaunchedEffect(state.userProfile) {
        state.userProfile?.let {
            userId = "${it.user_id ?: ""}"
            notifyDistance = "${it.notify_for_projects_within_km ?: 0}"
            experience = "${it.experience_years ?: 0}"
            droneOwned = it.drone_you_own ?: ""
            isCertified = it.certified_drone_operator ?: false
            certificateFileUrl = it.certificate_file ?: ""
            registrationFileUrl = it.certificate_file ?: ""
        }
    }

    // file picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri>? ->
        if (isPickingRegistrationFile) {
            val uri = uris?.firstOrNull() ?: return@rememberLauncherForActivityResult
            registrationFileUri = uri.toString()
        } else {
            val uri = uris?.firstOrNull() ?: return@rememberLauncherForActivityResult
            certificateFileUri = uri.toString()
        }
    }


    /**
     * Permission launcher for handling storage permission request
     */
    val mediaFilesStoragePermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
//        val readPermissionGranted = permissions[Manifest.permission.READ_EXTERNAL_STORAGE] == true
//        val writePermissionGranted = permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] == true
        val readPermissionGranted = true
        val writePermissionGranted = true

        Log.d("OtherDetailsScreen", "Permission Request Result:")
        Log.d("OtherDetailsScreen", "Read Permission Granted: $readPermissionGranted")
        Log.d("OtherDetailsScreen", "Write Permission Granted: $writePermissionGranted")
        Log.d("OtherDetailsScreen", "Requested Permissions: ${permissions.keys}")

        when {
            readPermissionGranted && writePermissionGranted -> {
                // Both read and write permissions granted
                Log.d("OtherDetailsScreen", "Both read and write permissions granted")
                // Proceed with file operations
                filePickerLauncher.launch(arrayOf("application/pdf", "image/jpeg"))
            }

            !readPermissionGranted && !writePermissionGranted -> {
                // If not granted both permission
                Log.e("OtherDetailsScreen", "Both read and write permissions denied")
                Toast.makeText(
                    context,
                    "You have denied both read and write permissions",
                    Toast.LENGTH_LONG
                ).show()
            }

            !readPermissionGranted -> {
                // If not granted read permission only
                Log.e("OtherDetailsScreen", "Read permission denied")
                Toast.makeText(
                    context,
                    "You have denied read permission",
                    Toast.LENGTH_LONG
                ).show()
            }

            else -> {
                // If not granted write permission only
                Log.e("OtherDetailsScreen", "Write permission denied")
                Toast.makeText(
                    context,
                    "You have denied write permission",
                    Toast.LENGTH_LONG
                ).show()
            }
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

        if (isCertified) {
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    isPickingRegistrationFile = false
                    // Else just checking requesting permission to handle media files
                    if (!PermissionUtils.hasMediaFileAccessPermissions(context)) {
                        mediaFilesStoragePermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                            )
                        )
                    } else {
                        filePickerLauncher.launch(arrayOf("application/pdf", "image/jpeg"))
                    }
                }
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    IconButton(onClick = {

                    }) {
                        Icon(
                            active = true,
                            activeContent = { Icons.Filled.CloudUpload },
                            inactiveContent = null,
                        )
                    }

                    Text(
                        text = "The supported file formats are pdf, jpeg, png",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (certificateFileUrl.isNotEmpty() || certificateFileUri.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { /* Handle file upload */ }
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {


                        Text(
                            text = "${
                                getFileName(
                                    context,
                                    Uri.parse(certificateFileUri)
                                ) ?: certificateFileUrl.getFileNameFromUrl()
                            }",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        IconButton(onClick = { /* Do something */ }) {
                            Icon(
                                active = true,
                                activeContent = { Icons.Filled.Download },
                                inactiveContent = null,
                            )
                        }

                        IconButton(onClick = { /* Do something */ }) {
                            Icon(
                                active = true,
                                activeContent = { Icons.Filled.Delete },
                                inactiveContent = null,
                            )
                        }
                    }
                }

            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        Text(
            text = "Drone Registration Certificate",
            style = MaterialTheme.typography.bodyMedium
        )
        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                isPickingRegistrationFile = true
                if (!PermissionUtils.hasMediaFileAccessPermissions(context)) {
                    mediaFilesStoragePermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        )
                    )
                } else {
                    filePickerLauncher.launch(arrayOf("application/pdf", "image/jpeg"))
                }
            }
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IconButton(onClick = {
                }) {
                    Icon(
                        active = true,
                        activeContent = { Icons.Filled.CloudUpload },
                        inactiveContent = null,
                    )
                }

                Text(
                    text = "The supported file formats are pdf, jpeg, png",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (registrationFileUrl.isNotEmpty() || registrationFileUri.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = { /* Handle file upload */ }
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {


                    Text(
                        text = "${
                            getFileName(
                                context,
                                Uri.parse(registrationFileUri)
                            ) ?: registrationFileUrl.getFileNameFromUrl()
                        }",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    IconButton(onClick = { /* Do something */ }) {
                        Icon(
                            active = true,
                            activeContent = { Icons.Filled.Download },
                            inactiveContent = null,
                        )
                    }

                    IconButton(onClick = { /* Do something */ }) {
                        Icon(
                            active = true,
                            activeContent = { Icons.Filled.Delete },
                            inactiveContent = null,
                        )
                    }
                }
            }

        }

        Spacer(modifier = Modifier.height(24.dp))


        Button(
            enabled = !profileUpdateState.isUserProfileUpdateLoading,
            onClick = {
                viewModel.onEvent(
                    UserProfileEvents.UpdateOtherDetails(
                        userId = userId,
                        certifiedDroneOperator = isCertified,
                        experienceYears = Integer.parseInt(experience),
                        droneYouOwn = droneOwned,
                        notifyForProjectsWithinKm = Integer.parseInt(notifyDistance),
                        certificateFile = certificateFileUri,
                        registrationFile = registrationFileUri,
                    )
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(0.dp),
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