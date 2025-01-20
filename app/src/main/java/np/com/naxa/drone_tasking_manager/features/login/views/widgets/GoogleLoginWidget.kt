package np.com.naxa.drone_tasking_manager.features.login.views.widgets

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import np.com.naxa.drone_tasking_manager.R


@Composable
fun GoogleLoginWidget(enabled: Boolean = true, onClick: () -> Unit) {
    Column {
        Button( onClick = onClick,
            enabled = enabled,
            modifier = Modifier .fillMaxWidth() .padding(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White)
        ) {
            Image( painter = painterResource(id = R.drawable.ic_google_logo),
                contentDescription = "Google Logo", modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Continue with Google", style = MaterialTheme.typography.titleSmall)
        }

        Row( modifier = Modifier .fillMaxWidth() .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically ) {
            HorizontalDivider( color = Color.Gray, modifier = Modifier .weight(1f) .height(1.dp) )
            Text( text = "OR", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(horizontal = 8.dp),
                color = Color.Gray )
            HorizontalDivider( color = Color.Gray, modifier = Modifier .weight(1f) .height(1.dp) )
        }
    }
}