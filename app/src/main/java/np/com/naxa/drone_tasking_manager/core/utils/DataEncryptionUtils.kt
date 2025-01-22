package np.com.naxa.drone_tasking_manager.core.utils

import android.util.Base64
import np.com.naxa.drone_tasking_manager.BuildConfig
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

// Main encryption utility class
object DataUtils {
    private const val ALGORITHM = BuildConfig.ENCRYPTION_ALGORITHM
    private const val KEY_ALGORITHM = BuildConfig.ENCRYPTION_ALGORITHM_KEY
    private const val ITERATION_COUNT = 65536
    private const val KEY_LENGTH = 256

    private fun deriveKey(password: String, salt: ByteArray): SecretKey {
        val factory = SecretKeyFactory.getInstance(KEY_ALGORITHM)
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH)
        val tmp = factory.generateSecret(spec)
        return SecretKeySpec(tmp.encoded, "AES")
    }

    object Encrypt {
        fun encryptData(data: String, password: String): String {
            try {
                val salt = ByteArray(16).apply { SecureRandom().nextBytes(this) }
                val iv = ByteArray(16).apply { SecureRandom().nextBytes(this) }
                val key = deriveKey(password, salt)

                val cipher = Cipher.getInstance(ALGORITHM)
                cipher.init(Cipher.ENCRYPT_MODE, key, IvParameterSpec(iv))
                val encrypted = cipher.doFinal(data.toByteArray())

                // Combine salt + IV + encrypted data
                val combined = ByteArray(salt.size + iv.size + encrypted.size)
                System.arraycopy(salt, 0, combined, 0, salt.size)
                System.arraycopy(iv, 0, combined, salt.size, iv.size)
                System.arraycopy(encrypted, 0, combined, salt.size + iv.size, encrypted.size)

                return Base64.encodeToString(combined, Base64.DEFAULT)
            } catch (e: Exception) {
                throw EncryptionException("Error encrypting data", e)
            }
        }
    }

    object Decrypt {
        fun decryptData(encryptedData: String?, password: String): String {
            try {
                // Check for null or empty data
                if (encryptedData.isNullOrEmpty()) {
                    throw EncryptionException("Encrypted data is null or empty")
                }

                // Try to decode Base64
                val combined = try {
                    Base64.decode(encryptedData, Base64.DEFAULT)
                } catch (e: IllegalArgumentException) {
                    throw EncryptionException("Invalid Base64 format", e)
                }

                // Check minimum length (16 bytes salt + 16 bytes IV)
                if (combined.size < 32) {
                    throw EncryptionException(
                        "Invalid encrypted data format: length ${combined.size} is less than minimum 32 bytes"
                    )
                }

                try {
                    // Extract salt, IV and encrypted data
                    val salt = combined.copyOfRange(0, 16)
                    val iv = combined.copyOfRange(16, 32)
                    val encrypted = combined.copyOfRange(32, combined.size)

                    val key = deriveKey(password, salt)

                    val cipher = Cipher.getInstance(ALGORITHM)
                    cipher.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(iv))
                    val decrypted = cipher.doFinal(encrypted)

                    return String(decrypted)
                } catch (e: Exception) {
                    throw EncryptionException("Error during decryption process", e)
                }
            } catch (e: EncryptionException) {
                throw e // Re-throw EncryptionException as is
            } catch (e: Exception) {
                throw EncryptionException("Unexpected error during decryption", e)
            }
        }
    }

    // Custom exception class for encryption/decryption errors
    class EncryptionException(message: String, cause: Throwable? = null) :
        Exception(message, cause)
}