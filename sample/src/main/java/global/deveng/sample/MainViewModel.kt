package global.deveng.sample

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import error_handling.DevengException
import error_handling.DevengUiError

class MainViewModel : ViewModel() {

    private var _error by mutableStateOf<DevengException?>(null)
    val error: DevengException
        get() = _error ?: DevengException(DevengUiError.UnknownError("Unknown error occurred."))


}
