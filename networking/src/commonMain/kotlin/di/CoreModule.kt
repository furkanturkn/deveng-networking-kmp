package networking.di

import networking.exception_handling.ExceptionHandler
import networking.exception_handling.ExceptionHandlerImpl

object CoreModule {
    val exceptionHandler: ExceptionHandler by lazy {
        ExceptionHandlerImpl
    }
}