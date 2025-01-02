package util

import kotlinx.serialization.Serializable

@Serializable
public data class ErrorResponse(
    public val message: String
)