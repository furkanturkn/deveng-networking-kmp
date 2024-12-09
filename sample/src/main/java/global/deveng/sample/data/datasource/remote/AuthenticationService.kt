package global.deveng.sample.data.datasource.remote

import global.deveng.sample.data.datasource.remote.model.response.AuthenticationResponse
import io.ktor.http.HttpMethod
import kotlinx.serialization.Serializable
import networking.DevengNetworkingModule

@Serializable
data class AuthenticationRequest(
    val username: String,
    val password: String
)

class AuthenticationService {
    suspend fun authenticate(
        username: String,
        password: String
    ): AuthenticationResponse? {
        DevengNetworkingModule.setBaseUrl("https://burzemanagementapi.sekompos.com/api")
        val requestBody = AuthenticationRequest(username, password)

        val result = DevengNetworkingModule.sendRequest<AuthenticationRequest, AuthenticationResponse>(
            endpoint = "/Authentication/login",
            requestBody = requestBody,
            requestMethod = HttpMethod.Post
        )

        return result.getOrElse {
            null
        }
    }
}