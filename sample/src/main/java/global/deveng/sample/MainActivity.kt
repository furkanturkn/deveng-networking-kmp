package global.deveng.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import error_handling.DevengException
import global.deveng.sample.ui.theme.DevengnetworkingkmpTheme
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import networking.DevengNetworkingConfig
import networking.DevengNetworkingModule
import networking.localization.Locale
import networking.util.DevengHttpMethod


class MainActivity : ComponentActivity() {
    data class SocketState(
        val socketListJson: String = ""
    )
    private val _socketList = mutableStateOf(SocketState())
    val socketList: State<SocketState> = _socketList

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val viewmodel = MainViewModel()

        setContent {
            DevengnetworkingkmpTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column {
                        Greeting(
                            name = viewmodel.error.message ?: "Yalaaancıııı",
                            modifier = Modifier.padding(innerPadding)
                        )

                        Greeting(
                            name = socketList.value.socketListJson,
                            modifier = Modifier.padding(innerPadding)
                        )

                        Button(
                            onClick = {
                                test()
                            },
                            modifier = Modifier.padding(innerPadding)
                        ) { }

                    }

                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    DevengnetworkingkmpTheme {
        Greeting("Android")
    }
}


suspend fun exampleWebSocketUsage(
    endPoint: String,
    onReceivedResponse: (String) -> Unit
) {

    println("Attempting to connect to WebSocket endpoint: $endPoint")
    DevengNetworkingModule.connectToWebSocket(
        endpoint = endPoint,
        onConnected = {
            println("Successfully connected to $endPoint")
            sendMessage("{\"protocol\":\"json\",\"version\":1}\u001E")
        },
        onMessageReceived = { message ->
            val cleanResponse = message.trimEnd('\u001E')
            onReceivedResponse(cleanResponse)
        },
        onError = { throwable ->
            println("WebSocket error on $endPoint: ${throwable.message}")
            throw throwable
        },
        onClose = {
            println("WebSocket connection closed for $endPoint")
            return@connectToWebSocket
        }
    )

}

@Serializable
data class VerifySmsOtpRequest(
    val otp: String,
    val phoneNumber: String
)

@Serializable
data class AuthenticateOtpResponse(
    val token: String
)

fun test() {
    GlobalScope.launch {
        try {
            DevengNetworkingModule.initDevengNetworkingModule(
                restBaseUrl = "https://test-api.huhuv.org/",
                config = DevengNetworkingConfig(
                    socketBaseUrl = "https://test-api.huhuv.org/",
                    loggingEnabled = true,
                    locale = Locale.TR,
                    token = "token",
                    customHeaders = mapOf("ApplicationType" to "Admin")
                )
            )

            val requestBody = VerifySmsOtpRequest(
                otp = "otp",
                phoneNumber = "phoneNumber"
            )

            val result =
                DevengNetworkingModule.sendRequest<VerifySmsOtpRequest, AuthenticateOtpResponse>(
                    endpoint = "identity/api/AuthSMS/VerifyLogin",
                    requestMethod = DevengHttpMethod.POST,
                    requestBody = requestBody
                )


        } catch (e: DevengException) {
            println("*******")
            println(e.message)
        }
    }


}