package global.deveng.sample.data.datasource.remote.model.response

import global.deveng.sample.domain.model.Authentication
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AuthenticationResponse(
    @SerialName("token")
    val token: String
)

fun AuthenticationResponse.toDomain(): Authentication {
    return Authentication(
        token = token
    )
}