package global.deveng.sample.data.repository

import global.deveng.sample.data.datasource.remote.AuthenticationService
import global.deveng.sample.data.datasource.remote.model.response.toDomain
import global.deveng.sample.domain.model.Authentication
import global.deveng.sample.domain.repository.AuthenticationRepository

class AuthenticationRepositoryImpl(
    private val authenticationService: AuthenticationService
) : AuthenticationRepository {

    override suspend fun authenticate(
        username: String,
        password: String
    ): Authentication? {
        val apiResponse = authenticationService.authenticate(username, password)

        return apiResponse?.toDomain()
    }

}
