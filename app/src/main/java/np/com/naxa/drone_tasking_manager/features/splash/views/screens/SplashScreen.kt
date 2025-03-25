package np.com.naxa.drone_tasking_manager.features.splash.views.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import np.com.naxa.drone_tasking_manager.R
import np.com.naxa.drone_tasking_manager.core.services.storage.MMKVStorageService
import np.com.naxa.drone_tasking_manager.core.services.storage.StorageKeys
import np.com.naxa.drone_tasking_manager.core.theme.PrimaryColor
import np.com.naxa.drone_tasking_manager.core.utils.DataUtils
import np.com.naxa.drone_tasking_manager.features.user.auth.refreshtoken.viewmodels.events.RefreshTokenEvents
import np.com.naxa.drone_tasking_manager.local_providers.LocalNavigationEventsViewModel
import np.com.naxa.drone_tasking_manager.local_providers.LocalRefreshTokenViewModel
import np.com.naxa.drone_tasking_manager.navigation.viewmodels.events.DroneTMAppNavigationEvent
import np.com.naxa.drone_tasking_manager.utils.internetutils.InternetConnectionUtils

@Composable
fun SplashScreen(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val alphaAnimation = remember { Animatable(0f) }
    val navigationEventsViewModel = LocalNavigationEventsViewModel.current


    val refreshTokenViewModel = LocalRefreshTokenViewModel.current
    val refreshTokenState by refreshTokenViewModel.state.collectAsState()

    var isInternetAvailable by remember { mutableStateOf(InternetConnectionUtils.isInternetAvailable(context)) }


    LaunchedEffect(Unit) {
        isInternetAvailable = InternetConnectionUtils.isInternetAvailable(context)

        alphaAnimation.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
        )
        delay(2000)

        try {
            val storageService = MMKVStorageService.getInstance()

            if (storageService.get<String>(StorageKeys.User.ACCESS_TOKEN, "").trim().isNotBlank()) {
//                    navigationEventsViewModel.sendEvent(DroneTMAppNavigationEvent.OnNavigateToProjects)

                if(isInternetAvailable){
                    refreshTokenViewModel.onEvent(RefreshTokenEvents.RefreshToken(forceRefresh = true))
                }else{
                    navigationEventsViewModel.sendEvent(DroneTMAppNavigationEvent.OnNavigateToProjects)
                }

                return@LaunchedEffect
            } else {
                navigationEventsViewModel.sendEvent(DroneTMAppNavigationEvent.OnNavigateToLogin)
                return@LaunchedEffect
            }
        } catch (e: DataUtils.EncryptionException) {
            // navigate to login screen if access token is not found/empty or error while decrypting the saved access token
            navigationEventsViewModel.sendEvent(DroneTMAppNavigationEvent.OnNavigateToLogin)
            return@LaunchedEffect
        }


    }


    if(isInternetAvailable) {
        // check refresh token state and navigate to respective screen
        if (refreshTokenState.isSuccess && !refreshTokenState.isLoading && refreshTokenState.errorMessage.isEmpty()) {
            navigationEventsViewModel.sendEvent(DroneTMAppNavigationEvent.OnNavigateToProjects)
        } else if (!refreshTokenState.isSuccess && !refreshTokenState.isLoading && refreshTokenState.errorMessage.isNotEmpty()) {
            navigationEventsViewModel.sendEvent(DroneTMAppNavigationEvent.OnNavigateToLogin)
        }
    }




    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PrimaryColor)
            .graphicsLayer {
                alpha = alphaAnimation.value
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.drone_tm_logo1),
                contentDescription = "Drone controller image",
                alignment = Alignment.BottomCenter,
            )
            Text(
                modifier = Modifier
                    .graphicsLayer {
                        alpha = alphaAnimation.value
                    },
                text = ContextCompat.getString(context, R.string.app_name),
                style = MaterialTheme.typography.headlineMedium,
                color = Color(0xFFF2C0C0),
                fontWeight = FontWeight.Bold
            )
        }

    }
}
