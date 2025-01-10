package global.deveng.sample

import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import error_handling.DevengException
import error_handling.DevengUiError
import global.deveng.sample.MainActivity.SocketState

class MainViewModel : ViewModel() {

    private var _error by mutableStateOf<DevengException?>(null)
    val error: DevengException
        get() = _error ?: DevengException(DevengUiError.UnknownError("Unknown error occurred."))

    private val _socketList = mutableStateOf(SocketState())
    val socketList: State<SocketState> = _socketList


}
