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
import global.deveng.sample.data.datasource.remote.AuthenticationService
import global.deveng.sample.data.repository.AuthenticationRepositoryImpl
import global.deveng.sample.domain.model.Authentication
import global.deveng.sample.ui.theme.DevengnetworkingkmpTheme
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import networking.DevengNetworkingModule
import websocket.WebSocketConnection
import kotlin.coroutines.cancellation.CancellationException


class MainActivity : ComponentActivity() {

    var a: Authentication? = null

    data class SocketState(
        val socketListJson: String = ""
    )

    private val _socketList = mutableStateOf(SocketState())
    val socketList: State<SocketState> = _socketList

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val authenticationRepositoryImpl = AuthenticationRepositoryImpl(
            authenticationService = AuthenticationService()
        )

        fun test() {

            try {
                GlobalScope.launch {

                    exampleWebSocketUsage(
                        endPoint = "/Doviz",
                        onReceivedResponse = {
                            _socketList.value = SocketState(socketListJson = it)

                            println("WebSocketResponse:${it}")
                        }
                    )

                    println("Active connections: ${WebSocketConnection.getActiveConnections()}")
                    println("Connection count: ${WebSocketConnection.getConnectionCount()}")

                    delay(2000)

                    DevengNetworkingModule.closeWebSocketConnection(
                        "/Doviz"
                    )

                    exampleWebSocketUsage(
                        endPoint = "/Maden",
                        onReceivedResponse = {
                            _socketList.value = SocketState(socketListJson = it)

                            println("WebSocketResponse:${it}")
                        }
                    )

                    println("Active connections: ${WebSocketConnection.getActiveConnections()}")
                    println("Connection count: ${WebSocketConnection.getConnectionCount()}")


                    /*
                    a = authenticationRepositoryImpl.authenticate(
                        "amin",
                        "1"
                    )
                    println(a?.token)

                     */


                }
            } catch (e: DevengException) {
                println("*******")
                println(e.message)
            }


        }


        val viewmodel = MainViewModel()


        enableEdgeToEdge()
        setContent {
            DevengnetworkingkmpTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column {
                        Greeting(
                            name = a?.token ?: "Requeest",
                            modifier = Modifier.padding(innerPadding)
                        )

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