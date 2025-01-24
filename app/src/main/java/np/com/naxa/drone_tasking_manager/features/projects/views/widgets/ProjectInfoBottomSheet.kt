package np.com.naxa.drone_tasking_manager.features.projects.views.widgets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.gson.JsonObject
import np.com.naxa.drone_tasking_manager.local_providers.LocalNavigationEventsViewModel
import np.com.naxa.drone_tasking_manager.navigation.viewmodels.events.DroneTMAppNavigationEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectInfoBottomSheet(
    modifier: Modifier = Modifier,
    infoJsonObject: JsonObject?,
    infoSheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false),
    show: Boolean,
    onDismiss: () -> Unit,
) {

    val navigationEventsViewModel = LocalNavigationEventsViewModel.current

    if (show) {
        ModalBottomSheet(
            modifier = modifier
                .wrapContentHeight(),
            sheetState = infoSheetState,
            onDismissRequest = {
                onDismiss.invoke()
            }
        ) {
            infoJsonObject?.let {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        modifier = Modifier.padding(bottom = 8.dp),
                        text = it.get("name").asString,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = "#${it.get("slug").asString}",
                        style = MaterialTheme.typography.labelMedium
                    )

                    ElevatedButton(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White
                        ),
                        onClick = {
                            val id = it.get("id").asString

                            if (id != null) {
                                navigationEventsViewModel.sendEvent(
                                    DroneTMAppNavigationEvent.OnNavigateToProjectDetail(
                                        id
                                    )
                                )
                            }
                        }
                    ) {
                        Text("Go to Project", color = Color.White)
                    }
                }
            }
        }
    }
}