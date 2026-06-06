# Deveng Networking KMP

A powerful, easy-to-use Kotlin Multiplatform networking library that simplifies REST API communication and WebSocket management with minimal setup.

[![](https://img.shields.io/badge/Kotlin%20Multiplatform-Latest-blue.svg)](https://kotlinlang.org/docs/multiplatform.html)
[![](https://img.shields.io/badge/License-Apache%202.0-green.svg)](LICENSE)

## 🚀 Quick Start

### Installation

Add to your `build.gradle.kts`:

```kotlin
dependencies {
    implementation("global.deveng:networking-kmp:latest")
}
```

### Basic Setup

```kotlin
// 1. Configure the module
val config = DevengNetworkingConfig(
    loggingEnabled = true,                         // Optional - defaults to true
    requestTimeoutMillis = 30_000L,               // Optional - defaults to 60 seconds
    connectTimeoutMillis = 10_000L,               // Optional - defaults to 10 seconds
    socketTimeoutMillis = 30_000L,                // Optional - defaults to 60 seconds
    token = "your-auth-token",                    // Optional - for Bearer auth (omit for cookie-only)
    locale = Locale.EN,                           // Optional - defaults to EN
    customHeaders = mapOf(                        // Optional - for global headers
        "X-API-Version" to "1.0",
        "X-Client-Platform" to "Android"
    ),
    socketBaseUrl = "wss://ws.example.com"        // Optional - only needed for WebSocket
)

// 2. Initialize
DevengNetworkingModule.initDevengNetworkingModule(
    restBaseUrl = "https://api.example.com",      // Required - your REST API base URL
    config = config
)

// 3. Make your first API call
suspend fun getUser(userId: String): User? {
    return DevengNetworkingModule.sendRequest<Unit, User?>(
        endpoint = "/users/{userId}",
        requestMethod = DevengHttpMethod.GET,
        pathParameters = mapOf("userId" to userId)
    )
}
```

**⚠️ Note**: The inline version requires **Java 21+**. For older Java versions, use explicit serializers instead:

```kotlin
// For Java < 21, use this approach:
suspend fun getUser(userId: String): User {
    return DevengNetworkingModule.sendRequest(
        endpoint = "/users/{userId}",
        requestMethod = DevengHttpMethod.GET,
        pathParameters = mapOf("userId" to userId),
        requestSerializer = null,              // No request body
        responseSerializer = User.serializer() // Explicit response serializer
    )
}
```

## ✨ Features

| Feature | Description |
|---------|-------------|
| 🌐 **Full REST API Support** | GET, POST, PUT, DELETE with automatic serialization |
| 📁 **File Upload Support** | Simple multipart file uploads with MIME detection |
| 🔄 **WebSocket Management** | Connection pooling, lifecycle events, automatic reconnection |
| 🎯 **Multiplatform** | Android, iOS, Desktop (JVM), WebAssembly |
| 🔒 **Authentication** | Bearer tokens, cookie sessions, CSRF, and refresh-on-401 |
| 🌍 **Localization** | Localized error messages and headers |
| 🎭 **Error Handling** | Centralized, customizable exception handling |
| 🛠️ **Dynamic Parameters** | Path and query parameter injection |
| 📋 **Custom Headers** | Global and per-request custom headers |
| 🔧 **Configurable Logging** | Enable/disable logging per environment |
| ⏱️ **Timeout Control** | Request, connection, and socket timeouts |

## 📱 Platform Support

- ✅ Android
- ✅ iOS  
- ✅ Desktop (JVM)
- ✅ WebAssembly (WASM)

## 🔧 Environment-Specific Configurations

```kotlin
// Development
val devConfig = DevengNetworkingConfig(
    loggingEnabled = true,
    requestTimeoutMillis = 120_000L,
    token = "dev-token"
)

// Production
val prodConfig = DevengNetworkingConfig(
    loggingEnabled = false,
    requestTimeoutMillis = 30_000L,
    customHeaders = mapOf("X-Client-Version" to "2.1.0")
)
```

## 🌐 REST API Usage

### HTTP Methods

```kotlin
// GET Request
suspend fun getUser(id: String): User? {
    return DevengNetworkingModule.sendRequest<Unit, User?>(
        endpoint = "/users/{id}",
        requestMethod = DevengHttpMethod.GET,
        pathParameters = mapOf("id" to id)
    )
}

// POST Request
suspend fun createUser(user: CreateUserRequest): User? {
    return DevengNetworkingModule.sendRequest<CreateUserRequest, User?>(
        endpoint = "/users",
        requestBody = user,
        requestMethod = DevengHttpMethod.POST
    )
}

// PUT Request
suspend fun updateUser(id: String, user: UpdateUserRequest) {
    DevengNetworkingModule.sendRequest<UpdateUserRequest, Unit>(
        endpoint = "/users/{id}",
        requestBody = user,
        requestMethod = DevengHttpMethod.PUT,
        pathParameters = mapOf("id" to id)
    )
}

// DELETE Request
suspend fun deleteUser(id: String) {
    DevengNetworkingModule.sendRequest<Unit, Unit>(
        endpoint = "/users/{id}",
        requestMethod = DevengHttpMethod.DELETE,
        pathParameters = mapOf("id" to id)
    )
}
```

### Query Parameters

```kotlin
suspend fun searchUsers(query: String, limit: Int): List<User>? {
    return DevengNetworkingModule.sendRequest<Unit, List<User>?>(
        endpoint = "/users/search",
        requestMethod = DevengHttpMethod.GET,
        queryParameters = mapOf(
            "query" to query,
            "limit" to limit.toString()
        )
    )
}
```

### Raw HTTP Response

Use this when you need access to HTTP headers, status codes, or other response metadata:

```kotlin
suspend fun getRawResponse(): HttpResponse {
    return DevengNetworkingModule.sendRequestForHttpResponse<Unit>(
        endpoint = "/status",
        requestMethod = DevengHttpMethod.GET
    )
}

// Example usage - accessing response metadata
suspend fun checkApiLimits() {
    val response = DevengNetworkingModule.sendRequestForHttpResponse<Unit>(
        endpoint = "/api/limits",
        requestMethod = DevengHttpMethod.GET
    )
    
    // Access headers for rate limiting info
    val remainingRequests = response.headers["X-RateLimit-Remaining"]
    val resetTime = response.headers["X-RateLimit-Reset"]
    
    // Check exact status code
    when (response.status.value) {
        200 -> println("API healthy")
        429 -> println("Rate limited - retry after $resetTime")
        else -> println("Status: ${response.status}")
    }
}
```

**Common use cases:**
- **Rate limiting**: Access `X-RateLimit-*` headers
- **Pagination**: Get `Link` or pagination headers  
- **Debugging**: Inspect full response details
- **Custom status handling**: Handle specific HTTP status codes
- **Cookies**: Access response cookies
- **Content metadata**: Check `Content-Type`, `Content-Length`, etc.

## 📁 File Upload Usage

Upload files with multipart form data using the same simplicity as regular API calls with `DevengHttpMethod.MULTIPART`:

### Simple Multipart File Upload

```kotlin
suspend fun uploadProfilePicture(userId: String, imageData: ByteArray) {
    DevengNetworkingModule.sendRequest<Unit, Unit>(
        endpoint = "/users/$userId/avatar",
        requestMethod = DevengHttpMethod.MULTIPART,
        fileName = "profile.jpg",
        fileContent = imageData
    )
}
```

### Multipart File Upload with Additional Form Fields

```kotlin
suspend fun uploadDocument(
    fileName: String,
    fileData: ByteArray,
    documentType: String,
    isPublic: Boolean
) {
    DevengNetworkingModule.sendRequest<Unit, UploadResponse>(
        endpoint = "/documents/upload",
        requestMethod = DevengHttpMethod.MULTIPART,
        fileName = fileName,
        fileContent = fileData,
        additionalFormFields = mapOf(
            "type" to documentType,
            "isPublic" to isPublic.toString(),
            "uploadedAt" to System.currentTimeMillis().toString()
        )
    )
}
```

### Custom File Field Name

```kotlin
suspend fun uploadAttachment(fileName: String, fileData: ByteArray) {
    DevengNetworkingModule.sendRequest<Unit, Unit>(
        endpoint = "/attachments",
        requestMethod = DevengHttpMethod.MULTIPART,
        fileName = fileName,
        fileContent = fileData,
        fileFieldName = "attachment" // Custom field name instead of default "File"
    )
}
```

### Multipart File Upload with Response Parsing

```kotlin
data class UploadResponse(val fileId: String, val downloadUrl: String)

suspend fun uploadAndGetUrl(fileName: String, fileData: ByteArray): UploadResponse {
    return DevengNetworkingModule.sendRequest<Unit, UploadResponse>(
        endpoint = "/files/upload",
        requestMethod = DevengHttpMethod.MULTIPART,
        fileName = fileName,
        fileContent = fileData,
        additionalFormFields = mapOf("source" to "mobile")
    )
}
```

### For Java < 21 (Explicit Serializers)

```kotlin
suspend fun uploadFile(fileName: String, fileData: ByteArray): UploadResponse {
    return DevengNetworkingModule.sendRequest(
        endpoint = "/files/upload",
        requestMethod = DevengHttpMethod.MULTIPART,
        requestSerializer = null,
        responseSerializer = UploadResponse.serializer(),
        fileName = fileName,
        fileContent = fileData
    )
}
```

**Multipart File Upload Features:**
- **Automatic MIME type detection** based on file extension
- **Custom file field names** for different API requirements
- **Additional multipart form fields** for metadata
- **Type-safe responses** with automatic JSON parsing
- **Same error handling** as regular API calls
- **Works on all platforms** - Android, iOS, Desktop, Web

## 🔌 WebSocket Usage

### Basic WebSocket Connection

```kotlin
val connection = DevengNetworkingModule.connectToWebSocket(
    endpoint = "/chat",
    onConnected = {
        sendMessage("Hello Server!")
    },
    onMessageReceived = { message ->
        println("Received: $message")
    },
    onError = { error ->
        println("WebSocket Error: $error")
    },
    onClose = {
        println("Connection closed")
    }
)

// Send messages
connection.sendMessage("Hello World!")
```

### Connection State Monitoring

```kotlin
scope.launch {
    connection.connectionState.collect { state ->
        when (state) {
            is ConnectionState.Connected -> println("✅ Connected")
            is ConnectionState.Connecting -> println("🔄 Connecting...")
            is ConnectionState.Disconnected -> println("❌ Disconnected")
            is ConnectionState.Error -> println("⚠️ Error: ${state.error}")
        }
    }
}
```

### Connection Management

```kotlin
// Configure max connections (default: 5)
WebSocketConnection.setMaxConnections(3)

// Get connection stats
val activeConnections = WebSocketConnection.getActiveConnections()
val connectionCount = WebSocketConnection.getConnectionCount()

// Close specific connection
DevengNetworkingModule.closeWebSocketConnection("/chat")

// Close all connections
DevengNetworkingModule.closeAllWebSocketConnections()
```

## ⚠️ Error Handling

### Standard Error Handling

```kotlin
try {
    val user = DevengNetworkingModule.sendRequest<Unit, User?>(
        endpoint = "/users/123",
        requestMethod = DevengHttpMethod.GET
    )
} catch (e: DevengException) {
    println("Error: ${e.message}") // Localized error message
}
```

### Custom Error Handling

```kotlin
class CustomExceptionHandler : ExceptionHandler {
    override var locale: Locale = Locale.EN

    override fun handleHttpException(
        errorMessage: String?,
        status: HttpStatusCode
    ): DevengUiError {
        // Custom HTTP error logic
        return when (status.value) {
            404 -> DevengUiError("Resource not found")
            500 -> DevengUiError("Server error occurred")
            else -> DevengUiError("Request failed: $errorMessage")
        }
    }

    override fun handleNetworkException(cause: Throwable): DevengUiError {
        // Custom network error logic
        return DevengUiError("Network connection failed")
    }
}
```

## 🔧 Advanced Features

### Dynamic Token Management

```kotlin
// Update authentication token at runtime
DevengNetworkingModule.setToken("new-auth-token")
```

When the token is blank (`""`), DNM omits the `Authorization` header entirely rather than sending an empty `Authorization: Bearer ` value — useful for cookie-only auth flows.

### Cookie-Based Sessions

Install Ktor's `HttpCookies` plugin with a custom `CookiesStorage` to persist cookies across requests (and across process restarts, if your storage writes to disk):

```kotlin
val config = DevengNetworkingConfig(
    httpClientConfig = {
        install(HttpCookies) {
            storage = MyPersistentCookiesStorage()
        }
    },
    wasmJsIncludeCredentials = true               // Required on wasmJs for cross-origin cookies
)
```

`httpClientConfig` is a generic Ktor `HttpClient` extension point — install any plugin you need, not just `HttpCookies`. `wasmJsIncludeCredentials` is engine-level and only relevant on the wasmJs target.

### CSRF Protection

Attach a CSRF token header on every POST, PUT, PATCH, DELETE. The provider is called once per mutating request; a `null` return skips the header for that request:

```kotlin
val config = DevengNetworkingConfig(
    csrfTokenProvider = DevengCsrfTokenProvider { myCsrfStore.getToken() },
    csrfHeaderName = "X-CSRF-TOKEN"               // Optional - defaults to "X-CSRF-TOKEN"
)
```

### Handling 401 Responses

Two opt-in hooks fire on HTTP 401. `sessionRefresher` runs first and can recover the session transparently by refreshing and retrying the original request; `onUnauthorized` runs only when recovery fails (or isn't configured) and is intended for global UI reactions:

```kotlin
val config = DevengNetworkingConfig(
    sessionRefresher = DevengSessionRefresher {
        try {
            authService.refreshSession()
            true                                  // Retry the failed request
        } catch (e: Exception) {
            false                                 // Surface UnauthorizedError
        }
    },
    onUnauthorized = { eventBus.emit(SessionExpired) }
)
```

Concurrent 401s are deduplicated by a per-client mutex + generation counter, so only one refresh runs even under heavy parallel load. The refresher can issue HTTP calls through the same DNM client without infinite recursion — an internal coroutine-context marker suppresses re-entrance.

### Version Catalog Setup

Add to `libs.versions.toml`:

```toml
[versions]
devengNetworkingKmp = "latest"

[libraries]
deveng-networking-kmp = { 
    module = "global.deveng:networking-kmp", 
    version.ref = "devengNetworkingKmp" 
}
```

Then in `build.gradle.kts`:

```kotlin
dependencies {
    implementation(libs.deveng.networking.kmp)
}
```



## 🏗️ Architecture

The library is built on top of **Ktor** and provides:

- **REST API Layer**: Handles HTTP requests with automatic serialization/deserialization
- **WebSocket Layer**: Manages real-time connections with pooling and lifecycle events
- **Error Handling**: Centralized exception handling with localization support
- **Configuration**: Type-safe configuration with dependency injection
- **Multiplatform**: Single codebase for all supported platforms

## 📄 License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## 🤝 Contributing

We welcome contributions! Please:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

For major changes, please open an issue first to discuss your proposal.

## 🌟 About Deveng Group

Created by [Deveng Group](https://github.com/Deveng-Group) - Building powerful, developer-friendly tools for the Kotlin ecosystem.

- 🌐 Website: [deveng.global](https://deveng.global)
- 📚 More Projects: [GitHub](https://github.com/Deveng-Group)

---

**Made with ❤️ by the Deveng Group team**

