package np.com.naxa.drone_tasking_manager.core.services.retrofit

import android.net.Uri
import np.com.naxa.drone_tasking_manager.core.services.storage.MMKVStorageService
import np.com.naxa.drone_tasking_manager.core.services.storage.StorageKeys
import np.com.naxa.drone_tasking_manager.core.utils.DataUtils.EncryptionException
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.util.concurrent.TimeUnit

class CacheInterceptor(
    private val storageService: MMKVStorageService = MMKVStorageService.getInstance(),
    private val cacheTimeInDays: Long = storageService.interceptorCacheAge,
) : Interceptor {

    companion object {
        private const val HEADER_CACHE_CONTROL = "Cache-Control"
        private const val HEADER_PRAGMA = "Pragma"
        private const val CACHE_PREFIX = "api_cache_"
        private const val CACHE_TIMESTAMP_SUFFIX = "_timestamp"
    }

    data class CacheEntry(
        val data: String,
        val timestamp: Long
    )


    /**
     * Checks if the given URL contains a "force_refresh" query parameter set to "true".
     *
     * @param url The URL to check.
     * @return True if the "force_refresh" query parameter is present and set to "true", false otherwise.
     */
    private fun isForceRefreshTrue(url: String): Boolean {
        val uri = Uri.parse(url)
        val forceRefresh = uri.getQueryParameter("force_refresh")
        return forceRefresh == "true"
    }


    /**
     * Removes the "force_refresh" query parameter from a given URL.
     *
     * @param url The URL to modify.
     * @return The modified URL without the "force_refresh" query parameter.
     */
    fun removeForceRefreshFromUrl(url: String): String {
        val uri = Uri.parse(url)
        val builder = uri.buildUpon().clearQuery() // Clear all query parameters

        // Add back all query parameters except "force_refresh"
        uri.queryParameterNames.forEach { param ->
            if (param != "force_refresh") {
                builder.appendQueryParameter(param, uri.getQueryParameter(param))
            }
        }

        return builder.build().toString()
    }

    override fun intercept(chain: Interceptor.Chain): Response {

        val requestOriginal = chain.request()

        val forceRefresh = isForceRefreshTrue(requestOriginal.url.toString())
        val requestUrl = removeForceRefreshFromUrl(requestOriginal.url.toString())

        val token: String = try{
            storageService.get(StorageKeys.User.ACCESS_TOKEN, "")
        }catch (ex: EncryptionException){
            ""
        }catch (ex:Exception){
            ""
        }

        val request = if(token.isNotEmpty()) {
            requestOriginal.newBuilder()
            .url(requestUrl)
            .header("Access-Token", storageService.get(StorageKeys.User.ACCESS_TOKEN, ""))
            .build()
        }
        else {
            requestOriginal.newBuilder()
                .url(requestUrl)
                .build()
        }

        // Skip caching for non-GET requests
        if (request.method != "GET") {
            return chain.proceed(request)
        }

        // Generate cache key from request URL
        val cacheKey = CACHE_PREFIX + requestUrl.hashCode()
        val timestampKey = cacheKey + CACHE_TIMESTAMP_SUFFIX

        // Check for cached response
        val cachedData = storageService.get(cacheKey, "")
        val cachedTimestamp = storageService.get(timestampKey, 0L)

        // Check if cache is valid
        if (!forceRefresh && (cachedData ).isNotEmpty() && !isCacheExpired(cachedTimestamp)) {
            return buildCachedResponse(request, cachedData)
        }

        // Proceed with network request
        val networkResponse = chain.proceed(request)

        // Cache the new response if successful
        if (networkResponse.isSuccessful) {
            cacheResponse(networkResponse, cacheKey, timestampKey)
        }

        return networkResponse
    }

    private fun isCacheExpired(timestamp: Long): Boolean {
        val currentTime = System.currentTimeMillis()
        val expirationTime = timestamp + TimeUnit.DAYS.toMillis(cacheTimeInDays)
        return currentTime > expirationTime
    }

    private fun buildCachedResponse(request: okhttp3.Request, cachedData: String): Response {
        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body(cachedData.toResponseBody(null))
            .header(HEADER_CACHE_CONTROL, "max-age=${TimeUnit.MINUTES.toSeconds(cacheTimeInDays)}")
            .build()
    }

    private fun cacheResponse(response: Response, cacheKey: String, timestampKey: String) {
        response.body.string().let { responseData ->
            storageService.save(cacheKey, responseData)
            storageService.save(timestampKey, System.currentTimeMillis())

            // Create a new response since the original body was consumed
            val newBody = responseData.toResponseBody(response.body.contentType())
            Response.Builder()
                .request(response.request)
                .protocol(response.protocol)
                .code(response.code)
                .message(response.message)
                .headers(response.headers)
                .body(newBody)
                .build()
        }
    }

    fun clearCache() {
        storageService.getAllKeys()?.forEach { key ->
            if (key.startsWith(CACHE_PREFIX)) {
                storageService.remove(key)
            }
        }
    }

    fun clearCacheForUrl(url: String) {
        val cacheKey = CACHE_PREFIX + url.hashCode()
        val timestampKey = cacheKey + CACHE_TIMESTAMP_SUFFIX
        storageService.remove(cacheKey)
        storageService.remove(timestampKey)
    }
}

// Extension function to add cache control headers
fun okhttp3.Request.Builder.cacheControl(maxAgeSeconds: Int): okhttp3.Request.Builder {
    return header(
        "Cache-Control",
        "public, max-age=$maxAgeSeconds"
    )
}