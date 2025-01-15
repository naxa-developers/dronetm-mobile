package np.com.naxa.drone_tasking_manager

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
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
    fun provideOkHttpClient(): OkHttpClient{
        return OkHttpClient.Builder()
            .build()
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
        return Retrofit.Builder()
            .baseUrl("https://dev.dronetm.org/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
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