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
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import error_handling.DevengException
import global.deveng.sample.data.datasource.remote.AuthenticationService
import global.deveng.sample.data.repository.AuthenticationRepositoryImpl
import global.deveng.sample.domain.model.Authentication
import global.deveng.sample.ui.theme.DevengnetworkingkmpTheme
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {

    var a: Authentication? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val authenticationRepositoryImpl = AuthenticationRepositoryImpl(
            authenticationService = AuthenticationService()
        )

        fun test() {
            try{
                GlobalScope.launch {
                    a = authenticationRepositoryImpl.authenticate(
                        "admin",
                        "1"
                    )
                    println(a?.token)
                }
            } catch (e: DevengException) {
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
