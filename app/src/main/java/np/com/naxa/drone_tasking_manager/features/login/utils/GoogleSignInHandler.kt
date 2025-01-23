package np.com.naxa.drone_tasking_manager.features.login.utils

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.net.Uri
import android.util.Log
import androidx.browser.customtabs.CustomTabsIntent
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.PasswordCredential
import androidx.credentials.PublicKeyCredential
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.AuthorizationResult
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.Scope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


object GoogleSignInHandler {

    private const val TAG = "GoogleSignInHandler"

    fun initiate(
        context: Context,
        launchSignIn: (IntentSender) -> Unit,
        onSignInSuccess: (AuthorizationResult) -> Unit,
        onSignInFailure: (Exception) -> Unit
    ) {

        val requestedScopes = listOf(
            Scope(Scopes.PROFILE),
            Scope(Scopes.EMAIL),
            Scope(Scopes.APP_STATE),
        )

        val authorizationRequest = AuthorizationRequest
            .builder()
            .setRequestedScopes(requestedScopes)
            .requestOfflineAccess("284837732251-e5cfncpoiriu4qdg68qjvtduoq7tmf8a.apps.googleusercontent.com")
            .build()

        CoroutineScope(Dispatchers.IO).launch {
            Identity.getAuthorizationClient(context)
                .authorize(authorizationRequest)
                .addOnSuccessListener { authorizationResult ->
                    if (authorizationResult.hasResolution()) {
                        authorizationResult.pendingIntent?.intentSender?.let {
                            launchSignIn.invoke(
                                it
                            )
                        }
                        return@addOnSuccessListener
                    }

                    onSignInSuccess.invoke(authorizationResult)
                }
                .addOnFailureListener { exception ->
                    onSignInFailure.invoke(exception)
                }
        }


    }

    //    val context = LocalContext.current
    //
    //    val authorizationRequestLauncher = rememberLauncherForActivityResult(
    //        contract = ActivityResultContracts.StartIntentSenderForResult()
    //    ) { activityResult ->
    //        if (activityResult.resultCode == Activity.RESULT_OK) {
    //            val result = Identity.getAuthorizationClient(context)
    //                .getAuthorizationResultFromIntent(activityResult.data)
    //
    //            val code = result.serverAuthCode
    //            val token = result.accessToken
    //            val scopes = result.grantedScopes
    //            val zac = result.toGoogleSignInAccount()?.zac()
    //            val zad = result.toGoogleSignInAccount()?.zad()
    //
    //            Log.d(
    //                "GoogleSignInHandler", """
    //                LoginScreen: [authorizationLauncher]:
    //                code: $code,
    //                token: $token
    //                scopes: $scopes
    //                zac: $zac
    //                zad: $zad
    //            """.trimIndent()
    //            )
    //
    //        } else {
    //            Log.e("GoogleSignInHandler", "Authorization cancelled")
    //        }
    //    }

    // Call on button click
    //    GoogleSignInHandler.initiate1(
    //    context = context,
    //    onSignInSuccess = { result ->
    //        val code = result.serverAuthCode
    //        val token = result.accessToken
    //        val scopes = result.grantedScopes
    //        val zac = result.toGoogleSignInAccount()?.zac()
    //        val zad = result.toGoogleSignInAccount()?.zad()
    //
    //        Log.d(
    //            "GoogleSignInHandler", """
    //                        LoginScreen: [authorizationLauncher]:
    //                        code: $code,
    //                        token: $token
    //                        scopes: $scopes
    //                        zac: $zac
    //                        zad: $zad
    //                    """.trimIndent()
    //        )
    //
    //    },
    //    onSignInFailure = {},
    //    launchSignIn = {
    //        authorizationRequestLauncher.launch(IntentSenderRequest.Builder(it).build())
    //    }
    //    )


    fun initiate1(context: Context) {

        val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId("284837732251-e5cfncpoiriu4qdg68qjvtduoq7tmf8a.apps.googleusercontent.com")
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest(
            credentialOptions = listOf(
                googleIdOption,
            )
        )

        CoroutineScope(Dispatchers.IO).launch {
            val credentialManager = CredentialManager.create(context)
            try {
                val result = credentialManager.getCredential(
                    request = request,
                    context = context,
                )
                handleSignIn(result)
            } catch (e: GetCredentialException) {
                Log.e(TAG, "initiate: Error: ${e.message}", e)
            }
        }


    }

    private fun handleSignIn(result: GetCredentialResponse) {
        // Handle the successfully returned credential.
        when (val credential = result.credential) {

            // Passkey credential
            is PublicKeyCredential -> {
                // Share responseJson such as a GetCredentialResponse on your server to
                // validate and authenticate
                val responseJson = credential.authenticationResponseJson

                Log.d(TAG, "handleSignIn: [PublicKeyCredential] -> $responseJson")

            }

            // Password credential
            is PasswordCredential -> {
                // Send ID and password to your server to validate and authenticate.
                val username = credential.id
                val password = credential.password

                Log.d(TAG, "handleSignIn: [PasswordCredential] -> $username, $password")
            }

            // GoogleIdToken credential
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        // Use googleIdTokenCredential and extract the ID to validate and
                        // authenticate on your server.
                        val googleIdTokenCredential = GoogleIdTokenCredential
                            .createFrom(credential.data)


                        val id = googleIdTokenCredential.id
                        val idToken = googleIdTokenCredential.idToken
                        val displayName = googleIdTokenCredential.displayName
                        val familyName = googleIdTokenCredential.familyName
                        val givenName = googleIdTokenCredential.givenName
                        val phoneNumber = googleIdTokenCredential.phoneNumber
                        val profilePictureUri = googleIdTokenCredential.profilePictureUri

                        Log.d(
                            TAG, """
                            handleSignIn: [GoogleIdTokenCredential] ->
                            id: $id,
                            idToken: $idToken,
                            displayName: $displayName,
                            familyName: $familyName,
                            givenName: $givenName,
                            phoneNumber: $phoneNumber,
                            profilePictureUri: $profilePictureUri
                        """.trimIndent()
                        )

                    } catch (e: GoogleIdTokenParsingException) {
                        Log.e(TAG, "Received an invalid google id token response", e)
                    }
                } else {
                    // Catch any unrecognized custom credential type here.
                    Log.e(TAG, "Unexpected type of credential")
                }
            }

            else -> {
                // Catch any unrecognized credential type here.
                Log.e(TAG, "Unexpected type of credential")
            }
        }
    }

    fun launchOAuthUrl(context: Context) {
        val oauthUrl =
            "https://accounts.google.com/o/oauth2/v2/auth?response_type=code&client_id=221924285441-51jkcndsunlttnknschjsh1m95dtgp0v.apps.googleusercontent.com&redirect_uri=https%3A%2F%2Fdev.dronetm.org%2Fauth&scope=openid+https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fuserinfo.email+https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fuserinfo.profile&state=zrRM6RKzt6hsV13Sq897OdzURQz2PL"

        // Create a Custom Tab Intent
        val builder = CustomTabsIntent.Builder()
        val customTabsIntent = builder.build()

        try {
            // Launch the URL in Custom Tabs
            customTabsIntent.launchUrl(context, Uri.parse(oauthUrl))
        } catch (e: Exception) {
            // Fallback to regular browser if Custom Tabs not available
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(oauthUrl))
            context.startActivity(intent)
        }
    }
}