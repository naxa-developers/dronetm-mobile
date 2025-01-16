package np.com.naxa.drone_tasking_manager.features.login.mapper

import np.com.naxa.drone_tasking_manager.features.login.dto.LoginResponseDto
import np.com.naxa.drone_tasking_manager.features.login.models.LoginResponse

fun LoginResponseDto.toLoginResponse(): LoginResponse {

    return  LoginResponse(
        access_token = this.accessToken,
        detail = this.detail,
        refresh_token = this.refreshToken,
        role = this.role,
        token_type = this.tokenType
    )
}