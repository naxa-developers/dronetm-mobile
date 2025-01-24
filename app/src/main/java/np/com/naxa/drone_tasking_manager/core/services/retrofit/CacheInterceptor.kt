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

/**
 * [CacheInterceptor] is an OkHttp interceptor responsible for caching network responses
 * to improve performance and reduce network requests. It leverages the [MMKVStorageService]
 * for persistent storage of cached data.
 *
 * This interceptor provides the following functionalities:
 *  - **Caching GET Requests:** Caches successful GET requests based on the URL.
 *  - **Force Refresh:** Allows bypassing the cache for a specific request by appending
 *    `force_refresh=true` to the URL query parameters.
 *  - **Cache Expiration:** Manages cache expiration based on the `interceptorCacheAge`
 *    property of [MMKVStorageService].
 *  - **Token Handling:** Adds an access token to requests if available.
 *  - **Cache Clearing:** Provides methods to clear the entire cache or cache for a specific URL.
 *
 * @property storageService The [MMKVStorageService] instance used for storing and retrieving
 *                           cached data. Defaults to a singleton instance.
 */
class CacheInterceptor(
    private val storageService: MMKVStorageService = MMKVStorageService.getInstance(),
) : Interceptor {

    /**
     * Constant representing the HTTP header field for cache control directives.
     * Used to specify how caching mechanisms should behave.
     */
    companion object {
        private const val HEADER_CACHE_CONTROL = "Cache-Control"
        private const val CACHE_PREFIX = "api_cache_"
        private const val CACHE_TIMESTAMP_SUFFIX = "_timestamp"
    }

    /**
     * Intercepts network requests to implement a caching mechanism with optional force refresh.
     *
     * This interceptor performs the following actions:
     * 1. **Token Injection:** Adds an "access-token" header to the request if a token is available.
     * 2. **Force Refresh Handling:** Checks for a "forceRefresh" flag in the URL. If present, it's removed from the URL and the cache is bypassed.
     * 3. **Caching Logic:**
     *    - **Cache Retrieval:** If not forcing refresh and the request is a GET request, it attempts to retrieve a cached response based on the URL.
     *    - **Cache Bypass:** If the request method is not GET, it skips caching entirely.
     *    - **Network Request:** If a cached response is not found (or force refresh is enabled), it proceeds with the network request.
     *    - **Cache Storage:** If the network request is successful, the response body is cached along with a timestamp.
     * 4. **Response Modification:** Modifies the response to include cache-control headers for indicating the maximum age of the response.
     * 5. Response body is cloned to avoid issues where the body is already consumed.
     *
     * @param chain The interceptor chain.
     * @return The network response or a cached response if available and valid.
     *
     * @throws Exception if an error occurs during the process
     */
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val originalUrl = originalRequest.url.toString()

        val forceRefresh = isForceRefreshTrue(originalUrl)
        val sanitizedUrl = removeForceRefreshFromUrl(originalUrl)

        val token = getTokenSafely()

        val modifiedRequest = originalRequest.newBuilder()
            .url(sanitizedUrl)
            .apply {
                if (token.isNotEmpty()) {
                    addHeader("access-token", token)
                }
            }
            .build()

        // Skip caching for non-GET requests
        if (modifiedRequest.method != "GET") {
            return chain.proceed(modifiedRequest)
        }

        val cacheKey = generateCacheKey(sanitizedUrl)
        val timestampKey = generateTimestampKey(cacheKey)

        if (!forceRefresh) {
            val cachedResponse = getCachedResponse(modifiedRequest, cacheKey, timestampKey)
            if (cachedResponse != null) return cachedResponse
        }

        // Proceed with network request
        val networkResponse = chain.proceed(modifiedRequest)

        // Clone response body for caching and returning
        val responseBody = networkResponse.body ?: return networkResponse
        val responseBodyString = responseBody.string()

        // Cache response if successful
        if (networkResponse.isSuccessful) {
            cacheResponse(responseBodyString, cacheKey, timestampKey)
        }

        return networkResponse.newBuilder()
            .body(responseBodyString.toResponseBody(responseBody.contentType()))
            .header(
                HEADER_CACHE_CONTROL,
                "max-age=${TimeUnit.MINUTES.toSeconds(storageService.interceptorCacheAge)}"
            )
            .build()
    }


    /**
     * Checks if the "force_refresh" query parameter in the given URL is set to "true".
     *
     * This function parses the provided URL and extracts the value of the "force_refresh"
     * query parameter. It then compares this value (case-sensitively) with the string "true".
     *
     * @param url The URL string to check.
     * @return `true` if the "force_refresh" query parameter is present and its value is "true",
     *         `false` otherwise (including cases where the parameter is absent or its value
     *         is not "true").
     * @throws NullPointerException if the URL string is null
     * @throws UnsupportedOperationException if the URL is invalid and can not be parsed by Uri.parse()
     *
     * Example:
     * ```kotlin
     * val url1 = "https://example.com/api/data?force_refresh=true"
     * val result1 = isForceRefreshTrue(url1) // result1 will be true
     *
     * val url2 = "https://example.com/api/data?force_refresh=false"
     * val result2 = isForceRefreshTrue(url2) // result2 will be false
     *
     * val url3 = "https://example.com/api/data"
     * val result3 = isForceRefreshTrue(url3) // result3 will be false
     *
     * val url4 = "https://example.com/api/data?force_refresh=TRUE"
     * val result4 = isForceRefreshTrue(url4) // result4 will be false (case-sensitive)
     *
     * val url5 = ""
     * try{
     *      val result5 = isForceRefreshTrue(url5)
     * } catch (exception: UnsupportedOperationException){
     *      // handle exception, result5 won't be assigned
     * }
     * ```
     */
    private fun isForceRefreshTrue(url: String): Boolean {
        return Uri.parse(url).getQueryParameter("force_refresh") == "true"
    }

    /**
     * Removes the "force_refresh" query parameter from a given URL.
     *
     * This function takes a URL string as input, parses it into a Uri,
     * removes the "force_refresh" query parameter if it exists, and then
     * reconstructs the URL string without this parameter. All other query
     * parameters are preserved.
     *
     * @param url The URL string to process.
     * @return The URL string with the "force_refresh" query parameter removed, or the original URL if the parameter was not present.
     */
    private fun removeForceRefreshFromUrl(url: String): String {
        val uri = Uri.parse(url)
        val builder = uri.buildUpon().clearQuery()
        uri.queryParameterNames
            .filter { it != "force_refresh" }
            .forEach { builder.appendQueryParameter(it, uri.getQueryParameter(it)) }
        return builder.build().toString()
    }

    /**
     * Retrieves the user's access token from secure storage.
     *
     * This function attempts to retrieve the access token using the `storageService`.
     * It handles potential `EncryptionException` and generic `Exception` during the retrieval process.
     *
     * @return The user's access token if successfully retrieved, or an empty string if any error occurs or the token is not found.
     *
     * @throws EncryptionException If there's an error during decryption or accessing encrypted data.
     * @throws Exception If any other error occurs during the token retrieval process.
     */
    private fun getTokenSafely(): String {
        return try {
            storageService.get(StorageKeys.User.ACCESS_TOKEN, "")
        } catch (ex: EncryptionException) {
            ""
        } catch (ex: Exception) {
            ""
        }
    }

    /**
     * Generates a unique cache key based on the provided URL.
     *
     * This function takes a URL as input and generates a cache key by combining a predefined
     * prefix (`CACHE_PREFIX`) with the hash code of the URL. This ensures that each URL has a
     * distinct cache key, allowing for proper storage and retrieval of cached data associated
     * with that URL.
     *
     * @param url The URL for which to generate a cache key.
     * @return A unique string representing the cache key for the given URL.
     *
     * @see CACHE_PREFIX
     */
    private fun generateCacheKey(url: String): String = CACHE_PREFIX + url.hashCode()

    /**
     * Generates a timestamp-specific cache key by appending a predefined suffix to the given base key.
     *
     * This function is used to create unique cache keys that can be invalidated based on a timestamp,
     * allowing for time-based cache expiration or updates. By adding a suffix, we can differentiate
     * between a base key representing the data and a specific version tied to a particular timestamp.
     *
     * @param cacheKey The base cache key to which the timestamp suffix will be appended.
     *                 This key should represent the data being cached.
     * @return A new cache key string that includes the original key and the timestamp suffix.
     *         This new key can be used for operations that need to consider timestamp-based cache behavior.
     *
     * @see CACHE_TIMESTAMP_SUFFIX
     */
    private fun generateTimestampKey(cacheKey: String): String = cacheKey + CACHE_TIMESTAMP_SUFFIX

    /**
     * Checks if the cache entry associated with the given timestamp has expired.
     *
     * The expiration time is calculated by adding the configured cache age (in days)
     * to the provided timestamp. The current time is then compared to the calculated
     * expiration time to determine if the cache has expired.
     *
     * @param timestamp The timestamp (in milliseconds) when the cache entry was created.
     * @return `true` if the cache has expired, `false` otherwise.
     */
    private fun isCacheExpired(timestamp: Long): Boolean {
        val currentTime = System.currentTimeMillis()
        val expirationTime = timestamp + TimeUnit.DAYS.toMillis(storageService.interceptorCacheAge)
        return currentTime > expirationTime
    }

    /**
     * Retrieves a cached response from the storage service if it exists and is not expired.
     *
     * This function checks the storage service for cached data associated with the provided `cacheKey`.
     * If data is found and the corresponding timestamp (identified by `timestampKey`) indicates
     * that the cache is still valid (not expired), a Response object is built from the cached data
     * and returned. Otherwise, `null` is returned, signaling that no valid cached response was found.
     *
     * @param request The original HTTP request associated with the cached response. This is needed to build a valid
     *                response object from cached data.
     * @param cacheKey The key used to identify the cached response data in the storage service.
     * @param timestampKey The key used to identify the timestamp associated with the cached data in the storage service.
     * @return A [Response] object built from the cached data if a valid cache entry exists, otherwise `null`.
     *
     * @see isCacheExpired
     * @see buildResponseFromCache
     */
    private fun getCachedResponse(
        request: okhttp3.Request,
        cacheKey: String,
        timestampKey: String
    ): Response? {
        val cachedData = storageService.get(cacheKey, "")
        val cachedTimestamp = storageService.get(timestampKey, 0L)

        return if (cachedData.isNotEmpty() && !isCacheExpired(cachedTimestamp)) {
            buildResponseFromCache(request, cachedData)
        } else null
    }

    /**
     * Builds an OkHttp [Response] object from cached data.
     *
     * This function constructs a simulated HTTP response that mimics a successful request,
     * but serves the data from the provided cache instead of a network call. It sets the
     * appropriate headers to indicate that the response is from the cache, including the
     * `Cache-Control` header with a `max-age` value based on the configured cache age.
     *
     * @param request The original [okhttp3.Request] that this response is associated with.
     * @param cachedData The cached data as a [String] that will be used as the response body.
     * @return A constructed [Response] object representing the cached response.
     */
    private fun buildResponseFromCache(request: okhttp3.Request, cachedData: String): Response {
        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body(cachedData.toResponseBody(null))
            .header(
                HEADER_CACHE_CONTROL,
                "max-age=${TimeUnit.MINUTES.toSeconds(storageService.interceptorCacheAge)}"
            )
            .build()
    }

    /**
     * Caches a response body and its associated timestamp in the storage service.
     *
     * This function saves the provided `responseBody` under the given `cacheKey`
     * and the current timestamp under the `timestampKey`. It utilizes the
     * `storageService` for persistent storage. If `responseBody` is null, no
     * caching operation is performed.
     *
     * @param responseBody The response data to be cached as a String. Can be null.
     * @param cacheKey The key under which the `responseBody` will be stored.
     * @param timestampKey The key under which the current timestamp will be stored.
     *                    This is useful for managing cache expiration.
     *
     * @see storageService
     */
    private fun cacheResponse(responseBody: String?, cacheKey: String, timestampKey: String) {
        responseBody?.let { responseData ->
            storageService.save(cacheKey, responseData)
            storageService.save(timestampKey, System.currentTimeMillis())
        }
    }

    /**
     * Clears all cached data stored in the storage service.
     *
     * This function iterates through all keys in the storage service and removes any key
     * that starts with the defined `CACHE_PREFIX`. This effectively clears out all data
     * that has been previously identified as cached.
     *
     * Note:
     * - It relies on the `storageService` to provide the functionality for retrieving all keys
     *   and removing specific keys.
     * - It uses `CACHE_PREFIX` to identify cache-related keys. Ensure that this constant
     *   is properly defined and used consistently throughout the application for caching.
     * - If `storageService.getAllKeys()` returns null, the function will effectively do nothing,
     *   as the filter and forEach operations will not be executed.
     */
    fun clearAllCache() {
        storageService.getAllKeys()
            ?.filter { it.startsWith(CACHE_PREFIX) }
            ?.forEach { storageService.remove(it) }
    }

    /**
     * Clears the cached data and associated timestamp for a given URL.
     *
     * This function removes both the cached data and the corresponding timestamp
     * that are stored using the provided URL as a basis for the cache key.
     * It utilizes the `generateCacheKey` and `generateTimestampKey` functions to
     * derive the specific storage keys for the data and its timestamp, respectively.
     *
     * @param url The URL for which the cached data and timestamp should be cleared.
     *            This URL is used to generate the cache key.
     * @throws Exception if any error occurs during the removal of the cached data or timestamp.
     *                  The underlying storage service may throw exceptions if, for instance, there's a problem with the storage medium.
     *
     * @see generateCacheKey
     * @see generateTimestampKey
     * @see storageService
     */
    fun clearCacheForUrl(url: String) {
        val cacheKey = generateCacheKey(url)
        val timestampKey = generateTimestampKey(cacheKey)
        storageService.remove(cacheKey)
        storageService.remove(timestampKey)
    }
}


/**
 * Adds a `Cache-Control` header to the request with a `public` directive and a specified `max-age`.
 *
 * This function is a convenience method for setting the `Cache-Control` header to control how the
 * response should be cached by intermediate caches (like CDNs) and the client's cache.
 *
 * The `public` directive indicates that the response can be cached by any cache (both shared
 * and private).
 *
 * The `max-age` directive specifies the maximum time in seconds that a response can be considered
 * fresh. After this time, caches must revalidate the response with the origin server.
 *
 * @param maxAgeSeconds The maximum number of seconds that the response can be considered fresh.
 * @return The same [okhttp3.Request.Builder] instance, allowing for method chaining.
 *
 * @sample
 * ```kotlin
 * val request = Request.Builder()
 *     .url("https://example.com/api/data")
 *     .withCacheControl(3600) // Cache the response for 1 hour (3600 seconds)
 *     .build()
 * ```
 */
fun okhttp3.Request.Builder.withCacheControl(maxAgeSeconds: Int): okhttp3.Request.Builder {
    return header("Cache-Control", "public, max-age=$maxAgeSeconds")
}
