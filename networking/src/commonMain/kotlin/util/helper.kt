package networking.util

import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.headers
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
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
 * Supports both single String values and List<String> for repeated query parameters.
 */
public fun URLBuilder.addQueryParameters(queryParameters: Map<String, Any>?) {
    queryParameters?.forEach { (key, value) ->
        when (value) {
            is String -> parameters.append(key, value)
            is List<*> -> {
                value.forEach { item ->
                    if (item is String) {
                        parameters.append(key, item)
                    }
                    else {
                        parameters.append(key, item.toString())
                    }
                }
            }
            else -> {
                parameters.append(key, value.toString())
            }
        }
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

/**
 * Helper function to create multipart content for file uploads
 */
public fun createMultipartContent(
    fileName: String,
    fileContent: ByteArray,
    fileFieldName: String = "File",
    additionalFields: Map<String, String>? = null,
    mimeType: String? = null
): MultiPartFormDataContent {
    return MultiPartFormDataContent(
        formData {
            // Add file
            append(
                key = fileFieldName,
                value = fileContent,
                headers = Headers.build {
                    append(
                        HttpHeaders.ContentDisposition,
                        "form-data; name=\"$fileFieldName\"; filename=\"$fileName\""
                    )
                    append(
                        HttpHeaders.ContentType, 
                        mimeType ?: detectMimeType(fileName)
                    )
                }
            )
            
            // Add additional fields
            additionalFields?.forEach { (key, value) ->
                append(key, value)
            }
        }
    )
}

/**
 * Detects MIME type based on file extension
 */
public fun detectMimeType(fileName: String): String {
    return when (fileName.substringAfterLast('.').lowercase()) {
        "jpg", "jpeg" -> "image/jpeg"
        "png" -> "image/png"
        "gif" -> "image/gif"
        "pdf" -> "application/pdf"
        "txt" -> "text/plain"
        "json" -> "application/json"
        "xml" -> "application/xml"
        "doc" -> "application/msword"
        "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        "xls" -> "application/vnd.ms-excel"
        "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        "mp4" -> "video/mp4"
        "mp3" -> "audio/mpeg"
        "zip" -> "application/zip"
        else -> "application/octet-stream"
    }
}

public fun HttpRequestBuilder.setupQueryParameters(queryParameters: Map<String, Any>?) {
    url {
        addQueryParameters(queryParameters = queryParameters)
    }
}