import androidx.compose.ui.window.ComposeUIViewController
import global.deveng.networking.App
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController = ComposeUIViewController { App() }

