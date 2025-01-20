package np.com.naxa.drone_tasking_manager.core.services.storage

import com.tencent.mmkv.MMKV
import np.com.naxa.drone_tasking_manager.BuildConfig
import np.com.naxa.drone_tasking_manager.core.utils.DataUtils


class MMKVStorageService private constructor() {

    private val mmkv: MMKV = MMKV.defaultMMKV()

    companion object {
        // Singleton instance
        @Volatile
        private var instance: MMKVStorageService? = null

        fun getInstance(): MMKVStorageService {
            return instance ?: synchronized(this) {
                instance ?: MMKVStorageService().also { instance = it }
            }
        }
    }

    // Save data (generic function)
    fun <T> save(key: String, rawValue: T) {

        var value = rawValue

        if(key.equals(StorageKeys.User.ACCESS_TOKEN) || key.equals(StorageKeys.User.REFRESH_TOKEN)){
            rawValue as String
            value = DataUtils.Encrypt.encryptData(rawValue, BuildConfig.SECRET_DATA_KEY) as T
        }

        when (value) {
            is String -> mmkv.encode(key, value)
            is Int -> mmkv.encode(key, value)
            is Long -> mmkv.encode(key, value)
            is Float -> mmkv.encode(key, value)
            is Double -> mmkv.encode(key, value)
            is Boolean -> mmkv.encode(key, value)
            is ByteArray -> mmkv.encode(key, value)
            else -> throw IllegalArgumentException("Unsupported data type for MMKV")
        }
    }

    // Retrieve data (generic function with default value)
    fun <T> get(key: String, defaultValue: T): T {
        return when (defaultValue) {
            is String -> decryptData(key, defaultValue)
            is Int -> mmkv.decodeInt(key, defaultValue) as T
            is Long -> mmkv.decodeLong(key, defaultValue) as T
            is Float -> mmkv.decodeFloat(key, defaultValue) as T
            is Double -> mmkv.decodeDouble(key, defaultValue) as T
            is Boolean -> mmkv.decodeBool(key, defaultValue) as T
            is ByteArray -> mmkv.decodeBytes(key, defaultValue) as T
            else -> throw IllegalArgumentException("Unsupported data type for MMKV")
        }
    }

    private fun<T> decryptData(key: String, defaultValue: String): T {
        var value = mmkv.decodeString(key, defaultValue)!!
        if(key.equals(StorageKeys.User.ACCESS_TOKEN) || key.equals(StorageKeys.User.REFRESH_TOKEN)){
            value = DataUtils.Decrypt.decryptData(value, BuildConfig.SECRET_DATA_KEY)
        }
        return value as T
    }

    // Check if a key exists
    fun contains(key: String): Boolean {
        return mmkv.containsKey(key)
    }

    // Remove a specific key
    fun remove(key: String) {
        mmkv.removeValueForKey(key)
    }

    // Clear all stored data
    fun clear() {
        mmkv.clearAll()
    }
}
