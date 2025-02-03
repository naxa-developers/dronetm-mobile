package np.com.naxa.drone_tasking_manager.core.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.components.SingletonComponent
import np.com.naxa.drone_tasking_manager.features.user.auth.refreshtoken.viewmodels.RefreshTokenViewModel
import np.com.naxa.drone_tasking_manager.features.user.auth.refreshtoken.viewmodels.usecases.RefreshTokenUseCase
import np.com.naxa.drone_tasking_manager.features.user.profile.viewmodels.UserProfileViewModel
import np.com.naxa.drone_tasking_manager.features.user.profile.viewmodels.usecases.FetchUserProfileUseCase
import np.com.naxa.drone_tasking_manager.features.user.profile.viewmodels.usecases.UpdateBasicUserDetailsUseCase
import np.com.naxa.drone_tasking_manager.features.user.profile.viewmodels.usecases.UpdateOtherUserDetailsUseCase
import javax.inject.Singleton

@Module
@InstallIn(ViewModelComponent::class, SingletonComponent::class)
object ViewModelModule {

    @Provides
    fun provideUserProfileViewModel(
        fetchUserProfileUseCase: FetchUserProfileUseCase,
        updateBasicUserDetailsUseCase: UpdateBasicUserDetailsUseCase,
        updateOtherUserDetailsUseCase: UpdateOtherUserDetailsUseCase
    ): UserProfileViewModel {
        return UserProfileViewModel(fetchUserProfileUseCase, updateBasicUserDetailsUseCase, updateOtherUserDetailsUseCase )
    }

    @Provides
    fun provideRefreshTokenViewModel(refreshTokenUseCase: RefreshTokenUseCase): RefreshTokenViewModel {
        return RefreshTokenViewModel(refreshTokenUseCase)
    }

}