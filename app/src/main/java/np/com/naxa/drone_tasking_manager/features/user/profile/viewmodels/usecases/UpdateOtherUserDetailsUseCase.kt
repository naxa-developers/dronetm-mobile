package np.com.naxa.drone_tasking_manager.features.user.profile.viewmodels.usecases

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import np.com.naxa.drone_tasking_manager.features.user.profile.repositories.UserProfileRepository
import javax.inject.Inject

@Module
@InstallIn(SingletonComponent::class)
class UpdateOtherUserDetailsUseCase @Inject constructor(private val profileRepository: UserProfileRepository) {
    suspend operator fun invoke(
        certifiedDroneOperator: Boolean,
        droneYouOwn: String,
        experienceYears: Int,
        notifyForProjectsWithinKm: Int,
        registrationCertificateUrl: String,
        registrationFile: String
    ) = profileRepository.updateOtherDetails(
        certifiedDroneOperator,
        droneYouOwn,
        experienceYears,
        notifyForProjectsWithinKm,
        registrationCertificateUrl,
        registrationFile
    )
}