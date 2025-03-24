package np.com.naxa.drone_tasking_manager.core.di


import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import np.com.naxa.drone_tasking_manager.features.login.repositories.LoginRepository
import np.com.naxa.drone_tasking_manager.features.login.repositories.LoginRepositoryImpl
import np.com.naxa.drone_tasking_manager.features.projects.repositories.ProjectsRepository
import np.com.naxa.drone_tasking_manager.features.projects.repositories.ProjectsRepositoryImpl
import np.com.naxa.drone_tasking_manager.features.tasks.repositories.OfflineFlightPlanRepository
import np.com.naxa.drone_tasking_manager.features.tasks.repositories.OfflineFlightPlanRepositoryImpl
import np.com.naxa.drone_tasking_manager.features.tasks.repositories.TasksRepository
import np.com.naxa.drone_tasking_manager.features.tasks.repositories.TasksRepositoryImpl
import np.com.naxa.drone_tasking_manager.features.user.auth.refreshtoken.repositories.RefreshTokenRepository
import np.com.naxa.drone_tasking_manager.features.user.auth.refreshtoken.repositories.RefreshTokenRepositoryImpl
import np.com.naxa.drone_tasking_manager.features.user.task_dashboard.repositories.UsersTaskRepository
import np.com.naxa.drone_tasking_manager.features.user.task_dashboard.repositories.UsersTaskRepositoryImpl
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
    abstract fun bindOfflineFlightPlanRepository(
        tasksRepositoryImpl: OfflineFlightPlanRepositoryImpl
    ): OfflineFlightPlanRepository

    /**
     * Binds the concrete implementation [UserProfileRepositoryImpl] to the [UserProfileRepository] interface.
     *
     * This function is part of a Dagger module and is responsible for providing an instance of
     * [UserProfileRepository] when it is requested.  It uses the `@Binds` annotation which tells
     * Dagger to provide the specified implementation type ([UserProfileRepositoryImpl]) whenever the
     * interface type ([UserProfileRepository]) is requested as a dependency.
     *
     * The `@Singleton` annotation indicates that a single instance of the bound implementation
     * ([UserProfileRepositoryImpl]) should be created and reused throughout the application's lifecycle.
     *
     * @param userProfileRepositoryImpl The concrete implementation of the UserProfileRepository interface.
     * @return An instance of UserProfileRepository that is backed by UserProfileRepositoryImpl.
     */
    @Binds
    @Singleton
    abstract fun bindUserProfileRepository(
        userProfileRepositoryImpl: UserProfileRepositoryImpl
    ): UserProfileRepository

    /**
     * This abstract function binds the concrete implementation [RefreshTokenRepositoryImpl]
     * to the [RefreshTokenRepository] interface.
     *
     * It is used within a Dagger module to provide a dependency binding for
     * the RefreshTokenRepository. This allows for the use of dependency injection
     * to obtain an instance of a class implementing the RefreshTokenRepository.
     *
     * The `@Binds` annotation indicates that this method provides a binding.
     * Dagger will use this binding to provide instances of [RefreshTokenRepository]
     * whenever it is requested as a dependency.
     *
     * The `@Singleton` annotation ensures that only a single instance of the
     * bound implementation ([RefreshTokenRepositoryImpl]) will be created and used
     * throughout the application's lifecycle. This is useful for resources like
     * repositories that manage data and should be shared.
     *
     * @param refreshTokenRepositoryImpl The concrete implementation of the
     *                                   [RefreshTokenRepository] interface.
     * @return An instance of [RefreshTokenRepository] that is bound to
     *         [refreshTokenRepositoryImpl].
     */
    @Binds
    @Singleton
    abstract fun bindRefreshTokenRepository(
        refreshTokenRepositoryImpl: RefreshTokenRepositoryImpl
    ): RefreshTokenRepository

    /**
     * This abstract function binds the concrete implementation [UsersTaskRepositoryImpl]
     * to the interface [UsersTaskRepository].
     *
     * Dagger will use this binding to provide an instance of [UsersTaskRepository]
     * whenever it's requested in a dependency graph.
     *
     * @param usersTaskRepositoryImpl The concrete implementation of the [UsersTaskRepository] interface.
     * This instance will be provided by Dagger.
     * @return An instance of [UsersTaskRepository] that Dagger will provide.
     *
     */
    @Binds
    @Singleton
    abstract fun bindUsersTaskRepository(
        usersTaskRepositoryImpl: UsersTaskRepositoryImpl
    ): UsersTaskRepository

}