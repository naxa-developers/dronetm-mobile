
sealed class EitherProfileResponseOrError<T>{
    data class Success<UserProfile>(val data: UserProfile): EitherProfileResponseOrError<UserProfile>()
    data class Error<T>(val error: String): EitherProfileResponseOrError<T>()

}