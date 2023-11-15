package com.devd.calenderbydw.data.remote

sealed class CallResult <out T> {

    data class Success<out T>(val data : T) : CallResult<T>()
    data class GenericError(val code:String,val message:String,val errorApi :String) : CallResult<Nothing>()
    object NetworkError: CallResult<Nothing>()
}
