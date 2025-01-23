package np.com.naxa.drone_tasking_manager.features.login.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.result.contract.ActivityResultContract
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Task

class AuthResultContract : ActivityResultContract<Int, Task<GoogleSignInAccount>?>() {
    override fun createIntent(context: Context, input: Int): Intent {
        return getGoogleSignInClient(context).signInIntent
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Task<GoogleSignInAccount>? {

        return when (resultCode) {
            Activity.RESULT_OK -> {
                if (intent == null) {
//                    Log.e("AuthResultContract", "googleLoginActivityResult Intent is null on RESULT_OK")
                    return null
                }

                try {
                    val task = GoogleSignIn.getSignedInAccountFromIntent(intent)

                    // Add more detailed logging
                    task.addOnSuccessListener { account ->
//                        Log.d("AuthResultContract", "googleLoginActivityResult Account retrieved successfully: ${account.email}")
                    }.addOnFailureListener { exception ->
//                        Log.e("AuthResultContract", "googleLoginActivityResult Failed to get signed-in account", exception)
                    }

                    task
                } catch (e: Exception) {
//                    Log.e("AuthResultContract", "googleLoginActivityResult Exception in parsing result", e)
                    null
                }
            }

            Activity.RESULT_CANCELED -> {
//                Log.w("AuthResultContract", "googleLoginActivityResult Google Sign-In was canceled by user")
                null
            }

            else -> {
//                Log.e("AuthResultContract", "googleLoginActivityResult Unexpected result code: $resultCode")
                null
            }
        }
    }

}
