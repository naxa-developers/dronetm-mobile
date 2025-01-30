package np.com.naxa.drone_tasking_manager.ui.screens.splash

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
import androidx.compose.runtime.remember
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
import np.com.naxa.drone_tasking_manager.local_providers.LocalNavigationEventsViewModel
import np.com.naxa.drone_tasking_manager.navigation.viewmodels.events.DroneTMAppNavigationEvent

@Composable
fun SplashScreen(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val alphaAnimation = remember { Animatable(0f) }
    val navigationEventsViewModel = LocalNavigationEventsViewModel.current

    LaunchedEffect(Unit) {
        alphaAnimation.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
        )
        delay(2000)

        try {
            val storageService = MMKVStorageService.getInstance()

            if (storageService.get<String>(StorageKeys.User.ACCESS_TOKEN, "").trim().isNotBlank()) {
                navigationEventsViewModel.sendEvent(DroneTMAppNavigationEvent.OnNavigateToProjects)
                return@LaunchedEffect
            } else {
                navigationEventsViewModel.sendEvent(DroneTMAppNavigationEvent.OnNavigateToLogin)
                return@LaunchedEffect
            }
        }catch (e: DataUtils.EncryptionException){
            navigationEventsViewModel.sendEvent(DroneTMAppNavigationEvent.OnNavigateToLogin)
            return@LaunchedEffect
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
