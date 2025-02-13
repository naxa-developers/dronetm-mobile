package np.com.naxa.drone_tasking_manager.core.di


import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import np.com.naxa.drone_tasking_manager.features.login.repositories.LoginRepository
import np.com.naxa.drone_tasking_manager.features.login.repositories.LoginRepositoryImpl
import np.com.naxa.drone_tasking_manager.features.projects.repositories.ProjectsRepository
import np.com.naxa.drone_tasking_manager.features.projects.repositories.ProjectsRepositoryImpl
import np.com.naxa.drone_tasking_manager.features.tasks.repositories.TasksRepository
import np.com.naxa.drone_tasking_manager.features.tasks.repositories.TasksRepositoryImpl
import np.com.naxa.drone_tasking_manager.features.user.auth.refreshtoken.repositories.RefreshTokenRepository
import np.com.naxa.drone_tasking_manager.features.user.auth.refreshtoken.repositories.RefreshTokenRepositoryImpl
import np.com.naxa.drone_tasking_manager.features.user.dashboard.repositories.UsersTaskRepository
import np.com.naxa.drone_tasking_manager.features.user.dashboard.repositories.UsersTaskRepositoryImpl
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
    abstract fun bindLoginRepository(
        loginRepositoryImpl: LoginRepositoryImpl
    ): LoginRepository

    /**
     * Binds the ProjectsRepositoryImpl to the ProjectsRepository interface.
     *
     * @param projectsRepositoryImpl The implementation of the ProjectsRepository interface.
     * @return The ProjectsRepository interface.
     */
    @Binds
    @Singleton
    abstract fun bindProjectsRepository(
        projectsRepositoryImpl: ProjectsRepositoryImpl
    ): ProjectsRepository

    /**
     * Binds the TasksRepositoryImpl to the TasksRepository interface.
     *
     * @param tasksRepositoryImpl The implementation of the TasksRepository interface.
     * @return The TasksRepository interface.
     */
    @Binds
    @Singleton
    abstract fun bindTasksRepository(
        tasksRepositoryImpl: TasksRepositoryImpl
    ): TasksRepository

    @Binds
    @Singleton
    abstract fun bindUserProfileRepository(
        userProfileRepositoryImpl: UserProfileRepositoryImpl
    ): UserProfileRepository

    @Binds
    @Singleton
    abstract fun bindRefreshTokenRepository(
        refreshTokenRepositoryImpl: RefreshTokenRepositoryImpl
    ): RefreshTokenRepository

    @Binds
    @Singleton
    abstract fun bindUsersTaskRepository(
        usersTaskRepositoryImpl: UsersTaskRepositoryImpl
    ): UsersTaskRepository

}