package np.com.naxa.drone_tasking_manager.features.login.utils

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.Scope
import np.com.naxa.drone_tasking_manager.BuildConfig

fun getGoogleSignInClient(context: Context): GoogleSignInClient {
    val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(BuildConfig.GOOGLE_CLIENT_ID)
        .requestEmail()
        .requestProfile()
        .requestScopes(Scope("${Scopes.OPEN_ID} ${Scopes.LEGACY_USERINFO_EMAIL} ${Scopes.LEGACY_USERINFO_PROFILE}"))
        .requestServerAuthCode(BuildConfig.GOOGLE_CLIENT_ID)
        .build()
    return GoogleSignIn.getClient(context, signInOptions) }