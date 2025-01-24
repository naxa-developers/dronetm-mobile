package np.com.naxa.drone_tasking_manager.features.login.mapper

import android.net.Uri
import np.com.naxa.drone_tasking_manager.features.login.dto.GoogleLoginLinkResponseDto
import np.com.naxa.drone_tasking_manager.features.login.models.GoogleLoginLinkResponse

fun GoogleLoginLinkResponseDto.toGoogleLoginLinkResponse(): GoogleLoginLinkResponse {
    return GoogleLoginLinkResponse(
        loginUrl = loginUrl
    )
}

fun GoogleLoginLinkResponse.state(): String? =
    if (loginUrl != null) Uri.parse(loginUrl!!).getQueryParameter("state") else null

fun GoogleLoginLinkResponse.clientId(): String? =
    if (loginUrl != null) Uri.parse(loginUrl!!).getQueryParameter("client_id") else null

fun GoogleLoginLinkResponse.scopes(): List<String>? =
    if (loginUrl != null) Uri.parse(loginUrl!!).getQueryParameter("scope")?.split("+") else null