package np.com.naxa.drone_tasking_manager.features.login.views.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import np.com.naxa.drone_tasking_manager.R

@Composable
fun HeaderLoginUi() {

    Column ( modifier = Modifier .fillMaxWidth() .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally ) {
        Icon( painter = painterResource(id = R.drawable.ic_user_avatar),
            contentDescription = "User Icon",
//            modifier = Modifier.size(24.dp),
            tint = Color.Black )

        Spacer(modifier = Modifier.width(8.dp))

        Text( text = "Drone Operator", style = MaterialTheme.typography.labelLarge,) }
}