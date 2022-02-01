package com.dwstyle.calenderbydw.utils

import android.content.Context
import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.Asset
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.*
import java.lang.Exception

class WriteDBFile(private val context : Context,private val receiveAsset : Asset) {

    fun createDBFromSendData(){
        val dbAsset : ByteArray = bytesArrayFromAsset(receiveAsset)
        val newFile = File(context.getDatabasePath("task.db").path)

        CoroutineScope(Dispatchers.IO).launch {
            writeBytesToFile(newFile,dbAsset)
        }

    }
    fun bytesArrayFromAsset(asset :Asset)  : ByteArray{
        Log.d("도원","assetInputStream.readBytes()");
        val assetInputStream : InputStream = Tasks.await(Wearable.getDataClient(context).getFdForAsset(asset)).inputStream
        return assetInputStream.readBytes()

    }
    fun writeBytesToFile(theFile : File,bytes : ByteArray) {
        var bos: BufferedOutputStream? = null
        try {
            val fos: FileOutputStream = FileOutputStream(theFile, false)
            bos = BufferedOutputStream(fos)
            bos.write(bytes)
        } catch (e: IOException) {
            Log.d("도원", " 에러에러   ${e.localizedMessage}")
        } finally {
            try {
                bos?.flush()
                bos?.close()
            } catch (e1: Exception) {
                Log.d("도원", " 에러에러2   ${e1.localizedMessage}")
            }

        }
    }

}