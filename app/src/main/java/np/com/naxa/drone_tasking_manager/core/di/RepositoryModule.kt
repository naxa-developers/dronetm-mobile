package np.com.naxa.drone_tasking_manager


import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import np.com.naxa.drone_tasking_manager.features.login.repositories.LoginRepository
import np.com.naxa.drone_tasking_manager.features.login.repositories.LoginRepositoryImpl
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

}