package global.deveng.sample.domain.usecase

import global.deveng.sample.domain.model.Authentication
import global.deveng.sample.domain.repository.AuthenticationRepository

class AuthenticateUseCase(
    private val authenticationRepository: AuthenticationRepository
) {
    suspend operator fun invoke(
        params: Params
    ): Authentication? {
        return authenticationRepository.authenticate(
            username = params.username,
            password = params.password
        )

    }

    data class Params(
        val username: String,
        val password: String
    )

}
