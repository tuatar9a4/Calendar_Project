package com.devd.calenderbydw.utils

import com.devd.calenderbydw.data.remote.CallResult
import com.devd.calenderbydw.utils.ConstVariable.ERROR_UK1001
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException

abstract class SafeNetCall {

    suspend fun <T> safeApiCall(
        dispatcher: CoroutineDispatcher,
        apiCall: suspend () -> T
    ): CallResult<T> {
        return withContext(dispatcher) {
            try {
                CallResult.Success(apiCall.invoke())
            } catch (throwable: Throwable) {
                when (throwable) {
                    is IOException -> {
                        Timber.e("throwable: $throwable")
                        CallResult.NetworkError
                    }
                    is HttpException -> {
                        val httpCode = throwable.code().toString()
                        val errorApi = throwable.response()?.raw()?.request?.url.toString()
                        Timber.d("http error: $httpCode , errorApi: ${errorApi}")
                        try {
                            CallResult.GenericError(httpCode, throwable.message(), errorApi)
                        }catch (e : JsonSyntaxException){
                            //통신 실팩 결과값이 ErrorBody::class 형태로 안내려와 sync 에러가 발생할 경우 예외처리
                            CallResult.GenericError(ERROR_UK1001, "Unknown Error", errorApi)
                        }
                    }
                    else -> {
                        Timber.e("throwable: $throwable")
                        CallResult.GenericError(ERROR_UK1001, "Unknown Error", "null")
                    }
                }
            }
        }
    }
}