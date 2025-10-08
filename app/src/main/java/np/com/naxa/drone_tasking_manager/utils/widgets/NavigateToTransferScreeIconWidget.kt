package np.com.naxa.drone_tasking_manager.utils.widgets

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.RichTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import np.com.naxa.drone_tasking_manager.R
import np.com.naxa.drone_tasking_manager.local_providers.LocalNavigationEventsViewModel
import np.com.naxa.drone_tasking_manager.navigation.routes.Routes
import np.com.naxa.drone_tasking_manager.navigation.viewmodels.events.DroneTMAppNavigationEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigateToTransferFileWidget(
    boxModifier: Modifier = Modifier,
    toolTipModifier: Modifier = Modifier,
    isFromToolbar: Boolean = false,
    iconSize: Dp = 48.dp
) {

    val navigationEventsViewModel = LocalNavigationEventsViewModel.current

    val tooltipPosition = TooltipDefaults.rememberTooltipPositionProvider(
        positioning = TooltipAnchorPosition.Above
    )
    val tooltipState = rememberTooltipState(isPersistent = true)
    val scope = rememberCoroutineScope()


    if (isFromToolbar) {
        TooltipBox(
            modifier = toolTipModifier,
            positionProvider = tooltipPosition,
            tooltip = {
                RichTooltip(
                    title = { Text("Transfer File?") },
                    // caretSize = caretSize,
                    action = {
                        TextButton(onClick = {
                            scope.launch {
                                tooltipState.dismiss()
                                tooltipState.onDispose()
                            }
                        }) {
                            Text("Dismiss")
                        }
                    }
                ) {
                    Text("Navigate to the Transfer File screen? Connect your controller to this device using a C-to-C cable, and wait for a stable connection before transferring the file to the controller.")
                }
            },
            state = tooltipState
        ) {
            IconButton(
                onClick = {
                    navigationEventsViewModel.sendEvent(
                        DroneTMAppNavigationEvent.OnNavigateToDeviceConnection(
                            route = Routes.DownloadAndTransfer
                        )
                    )
                },
                modifier = Modifier
                    .size(iconSize)
                    .padding(8.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_file_move_outline_24), // Your drawable
                    contentDescription = "Transfer file to device",
                    modifier = Modifier
                        .size(24.dp)
                )
            }
        }
    } else {
        Box(
            modifier = boxModifier,
            contentAlignment = Alignment.TopEnd
        ) {
            TooltipBox(
                modifier = toolTipModifier,
                positionProvider = tooltipPosition,
                tooltip = {
                    RichTooltip(
                        title = { Text("Transfer File?") },
                        // caretSize = caretSize,
                        action = {
                            TextButton(onClick = {
                                scope.launch {
                                    tooltipState.dismiss()
                                    tooltipState.onDispose()
                                }
                            }) {
                                Text("Dismiss")
                            }
                        }
                    ) {
                        Text("Navigate to the Transfer File screen? Connect your controller to this device using a C-to-C cable, and wait for a stable connection before transferring the file to the controller.")
                    }
                },
                state = tooltipState
            ) {
                IconButton(
                    onClick = {
                        navigationEventsViewModel.sendEvent(
                            DroneTMAppNavigationEvent.OnNavigateToDeviceConnection(
                                route = Routes.DownloadAndTransfer
                            )
                        )
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(iconSize)
                        .padding(8.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_file_move_outline_24), // Your drawable
                        contentDescription = "Transfer file to device",
                        modifier = Modifier
                            .size(24.dp)
                    )
                }
            }
        }
    }
}