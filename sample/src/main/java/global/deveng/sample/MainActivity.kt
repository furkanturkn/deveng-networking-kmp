package global.deveng.sample

import android.os.Bundle
import android.provider.Settings.Global
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import error_handling.DevengException
import global.deveng.sample.data.datasource.remote.AuthenticationService
import global.deveng.sample.data.repository.AuthenticationRepositoryImpl
import global.deveng.sample.domain.model.Authentication
import global.deveng.sample.ui.theme.DevengnetworkingkmpTheme
import io.ktor.websocket.Frame
import io.ktor.websocket.readReason
import io.ktor.websocket.readText
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import networking.DevengNetworkingModule


class MainActivity : ComponentActivity() {

    var a: Authentication? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val authenticationRepositoryImpl = AuthenticationRepositoryImpl(
            authenticationService = AuthenticationService()
        )

        fun test() {

            try {
                GlobalScope.launch {

                   // exampleWebSocketUsage()


                    a = authenticationRepositoryImpl.authenticate(
                        "amin",
                        "1"
                    )
                    println(a?.token)


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


suspend fun exampleWebSocketUsage() {
    val connection = DevengNetworkingModule.connectToWebSocket(
        endpoint = "/Doviz",
        onConnected = {
            sendMessage("{\"protocol\":\"json\",\"version\":1}\u001E")
        },
        onMessageReceived = { message ->
            println("Message received: $message")
        },
        onError = { error ->
            println("An error occurred: ${error.message}")
        },
        onClose = {
            println("WebSocket connection closed.")
        }
    )

    runBlocking {
        delay(2000)
        try {
            connection.sendMessage("This is another message sent after connection.")
        } catch (e: Exception) {
            println("Error sending message: ${e.message}")
        }
    }
}