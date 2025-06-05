package networking.di

import kotlinx.serialization.json.Json
import networking.exception_handling.ExceptionHandler
import networking.exception_handling.ExceptionHandlerImpl

internal object CoreModule {
    val exceptionHandler: ExceptionHandler by lazy {
        ExceptionHandlerImpl
    }

    val sharedJson: Json by lazy {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
            prettyPrint = false
        }
    }
}