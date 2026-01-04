package global.deveng.networking

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import global.deveng.networking.theme.AppTheme
import kotlinx.coroutines.launch
import error_handling.DevengException
import networking.DevengNetworkingConfig
import networking.DevengNetworkingModule
import networking.localization.Locale
import networking.util.DevengHttpMethod
import kotlinx.serialization.Serializable

@Composable
internal fun App() {
    var responseText by remember { mutableStateOf("No request made yet") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val networkingModule = remember { DevengNetworkingModule() }

    AppTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
                Text(
                    text = "Deveng Networking KMP Sample",
                    style = MaterialTheme.typography.headlineMedium
                )

                Button(
                    onClick = {
                        scope.launch {
                            isLoading = true
                            errorMessage = null
                            try {
                                // Initialize the networking module
                                networkingModule.initDevengNetworkingModule(
                                    restBaseUrl = "https://jsonplaceholder.typicode.com",
                                    config = DevengNetworkingConfig(
                                        socketBaseUrl = "",
                                        loggingEnabled = true,
                                        locale = Locale.EN,
                                        token = "",
                                        customHeaders = emptyMap()
                                    )
                                )

                                // Make a test request
                                val result = networkingModule.sendRequest<Unit, PostResponse>(
                                    endpoint = "/posts/1",
                                    requestMethod = DevengHttpMethod.GET,
                                    requestBody = null
                                )

                                responseText = "Success!\n\nTitle: ${result.title}\nBody: ${result.body}"
                            } catch (e: DevengException) {
                                errorMessage = "Error: ${e.message ?: "Unknown error"}"
                                responseText = "Request failed"
                            } catch (e: Exception) {
                                errorMessage = "Unexpected error: ${e.message}"
                                responseText = "Request failed"
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(if (isLoading) "Loading..." else "Make Test Request")
                }

                if (errorMessage != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = responseText,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
}

@Serializable
data class PostResponse(
    val userId: Int,
    val id: Int,
    val title: String,
    val body: String
)

