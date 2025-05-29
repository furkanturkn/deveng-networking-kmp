package networking.util

import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.headers
import io.ktor.http.HttpMessageBuilder
import io.ktor.http.URLBuilder
import networking.DevengNetworkingModule
import networking.DevengNetworkingModule.exceptionHandler
import networking.DevengNetworkingModule.getCustomHeaders
import networking.DevengNetworkingModule.getRestBaseUrl
import networking.DevengNetworkingModule.getToken

public fun logDebug(
    tag: String? = null,
    message: Any?
) {
    if (DevengNetworkingModule.loggingEnabled) {
        if (tag != null) {
            println("[$tag] $message")
        } else {
            println(message)
        }
    }
}

/**
 * Extension function to add query parameters to the URL builder.
 */
public fun URLBuilder.addQueryParameters(queryParameters: Map<String, String>?) {
    queryParameters?.forEach { (key, value) ->
        parameters.append(key, value)
    }
}

/**
 * Extension function to add path parameters to an endpoint.
 */
public fun String.addPathParameters(pathParameters: Map<String, String>?): String {
    return pathParameters?.entries?.fold(this) { acc, (key, value) ->
        acc.replace("{$key}", value)
    } ?: this
}

/**
 * Extension function to set the authorization header in the request builder.
 */
public fun HttpMessageBuilder.setupAuthorizationHeader(token: String) {
    this.headers {
        append("Authorization", "Bearer $token")
    }

}

/**
 * Extension function to set the locale header in the request builder.
 */
public fun HttpMessageBuilder.setupLocaleHeader(locale: String) {
    this.headers {
        append("language", locale)
    }
}

/**
 * Extension function to set extra custom headers in the request builder.
 */
public fun HttpMessageBuilder.setupCustomHeaders(customHeaders: Map<String, String>) {
    this.headers {
        customHeaders.forEach { (key, value) ->
            append(key, value)
        }
    }
}

public fun HttpMessageBuilder.setupAllHeaders() {
    setupAuthorizationHeader(
        token = getToken()
    )

    if (exceptionHandler?.locale != null) {
        setupLocaleHeader(
            locale = exceptionHandler?.locale.toString()
        )
    }

    setupCustomHeaders(getCustomHeaders())
}

public fun buildRequestUrl(
    endpoint: String,
    pathParameters: Map<String, String>? = null
): String {
    val resolvedEndpoint = endpoint.addPathParameters(pathParameters = pathParameters)
    return "${getRestBaseUrl()}$resolvedEndpoint"
}

public fun HttpRequestBuilder.setupQueryParameters(queryParameters: Map<String, String>?) {
    url {
        addQueryParameters(queryParameters = queryParameters)
    }
}