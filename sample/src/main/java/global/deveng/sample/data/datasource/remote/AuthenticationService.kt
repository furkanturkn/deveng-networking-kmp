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

@Serializable
data class CurrencySymbolsResponse(
    val id: Int,
    val symbolId: String,
    val lastUpdateTime: String,
    val isPinned: Boolean,
    val isListed: Boolean,
    val manipulatedPercentage: Double,
    val manipulatedValue: Double,
    val manipulatedCoefficients: Double,
    val symbolTypeId: Double,
    val symbolTypeName: String
)

class AuthenticationService {
    suspend fun authenticate(
        username: String,
        password: String
    ): AuthenticationResponse? {
        val requestBody = AuthenticationRequest(username, password)

        try {
            /*
            val result =
                DevengNetworkingModule.sendRequest<Unit, List<CurrencySymbolsResponse>?>(
                    endpoint = "/Symbols/all/{symbolTypeId}",
                    requestBody = Unit,
                    requestMethod = DevengHttpMethod.GET,
                    pathParameters = mapOf("symbolTypeId" to "1")
                )


             */


            val result = DevengNetworkingModule.sendRequest<AuthenticationRequest, AuthenticationResponse?>(
                endpoint = "/Authentication/login",
                requestBody = requestBody,
                requestMethod = DevengHttpMethod.POST
            )
            return result
        } catch (e: Exception) {
            print(e.message)
        }

        return null
    }
}