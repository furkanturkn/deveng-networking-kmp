# Deveng Networking KMP

The **Deveng Networking KMP** is a Kotlin Multiplatform networking solution designed to simplify REST API communication and WebSocket management.
It provides a unified API for handling network requests, token-based authentication, localization, custom headers, and
advanced error handling with minimal setup. This library is tailored for projects requiring a robust, reusable, 
and multiplatform-friendly networking layer.

---

## Table of Contents
1. [Introduction](#introduction)
2. [Features](#features)
3. [Platform Support](#platform-support)
4. [Installation](#installation)
5. [Quick Start](#quick-start)
6. [Initialization](#initialization)
7. [REST API Calls](#rest-api-calls)
8. [WebSocket Connections](#websocket-connections)
9. [Error Handling](#error-handling)
10. [Advanced Usage](#advanced-usage)
11. [How It Works](#how-it-works)
12. [License](#license)
13. [Contributing](#contributing)

---

## Introduction

The **Deveng Networking KMP** was created to streamline the complexities of managing network requests 
and WebSocket connections in Kotlin Multiplatform projects.
The module integrates with `Ktor` for HTTP client operations and includes robust tools for error handling, token management, custom headers, and localization.

This module aims to:
- Reduce boilerplate code for networking operations.
- Provide customizable error handling for REST API calls and WebSocket connections.
- Offer a flexible and reusable API suitable for any Kotlin Multiplatform project.
- Support custom headers and configurable logging.

---

## Features

- 🌐 **Full REST API Support**: Seamless handling of HTTP methods like GET, POST, PUT, and DELETE.
- 🔄 **WebSocket Client with Connection Management**: Manage WebSocket lifecycle events, such as connection establishment, message handling, and graceful disconnection.
- ⚡ **Efficient WebSocket Connection Pooling**: Optimize resource usage by managing multiple WebSocket connections simultaneously with connection pooling.
- 🎯 **Multiplatform Support**: Fully compatible with Android, iOS, Desktop (JVM), and WebAssembly.
- 🔒 **Token-Based Authentication**: Built-in support for secure API calls.
- 🌍 **Localization**: Localized error messages and header support.
- 🎭 **Error Handling**: Centralized and customizable exception handling.
- 🛠️ **Dynamic Parameters**: Easily manage path and query parameters.
- 📋 **Custom Headers**: Support for adding custom headers to requests.
- 🔧 **Configurable Logging**: Optional logging that can be enabled or disabled.
- 🚀 **Easy Initialization**: Single function initialization with all configuration options.

---

## Platform Support

- 🤖 Android
- 🍎 iOS
- 🖥️ Desktop (JVM)
- 🌐 WebAssembly (WASM)

---

## Installation

### Gradle (build.gradle.kts)
If you are not using a version catalog add the following dependency to your project:
```kotlin
dependencies {
    implementation("global.deveng:networking-kmp:2.6.+")
}
```

### Version Catalog (libs.versions.toml)
If you are using a version catalog add the following to your version Catalog:
```toml
[versions]
devengNetworkingKmp = "2.6.+"

[libraries]
deveng-networking-kmp = { module = "global.deveng:networking-kmp", version.ref = "devengNetworkingKmp" }
```

Then to your build.gradle.kts:
```kotlin
dependencies {
    implementation(libs.deveng.networking.kmp)
}
```

---

## Quick Start

Get up and running in 3 simple steps:

### 1. Initialize the Module

```kotlin
DevengNetworkingModule.initDevengNetworkingModule(
    restBaseUrl = "https://api.example.com",
    socketBaseUrl = "wss://ws.example.com"
)
```

### 2. Make Your First API Call

```kotlin
// Simple GET request
suspend fun getUser(userId: String): User? {
    return DevengNetworkingModule.sendRequest<Unit, User?>(
        endpoint = "/users/{userId}",
        requestMethod = DevengHttpMethod.GET,
        pathParameters = mapOf("userId" to userId)
    )
}
```

### 3. Connect to WebSocket (Optional)

```kotlin
// Simple WebSocket connection
val connection = DevengNetworkingModule.connectToWebSocket(
    endpoint = "/chat",
    onConnected = { sendMessage("Hello!") },
    onMessageReceived = { message -> println("Received: $message") },
    onError = { error -> println("Error: $error") }
)
```

That's it! You're ready to go. For more detailed configuration and advanced features, see the sections below.

---

## Initialization

The module now provides a convenient initialization function that configures all settings in one place:

### Recommended: Single Function Initialization

```kotlin
DevengNetworkingModule.initDevengNetworkingModule(
    restBaseUrl = "https://api.example.com",
    socketBaseUrl = "wss://ws.example.com",
    loggingEnabled = true, // Optional, defaults to true
    token = "your-auth-token", // Optional
    locale = Locale.EN, // Optional
    customHeaders = mapOf( // Optional
        "X-API-Version" to "1.0",
        "X-Client-Platform" to "Android"
    )
)
```

### Alternative: Manual Configuration (Legacy Support)

```kotlin
// Set the base URL for REST API requests
DevengNetworkingModule.setApiBaseUrl("https://api.example.com")

// Set the base URL for WebSocket connections
DevengNetworkingModule.setWebSocketBaseUrl("wss://ws.example.com")

// Optional: Set the authentication token for secured API calls
DevengNetworkingModule.setBearerToken("your-auth-token")

// Optional: Set custom headers for all requests
DevengNetworkingModule.setCustomDnmHeaders(mapOf(
    "X-API-Version" to "1.0",
    "X-Client-Platform" to "Android"
))

// Optional: Set the localization for error messages and headers
DevengNetworkingModule.setLocale(Locale.EN)
```

---

## REST API Calls

The sendRequest function provides a flexible, type-safe way to interact with REST APIs. It uses Kotlin's
generics (<T, R>) to specify the type of the request body and the expected response. Below are examples 
of how to use it for common REST operations. We will use suspend functions because 'sendRequest' is a 
suspend function so it can be called only from a coroutine or another suspend function. Also here are the
example Data Classes we will be using for this section:

```kotlin
data class YourResponseType(val id: String, val name: String, val type: String)
data class CreateResourceRequest(val name: String, val type: String)
data class UpdateResourceRequest(val id: String, val active: Boolean, val value: Double)
data class DeleteResourceRequest(val id:String)
```

```kotlin
// GET Request with path parameters
suspend fun getResourceById(resourceId: String): YourResponseType? {
    val result = DevengNetworkingModule.sendRequest<Unit, YourResponseType?>(
        endpoint = "/resources/{resourceId}",  // API endpoint with dynamic path parameter
        requestMethod = DevengHttpMethod.GET,  // HTTP GET method
        pathParameters = mapOf("resourceId" to resourceId)  // Map of path parameters
    )
    return result
}

// POST Request with body
suspend fun createResource(resourceName: String, resourceType: String) {
    val requestBody = CreateResourceRequest(
        name = resourceName,
        type = resourceType
    )
    DevengNetworkingModule.sendRequest<CreateResourceRequest, Unit>(
        endpoint = "/resources",  // API endpoint
        requestBody = requestBody,  // Request body serialized to JSON
        requestMethod = DevengHttpMethod.POST  // HTTP POST method
    )
}

// GET Request with query parameters
suspend fun searchResources(query: String): List<YourResponseType>? {
    val result = DevengNetworkingModule.sendRequest<Unit, List<YourResponseType>?>(
        endpoint = "/resources/search",  // API endpoint
        requestBody = Unit,
        /* Request body is not required for GET,
        but Unit is provided to match the generic type*/
        requestMethod = DevengHttpMethod.GET,  // HTTP GET method
        queryParameters = mapOf("query" to query)  // Map of query parameters
    )
    return result
}

// PUT Request with path parameters and a body
suspend fun updateResource(resourceId: String, isActive: Boolean, resourceValue: Double) {
    val requestBody = UpdateResourceRequest(
        id = resourceId,
        active = isActive,
        value = resourceValue
    )

    DevengNetworkingModule.sendRequest<UpdateResourceRequest, Unit>(
        endpoint = "/resources/{resourceId}",  // API endpoint with dynamic path parameter
        requestBody = requestBody,  // Request body serialized to JSON
        requestMethod = DevengHttpMethod.PUT,  // HTTP PUT method
        pathParameters = mapOf("resourceId" to resourceId)  // Map of path parameters
    )
}

// DELETE Request
suspend fun deleteResourceById(resourceId: String) {
    DevengNetworkingModule.sendRequest<Unit, Unit>(
        endpoint = "/resources/{resourceId}",  // API endpoint with dynamic path parameter
        requestMethod = DevengHttpMethod.DELETE,  // HTTP DELETE method
        pathParameters = mapOf("resourceId" to resourceId)  // Map of path parameters
    )
}
```

---

## WebSocket Connections

The connectToWebSocket function simplifies real-time communication with WebSocket endpoints.
The module manages connection pooling, state tracking, and error handling:

```kotlin
// Connect to a WebSocket endpoint
val connection = DevengNetworkingModule.connectToWebSocket(
    endpoint = "/realtime",
    onConnected = {
        // Connection established
        sendMessage("Hello Server!") 
        // This message here is usually the handshake with the websocket.
    },
    onMessageReceived = { message ->
        // Handle incoming message
        println("Received: $message")
    },
    onError = { error ->
        // Handle error
        println("Error: $error")
    },
    onClose = {
        // Connection closed
        println("Connection closed")
    }
)

// Send message through the connection
connection.sendMessage("Update request")

// Monitor connection state
scope.launch {
    connection.connectionState.collect { state ->
        when (state) {
            is ConnectionState.Connected -> println("Connected")
            is ConnectionState.Connecting -> println("Connecting...")
            is ConnectionState.Disconnected -> println("Disconnected")
            is ConnectionState.Error -> println("Error: ${state.error}")
        }
    }
}
```

### Managing Multiple WebSocket Connections

The module supports multiple simultaneous WebSocket connections with automatic connection management:

```kotlin
/* 
  Configure maximum simultaneous connections,
  when the max is reached the oldest socket will be closed.
*/
WebSocketConnection.setMaxConnections(3) // Default is 5

// Get current connection stats
val activeConnections = WebSocketConnection.getActiveConnections()
val connectionCount = WebSocketConnection.getConnectionCount()

// Close specific connection using the module methods
DevengNetworkingModule.closeWebSocketConnection("/realtime")

// Close all connections using the module methods
DevengNetworkingModule.closeAllWebSocketConnections()

// Or use WebSocketConnection directly
WebSocketConnection.closeConnection("/realtime")
WebSocketConnection.closeAll()
```

---

## Error Handling

The module provides comprehensive and centralized error handling, with the DevengException class.
All errors can be localized based on the set Locale.:

```kotlin
try {
    val response = DevengNetworkingModule.sendRequest<RequestType, ResponseType>(
        endpoint = "/endpoint",
        requestMethod = DevengHttpMethod.GET
    )
} catch (e: DevengException) {
    // Handle specific error with localized message
    println("Localized Error: ${e.message}")
}
```

### Custom Error Handling

you can implement the `ExceptionHandler` interface to provide custom error handling if needed:

```kotlin
class CustomExceptionHandler : ExceptionHandler {
    override var locale: Locale = Locale.EN

    override fun handleHttpException(
        errorMessage: String?,
        status: HttpStatusCode
    ): DevengUiError {
        // Custom HTTP error handling
    }

    override fun handleNetworkException(
        cause: Throwable
    ): DevengUiError {
        // Custom network error handling
    }
}
```

---

## Advanced Usage

### Custom Headers

The module supports adding custom headers to all requests:

```kotlin
// Set custom headers during initialization
DevengNetworkingModule.initDevengNetworkingModule(
    restBaseUrl = "https://api.example.com",
    socketBaseUrl = "wss://ws.example.com",
    customHeaders = mapOf(
        "X-API-Version" to "1.0",
        "X-Client-Platform" to "Android",
        "X-Device-ID" to "unique-device-id"
    )
)

// Or set them separately
DevengNetworkingModule.setCustomDnmHeaders(mapOf(
    "X-Custom-Header" to "custom-value"
))
```

### Configurable Logging

Control logging output for debugging and production environments:

```kotlin
// Enable logging (default is true)
DevengNetworkingModule.initDevengNetworkingModule(
    restBaseUrl = "https://api.example.com",
    socketBaseUrl = "wss://ws.example.com",
    loggingEnabled = true // Set to false in production if needed
)
```

### Using Custom Serializers

For advanced use cases, you can use custom serializers:

```kotlin
suspend fun customSerializationRequest() {
    val response = DevengNetworkingModule.sendRequest(
        endpoint = "/custom",
        requestBody = customData,
        requestMethod = DevengHttpMethod.POST,
        requestSerializer = CustomData.serializer(),
        responseSerializer = CustomResponse.serializer()
    )
}
```

### Getting Raw HTTP Response

For cases where you need access to the raw HTTP response:

```kotlin
suspend fun getRawResponse() {
    val httpResponse = DevengNetworkingModule.sendRequestForHttpResponse<Unit>(
        endpoint = "/raw",
        requestMethod = DevengHttpMethod.GET
    )
    
    // Access status, headers, etc.
    println("Status: ${httpResponse.status}")
    println("Headers: ${httpResponse.headers}")
}
```

---

## How It Works

The **Deveng Networking KMP** operates as a wrapper around the `Ktor` HTTP client and WebSocket API,
providing simplified functionality for REST API calls and WebSocket connections. Here's an overview 
of its main components:

### 1. REST API Requests (`sendRequest`)
- **Dynamic URL Resolution**:
    - Path parameters are injected into the endpoint using the `addPathParameters` utility.
    - Query parameters are appended to the URL using the `addQueryParameters` utility.
- **Headers Management**:
    - The module automatically adds an `Authorization` header if a Bearer token is set.
    - It also adds a `Locale` header for localization, based on the configured locale in the `ExceptionHandler`.
    - Custom headers are automatically included in all requests.
- **Request Body**:
    - If a request body (`T`) is provided, it is serialized into JSON format, and the `Content-Type: application/json` header is added.
- **Response Handling**:
    - For successful responses (`isSuccess()`), the body is deserialized into the specified type (`R`).
    - For error responses, it attempts to parse the error body into an `ErrorResponse` object.
    - If parsing fails, the module delegates error handling to the `ExceptionHandler`, which generates user-friendly or localized error messages.
- **Error Handling**:
    - Network-related errors (e.g., timeouts) are caught and handled via the `ExceptionHandler`.
    - Custom exceptions (`DevengException`) are thrown for HTTP and network errors.

### 2. WebSocket Connections (`connectToWebSocket`)
- **Connection Setup**:
    - The WebSocket URL is built dynamically by combining the WebSocket base URL and the endpoint.
- **Connection Pooling**:
    - Manages multiple connections with configurable limits.
    - Automatically closes oldest connections when limits are reached.
- **Lifecycle Callbacks**:
    - Provides hooks for:
        - `onConnected`: Triggered when the WebSocket connection is established.
        - `onMessageReceived`: Called whenever a message is received from the server.
        - `onError`: Triggered when an error occurs during communication.
        - `onClose`: Triggered when the WebSocket connection is closed.
- **Error Handling**:
    - Errors encountered during the WebSocket lifecycle are handled using the `ExceptionHandler`, ensuring consistent error messaging across REST and WebSocket functionality.

### 3. Exception Handling
- The module uses the `ExceptionHandler` to:
    - Generate localized error messages.
    - Map HTTP status codes or network errors to user-friendly `DevengException` objects.
    - Handle cases where error bodies cannot be parsed or decoded.

### 4. Initialization and Configuration
- **Single Point Configuration**: The `initDevengNetworkingModule` function provides a centralized way to configure all module settings.
- **Dependency Injection**: Uses internal DI to manage HTTP client and exception handler instances.
- **Logging Control**: Configurable logging that can be enabled or disabled based on environment needs.

By encapsulating these functionalities, the **Deveng Networking KMP** provides a streamlined interface for managing REST API and WebSocket interactions while handling errors in a consistent and customizable manner.

---

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

---

## Contributing

Contributions are welcome! Follow these steps:
1. Fork the repository.
2. Create a branch for your feature or bug fix.
3. Submit a pull request for review. 

For significant changes, please open an issue to discuss your proposal first.

