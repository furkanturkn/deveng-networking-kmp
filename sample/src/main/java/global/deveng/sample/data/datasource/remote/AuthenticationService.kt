package global.deveng.sample.data.datasource.remote

import global.deveng.sample.data.datasource.remote.model.response.AuthenticationResponse
import kotlinx.serialization.Serializable
import networking.DevengNetworkingModule
import networking.util.DevengHttpMethod

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
        DevengNetworkingModule.setBaseUrl("https://...")
        val requestBody = AuthenticationRequest(username, password)

        try {
            val result =
                DevengNetworkingModule.sendRequest<AuthenticationRequest, AuthenticationResponse>(
                    endpoint = "/Authentication/login",
                    requestBody = requestBody,
                    requestMethod = DevengHttpMethod.POST
                )

            return result.getOrElse {
                null
            }
        } catch (e: Exception) {
            print(e)
        }

        return null
    }
}