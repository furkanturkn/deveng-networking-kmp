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
import kotlinx.coroutines.launch
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

                    exampleWebSocketUsage()

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
    DevengNetworkingModule.connectToWebSocket("/raw") {
        send(Frame.Text("Hello WebSocket!")) // Send a message

        for (frame in incoming) {
            when (frame) {
                is Frame.Text -> {
                    println("Received: ${frame.readText()}")
                }
                is Frame.Close -> {
                    println("WebSocket closed: ${frame.readReason()}")
                }
                else -> Unit
            }
        }
    }
}