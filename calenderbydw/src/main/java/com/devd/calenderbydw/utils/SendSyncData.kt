package com.devd.calenderbydw.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import com.devd.calenderbydw.database.TaskDatabaseHelper
import com.google.android.gms.wearable.Asset
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.PutDataMapRequest
import java.io.File
import java.lang.Exception
import java.nio.file.Files
import java.time.Instant
import java.util.concurrent.CancellationException

class SendSyncData {

    companion object{

        //데이터 전송하기 전에 DB를 byteArray형태로 변경
        fun changeDBToBytes(context:Context,dataClient :DataClient){
            //DB 경로를 구한 한다.
            val dbPath = TaskDatabaseHelper(context,"wearTask.db",null,3).readableDatabase.path
            val dbFile = File(dbPath)
            val dbUri = Uri.fromFile(dbFile)
//        val realAsset = Asset.createFromUri(dbUri)
            val bytesFromDB = Files.readAllBytes(dbFile.toPath())
            val realAsset = Asset.createFromBytes(bytesFromDB)
            sendDBData(realAsset,dbPath,dataClient)
        }


        //Asset 으로 만든 데이터 보내기
        private fun sendDBData(sendData : Asset, dBPtah : String,dataClient :DataClient) {
//        val dataMap : PutDataMapRequest = PutDataMapRequest.create("/taskdata")
//        dataMap.dataMap.putAsset("taskDB",sendData)
//        dataMap.dataMap.putString("taskDBPath",dBPtah)
//        Log.d("도원","dBPtah :  ${dBPtah}")
//        val request : PutDataRequest= dataMap.asPutDataRequest()
//        request.setUrgent()
//        val putTask : Task<DataItem> =Wearable.getDataClient(this).putDataItem(request)
            try {
                val putDataReq = PutDataMapRequest.create("/taskdata").apply {
                    this.dataMap.putAsset("taskDB", sendData)
                    this.dataMap.putString("taskDBPath", dBPtah)
                    this.dataMap.putLong("KEY", Instant.now().epochSecond)

                }
                    .asPutDataRequest()
                    .setUrgent()

                val result = dataClient.putDataItem(putDataReq)

                Log.d("도원", "putDataReq.uri: $putDataReq.uri")
                Log.d("도원", "DataItem saved: $result")
            } catch (cancellationException: CancellationException) {
                Log.d("도원", "Saving DataItem failed: ${cancellationException.localizedMessage}")
                throw cancellationException
            } catch (exception: Exception) {
                Log.d("도원", "Saving DataItem failed: $exception")
            }
        }
    }
}