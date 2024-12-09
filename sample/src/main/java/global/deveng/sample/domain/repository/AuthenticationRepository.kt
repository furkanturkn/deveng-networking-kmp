package global.deveng.sample.domain.repository

import global.deveng.sample.domain.model.Authentication



interface AuthenticationRepository {
    suspend fun authenticate(
        username: String,
        password: String
    ): Authentication?
}