package np.com.naxa.drone_tasking_manager.features.user.profile.views.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import np.com.naxa.drone_tasking_manager.core.widgets.ShimmerItemPlaceHolder
import np.com.naxa.drone_tasking_manager.features.user.profile.viewmodels.UserProfileViewModel
import np.com.naxa.drone_tasking_manager.features.user.profile.viewmodels.events.UserProfileEvents
import np.com.naxa.drone_tasking_manager.features.user.profile.views.widgets.CountryDropdown
import np.com.naxa.drone_tasking_manager.local_providers.LocalLoginViewModel
import np.com.naxa.drone_tasking_manager.local_providers.LocalUserProfileViewModel
import np.com.naxa.drone_tasking_manager.utils.NetworkImageAvatar
import np.com.naxa.drone_tasking_manager.utils.NetworkImageAvatarRemember

@Composable
fun BasicDetailsScreen() {

    val viewModel = LocalUserProfileViewModel.current
    val state by viewModel.userProfileState.collectAsState()

    val profileUpdateState by viewModel.userProfileUpdateState.collectAsState()


    var userId by rememberSaveable { mutableStateOf("") }
    var imgUrl by rememberSaveable { mutableStateOf("") }
    var name by rememberSaveable { mutableStateOf("") }
    var country by rememberSaveable { mutableStateOf("") }
    var city by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }

    val profileAvatarSize by remember { mutableStateOf(84.dp) }


    LaunchedEffect(state.userProfile) {
        Log.d("TAG", "fetchUserProfile BasicDetailsScreen I am Here: ${state.userProfile}")
        state.userProfile?.let {
            userId = "${it.user_id ?: ""}"
            imgUrl = it.profile_img ?: ""
            name = it.name ?: ""
            country = it.country ?: ""
            city = it.city ?: ""
            phone = it.phone_number ?: ""
        }
    }


    if(state.isUserProfileLoading || profileUpdateState.isUserProfileUpdateLoading){
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {

                items(5) {
                    ShimmerItemPlaceHolder()
                }
        }
    }else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Basic Details",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Profile Avatar
            Surface(
                modifier = Modifier
                    .size(profileAvatarSize)
                    .align(Alignment.CenterHorizontally),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary
            ) {
                if (imgUrl.isNotEmpty()) {
                    // If an image URL is provided, load the image from the network.
                    NetworkImageAvatarRemember(imageUrl = imgUrl, size = profileAvatarSize)
                } else {
                    // If no image URL is provided, display the user's initials as a fallback.
                    // The initials are derived from the first letter of the name.
                    Text(
                        text = "${
                            name.getOrNull(0)?.uppercase()?.ifEmpty { "TM" }
                        }", // Get the first letter, convert to uppercase, and use "TM" if the name is empty.
                        modifier = Modifier.wrapContentSize(), // Wrap the content size to fit the text.
                        style = MaterialTheme.typography.headlineMedium, // Apply the headlineMedium style from the MaterialTheme.
                        color = Color.White // Set the text color to white.
                    )
                }

            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                enabled = false,
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            CountryDropdown(
                selectedCountry = country,
                onCountrySelected = { onSelectedCountry ->
                    country = onSelectedCountry
                },
                modifier = Modifier
                    .fillMaxWidth()
            )


            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = city,
                onValueChange = { city = it },
                label = { Text("City") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone number") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                enabled = !profileUpdateState.isUserProfileUpdateLoading,
                onClick = { /* Handle save */

                    if (userId.isEmpty()) return@Button

                    viewModel.onEvent(
                        UserProfileEvents.UpdateBasicDetails(
                            userId = userId,
                            name = name,
                            country = country,
                            city = city,
                            phone = phone
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

}