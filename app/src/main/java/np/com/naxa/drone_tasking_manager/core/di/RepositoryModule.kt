package np.com.naxa.drone_tasking_manager.core.di


import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import np.com.naxa.drone_tasking_manager.features.login.repositories.LoginRepository
import np.com.naxa.drone_tasking_manager.features.login.repositories.LoginRepositoryImpl
import np.com.naxa.drone_tasking_manager.features.user.profile.repositories.UserProfileRepository
import np.com.naxa.drone_tasking_manager.features.user.profile.repositories.UserProfileRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    /**
     * Binds the LoginRepositoryImpl to the LoginRepository interface.
     *
     * @param loginRepositoryImpl The implementation of the LoginRepository interface.
     * @return The LoginRepository interface.
     */
    @Binds
    @Singleton
    abstract  fun bindLoginRepository(
        loginRepositoryImpl: LoginRepositoryImpl
    ) : LoginRepository


    @Binds
    @Singleton
    abstract  fun bindUserProfileRepository(
        userProfileRepositoryImpl: UserProfileRepositoryImpl
    ) : UserProfileRepository

}