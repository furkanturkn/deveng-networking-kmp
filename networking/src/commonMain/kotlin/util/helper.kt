package networking.util

import io.ktor.client.request.headers
import io.ktor.http.HttpMessageBuilder
import io.ktor.http.URLBuilder

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
