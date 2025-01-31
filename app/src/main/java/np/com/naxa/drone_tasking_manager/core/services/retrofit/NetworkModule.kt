package np.com.naxa.drone_tasking_manager.core.services.retrofit

import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import np.com.naxa.drone_tasking_manager.BuildConfig
import np.com.naxa.drone_tasking_manager.core.services.ApiService
import np.com.naxa.drone_tasking_manager.core.services.retrofit.utils.LongNumDeserializer
import np.com.naxa.drone_tasking_manager.features.projects.dto.project.Outline
import np.com.naxa.drone_tasking_manager.features.projects.dto.project.OutlineDeserializer
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton


/**
 * Dagger Hilt module for providing network-related dependencies.
 * This module is installed in the SingletonComponent, ensuring single instances across the app.
 */
@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {

    /**
     * Provides a singleton instance of OkHttpClient.
     *
     * @return A configured OkHttpClient instance for making HTTP requests
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder().addInterceptor(CacheInterceptor()).build()
    }

    /**
     * Provides a singleton instance of Retrofit.
     *
     * @param okHttpClient The OkHttpClient instance to be used by Retrofit
     * @return A configured Retrofit instance for making API calls
     */
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        val gson = GsonBuilder()
                .registerTypeAdapter(String::class.java, LongNumDeserializer())
                .registerTypeAdapter(Outline::class.java, OutlineDeserializer())
                .create()

        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    /**
     * Provides a singleton instance of ApiService.
     *
     * @param retrofit The Retrofit instance used to create the API service
     * @return An implementation of the ApiService interface
     */
    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }

}