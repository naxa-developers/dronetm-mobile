package np.com.naxa.drone_tasking_manager.core.utils

import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

object DataUtils {

    private fun stringToSecretKey(keyString: String): SecretKey {

        val keyBytes = keyString.toByteArray() // Ensure the key length is 16, 24, or 32 bytes
         val keyBytesPadded = ByteArray(32) // Use 32 for 256 bits, 16 for 128 bits, 24 for 192 bits
         System.arraycopy(keyBytes, 0, keyBytesPadded, 0, keyBytes.size.coerceAtMost(keyBytesPadded.size))

        return  SecretKeySpec(keyBytesPadded, "AES")
    }

    object Encrypt {
        fun encryptData(data: String, secretKey: String): String {
            val cipher = Cipher.getInstance("AES")
            cipher.init(Cipher.ENCRYPT_MODE, stringToSecretKey(secretKey))
            val encryptedBytes = cipher.doFinal(data.toByteArray())
            return Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
        }

    }

    object Decrypt {
        fun decryptData(encryptedData: String, secretKey: String): String {
            val cipher = Cipher.getInstance("AES")
            cipher.init(Cipher.DECRYPT_MODE, stringToSecretKey(secretKey))
            val decodedBytes = Base64.decode(encryptedData, Base64.DEFAULT)
            val decryptedBytes = cipher.doFinal(decodedBytes)
            return String(decryptedBytes)
        }
    }


}