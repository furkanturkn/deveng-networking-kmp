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

The **Deveng Networking KMP** was created by [Deveng Group](https://github.com/Deveng-Group) to streamline the complexities of managing network requests 
and WebSocket connections in Kotlin Multiplatform projects.
The module integrates with `Ktor` for HTTP client operations and includes robust tools for error handling, token management, custom headers, and localization.

This module aims to:
- Reduce boilerplate code for networking operations.
- Provide customizable error handling for REST API calls and WebSocket connections.
- Offer a flexible and reusable API suitable for any Kotlin Multiplatform project.
- Support custom headers and configurable logging.

**Learn more about Deveng Group**: [deveng.global](https://deveng.global)

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
- ⏱️ **Timeout Configuration**: Configurable request, connection, and socket timeouts.
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

> **⚠️ Important Note**: The inline version of `sendRequest` requires **Gradle Java version 21+**. If you're using an older Java version, use the custom serializer version instead (see [Alternative for Older Java Versions](#alternative-for-older-java-versions) below).

### 1. Initialize the Module

```kotlin
val config = DevengNetworkingConfig(
    loggingEnabled = true,
    requestTimeoutMillis = 30_000L,  // 30 seconds
    token = "your-auth-token",
    socketBaseUrl = "wss://ws.example.com" // Optional, only needed for WebSocket
)

DevengNetworkingModule.initDevengNetworkingModule(
    restBaseUrl = "https://api.example.com",
    config = config
)
```

### 2. Make Your First API Call

```kotlin
// Simple GET request (requires Java 21+)
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

### Alternative for Older Java Versions

If you're using **Java version < 21**, use the custom serializer version:

```kotlin
// For Java versions below 21
suspend fun getUser(userId: String): User {
    return DevengNetworkingModule.sendRequest(
        endpoint = "/users/{userId}",
        requestMethod = DevengHttpMethod.GET,
        pathParameters = mapOf("userId" to userId),
        requestSerializer = null, // No request body
        responseSerializer = User.serializer()
    )
}
```

That's it! You're ready to go. For more detailed configuration and advanced features, see the sections below.

---

## Initialization

The module uses a clean configuration data class approach for initialization:

### Configuration Data Class

```kotlin
val config = DevengNetworkingConfig(
    loggingEnabled = true, // Optional, defaults to true
    requestTimeoutMillis = 60_000L, // Optional, defaults to 60 seconds
    connectTimeoutMillis = 10_000L, // Optional, defaults to 10 seconds
    socketTimeoutMillis = 60_000L,  // Optional, defaults to 60 seconds
    token = "your-auth-token", // Optional
    locale = Locale.EN, // Optional
    customHeaders = mapOf( // Optional
        "X-API-Version" to "1.0",
        "X-Client-Platform" to "Android"
    ),
    socketBaseUrl = "wss://ws.example.com" // Optional, only needed for WebSocket connections
)

DevengNetworkingModule.initDevengNetworkingModule(
    restBaseUrl = "https://api.example.com",
    config = config
)
```

### Timeout Configuration

Configure timeouts to handle slow or unresponsive servers:

```kotlin
// Configuration for file upload scenarios
val uploadConfig = DevengNetworkingConfig(
    requestTimeoutMillis = 300_000L,  // 5 minutes for large uploads
    connectTimeoutMillis = 15_000L,   // 15 seconds to connect
    socketTimeoutMillis = 120_000L    // 2 minutes between data packets
)

DevengNetworkingModule.initDevengNetworkingModule(
    restBaseUrl = "https://api.example.com",
    config = uploadConfig
)

// Or set them separately after initialization
DevengNetworkingModule.setTimeouts(
    requestTimeoutMillis = 120_000L,  // 2 minutes for file uploads
    connectTimeoutMillis = 15_000L,   // 15 seconds connect timeout
    socketTimeoutMillis = 90_000L     // 1.5 minutes socket timeout
)

// Set individual timeout values
DevengNetworkingModule.setRequestTimeout(30_000L)  // 30 seconds
DevengNetworkingModule.setConnectTimeout(8_000L)   // 8 seconds
DevengNetworkingModule.setSocketTimeout(60_000L)   // 60 seconds
```

**Timeout Types Explained:**
- **Request Timeout**: Total time from sending request to receiving complete response
- **Connect Timeout**: Time allowed to establish initial connection with server
- **Socket Timeout**: Maximum time of inactivity between data packets

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

### 4. Initialization and Configuration
- **Configuration Data Class**: The `DevengNetworkingConfig` provides a clean, type-safe way to configure all module settings.
- **Single Point Configuration**: All settings are configured in one place for better maintainability.
- **Dependency Injection**: Uses internal DI to manage HTTP client and exception handler instances.
- **Logging Control**: Configurable logging that can be enabled or disabled based on environment needs.

### Common Configuration Examples

```kotlin
// Development Configuration
val devConfig = DevengNetworkingConfig(
    loggingEnabled = true,
    requestTimeoutMillis = 120_000L, // Longer timeouts for debugging
    token = "dev-token-123",
    socketBaseUrl = "wss://dev-ws.example.com"
)

DevengNetworkingModule.initDevengNetworkingModule(
    restBaseUrl = "https://dev-api.example.com",
    config = devConfig
)

// Production Configuration
val prodConfig = DevengNetworkingConfig(
    loggingEnabled = false, // Disable logging in production
    requestTimeoutMillis = 30_000L,
    customHeaders = mapOf(
        "X-API-Version" to "v1",
        "X-Client-Version" to "2.1.0"
    ),
    socketBaseUrl = "wss://ws.example.com"
)

DevengNetworkingModule.initDevengNetworkingModule(
    restBaseUrl = "https://api.example.com",
    config = prodConfig
)

// File Upload Configuration
val uploadConfig = DevengNetworkingConfig(
    requestTimeoutMillis = 600_000L, // 10 minutes for large files
    connectTimeoutMillis = 30_000L,
    socketTimeoutMillis = 300_000L
)

DevengNetworkingModule.initDevengNetworkingModule(
    restBaseUrl = "https://upload.example.com",
    config = uploadConfig
)
```

---

## REST API Calls

> **⚠️ Java Version Compatibility**: 
> - **Inline version** (with `<T, R>` generics): Requires **Java 21+**
> - **Custom serializer version**: Works with **any Java version**

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

### Inline Version (Java 21+ Required)

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

### Custom Serializer Version (Any Java Version)

If you're using Java version below 21, use these examples instead:

```kotlin
// GET Request with path parameters
suspend fun getResourceById(resourceId: String): YourResponseType {
    return DevengNetworkingModule.sendRequest(
        endpoint = "/resources/{resourceId}",
        requestMethod = DevengHttpMethod.GET,
        pathParameters = mapOf("resourceId" to resourceId),
        requestSerializer = null, // No request body
        responseSerializer = YourResponseType.serializer()
    )
}

// POST Request with body
suspend fun createResource(resourceName: String, resourceType: String) {
    val requestBody = CreateResourceRequest(
        name = resourceName,
        type = resourceType
    )
    DevengNetworkingModule.sendRequest(
        endpoint = "/resources",
        requestBody = requestBody,
        requestMethod = DevengHttpMethod.POST,
        requestSerializer = CreateResourceRequest.serializer(),
        responseSerializer = Unit.serializer()
    )
}

// GET Request with query parameters
suspend fun searchResources(query: String): List<YourResponseType> {
    return DevengNetworkingModule.sendRequest(
        endpoint = "/resources/search",
        requestMethod = DevengHttpMethod.GET,
        queryParameters = mapOf("query" to query),
        requestSerializer = null, // No request body
        responseSerializer = ListSerializer(YourResponseType.serializer())
    )
}

// PUT Request with path parameters and a body
suspend fun updateResource(resourceId: String, isActive: Boolean, resourceValue: Double) {
    val requestBody = UpdateResourceRequest(
        id = resourceId,
        active = isActive,
        value = resourceValue
    )

    DevengNetworkingModule.sendRequest(
        endpoint = "/resources/{resourceId}",
        requestBody = requestBody,
        requestMethod = DevengHttpMethod.PUT,
        pathParameters = mapOf("resourceId" to resourceId),
        requestSerializer = UpdateResourceRequest.serializer(),
        responseSerializer = Unit.serializer()
    )
}

// DELETE Request
suspend fun deleteResourceById(resourceId: String) {
    DevengNetworkingModule.sendRequest(
        endpoint = "/resources/{resourceId}",
        requestMethod = DevengHttpMethod.DELETE,
        pathParameters = mapOf("resourceId" to resourceId),
        requestSerializer = null, // No request body
        responseSerializer = Unit.serializer()
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
// Set custom headers using configuration data class
val config = DevengNetworkingConfig(
    customHeaders = mapOf(
        "X-API-Version" to "1.0",
        "X-Client-Platform" to "Android",
        "X-Device-ID" to "unique-device-id"
    )
)

DevengNetworkingModule.initDevengNetworkingModule(
    restBaseUrl = "https://api.example.com",
    config = config
)

// Or set them separately after initialization
DevengNetworkingModule.setCustomDnmHeaders(mapOf(
    "X-Custom-Header" to "custom-value"
))
```

### Configurable Logging

Control logging output for debugging and production environments:

```kotlin
// Production configuration with logging disabled
val prodConfig = DevengNetworkingConfig(
    loggingEnabled = false // Disable logging in production
)

DevengNetworkingModule.initDevengNetworkingModule(
    restBaseUrl = "https://api.example.com",
    config = prodConfig
)

// Development configuration with logging enabled
val devConfig = DevengNetworkingConfig(
    loggingEnabled = true, // Enable detailed logging for debugging
    requestTimeoutMillis = 120_000L, // Longer timeouts for debugging
    socketBaseUrl = "wss://dev-ws.example.com"
)

DevengNetworkingModule.initDevengNetworkingModule(
    restBaseUrl = "https://dev-api.example.com",
    config = devConfig
)
```

### Timeout Configuration

Configure timeouts to handle slow or unresponsive servers:

```kotlin
// Configuration for file upload scenarios
val uploadConfig = DevengNetworkingConfig(
    requestTimeoutMillis = 300_000L,  // 5 minutes for large uploads
    connectTimeoutMillis = 15_000L,   // 15 seconds to connect
    socketTimeoutMillis = 120_000L    // 2 minutes between data packets
)

DevengNetworkingModule.initDevengNetworkingModule(
    restBaseUrl = "https://api.example.com",
    config = uploadConfig
)

// Or set them separately after initialization
DevengNetworkingModule.setTimeouts(
    requestTimeoutMillis = 120_000L,  // 2 minutes for file uploads
    connectTimeoutMillis = 15_000L,   // 15 seconds connect timeout
    socketTimeoutMillis = 90_000L     // 1.5 minutes socket timeout
)

// Set individual timeout values
DevengNetworkingModule.setRequestTimeout(30_000L)  // 30 seconds
DevengNetworkingModule.setConnectTimeout(8_000L)   // 8 seconds
DevengNetworkingModule.setSocketTimeout(60_000L)   // 60 seconds
```

**Timeout Types Explained:**
- **Request Timeout**: Total time from sending request to receiving complete response
- **Connect Timeout**: Time allowed to establish initial connection with server
- **Socket Timeout**: Maximum time of inactivity between data packets

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
- **Configuration Data Class**: The `DevengNetworkingConfig` provides a clean, type-safe way to configure all module settings.
- **Single Point Configuration**: All settings are configured in one place for better maintainability.
- **Dependency Injection**: Uses internal DI to manage HTTP client and exception handler instances.
- **Logging Control**: Configurable logging that can be enabled or disabled based on environment needs.

### Common Configuration Examples

```kotlin
// Development Configuration
val devConfig = DevengNetworkingConfig(
    loggingEnabled = true,
    requestTimeoutMillis = 120_000L, // Longer timeouts for debugging
    token = "dev-token-123",
    socketBaseUrl = "wss://dev-ws.example.com"
)

DevengNetworkingModule.initDevengNetworkingModule(
    restBaseUrl = "https://dev-api.example.com",
    config = devConfig
)

// Production Configuration
val prodConfig = DevengNetworkingConfig(
    loggingEnabled = false, // Disable logging in production
    requestTimeoutMillis = 30_000L,
    customHeaders = mapOf(
        "X-API-Version" to "v1",
        "X-Client-Version" to "2.1.0"
    ),
    socketBaseUrl = "wss://ws.example.com"
)

DevengNetworkingModule.initDevengNetworkingModule(
    restBaseUrl = "https://api.example.com",
    config = prodConfig
)

// File Upload Configuration
val uploadConfig = DevengNetworkingConfig(
    requestTimeoutMillis = 600_000L, // 10 minutes for large files
    connectTimeoutMillis = 30_000L,
    socketTimeoutMillis = 300_000L
)

DevengNetworkingModule.initDevengNetworkingModule(
    restBaseUrl = "https://upload.example.com",
    config = uploadConfig
)
```

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

**More Projects**: Check out other projects by [Deveng Group](https://github.com/Deveng-Group) | **Website**: [deveng.global](https://deveng.global)

