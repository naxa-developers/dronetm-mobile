package np.com.naxa.drone_tasking_manager.features.user.auth.refreshtoken.mapper

import np.com.naxa.drone_tasking_manager.features.user.auth.refreshtoken.dto.RefreshTokenDto
import np.com.naxa.drone_tasking_manager.features.user.auth.refreshtoken.models.RefreshToken
import np.com.naxa.drone_tasking_manager.features.user.auth.refreshtoken.models.RefreshTokenFailed

fun RefreshTokenDto.toRefreshToken(): RefreshToken {
    return RefreshToken(
        access_token = this.accessToken,
        refresh_token = this.refreshToken,
        token_type = this.tokenType,
        role = this.role
    )
}

fun RefreshTokenDto.toRefreshTokenFailed(): RefreshTokenFailed {
    return RefreshTokenFailed(
        detail = this.detail,
    )
}