# Deveng Networking Module

A Kotlin Multiplatform networking library that provides a simple and efficient way to handle REST API calls and WebSocket connections.

## Features

- 🌐 REST API client with full HTTP method support
- 🔄 WebSocket client with connection management
- 🎯 Multiplatform support (Android, iOS, Desktop)
- 🔒 Built-in token-based authentication
- 🌍 Localization support
- ⚡ Efficient WebSocket connection pooling
- 🎭 Comprehensive error handling

## Installation

Add the dependency to your project:

### Gradle (build.gradle.kts)
```kotlin
dependencies {
    implementation("global.deveng:networking-kmp:1.2.0")
}
```

### Version Catalog (libs.versions.toml)
```toml
[versions]
devengNetworkingKmp = "1.2.0"

[libraries]
deveng-networking-kmp = { module = "global.deveng:networking-kmp", version.ref = "devengNetworkingKmp" }
```

Then in your build.gradle.kts:
```kotlin
dependencies {
    implementation(libs.deveng.networking.kmp)
}
```

## Usage

### Initialization

Initialize the module with your API endpoints:

```kotlin
DevengNetworkingModule.apply {
    restBaseUrl = "https://api.example.com"
    socketBaseUrl = "wss://ws.example.com"
    setBearerToken("your-auth-token") // Optional: Set if using token-based auth
    setLocale(Locale.EN) // Optional: Set preferred locale
}
```

### REST API Calls

Make type-safe API calls with built-in error handling:

```kotlin
// GET Request
val response: UserResponse = DevengNetworkingModule.sendRequest(
    endpoint = "/users/{userId}",
    requestMethod = DevengHttpMethod.GET,
    pathParameters = mapOf("userId" to "123")
)

// POST Request with Body
val createResponse: CreateUserResponse = DevengNetworkingModule.sendRequest(
    endpoint = "/users",
    requestBody = UserCreateRequest(name = "John", age = 25),
    requestMethod = DevengHttpMethod.POST
)

// Request with Query Parameters
val searchResponse: SearchResponse = DevengNetworkingModule.sendRequest(
    endpoint = "/search",
    requestMethod = DevengHttpMethod.GET,
    queryParameters = mapOf(
        "query" to "example",
        "page" to "1"
    )
)
```

### WebSocket Connections

The module provides a powerful WebSocket client with connection pooling:

```kotlin
// Connect to a WebSocket endpoint
val connection = DevengNetworkingModule.connectToWebSocket(
    endpoint = "/realtime",
    onConnected = {
        // Connection established
        sendMessage("Hello Server!")
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
// Configure maximum simultaneous connections
WebSocketConnection.setMaxConnections(3) // Default is 5

// Get current connection stats
val activeConnections = WebSocketConnection.getActiveConnections()
val connectionCount = WebSocketConnection.getConnectionCount()

// Close specific connection
WebSocketConnection.closeConnection("/realtime")

// Close all connections
WebSocketConnection.closeAll()
```

### Error Handling

The module provides comprehensive error handling:

```kotlin
try {
    val response = DevengNetworkingModule.sendRequest<RequestType, ResponseType>(
        endpoint = "/endpoint",
        requestMethod = DevengHttpMethod.GET
    )
} catch (e: DevengException) {
    // Handle specific error with localized message
    println(e.message)
}
```

## Advanced Configuration

### Custom Error Handling

Implement the `ExceptionHandler` interface to provide custom error handling:

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

## Platform Support

- ✅ Android
- ✅ iOS
- ✅ Desktop (JVM)
- ✅ Web (Experimental)

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.