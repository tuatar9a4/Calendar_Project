package com.dwstyle.calenderbydw

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.wear.tiles.TileService
import com.dwstyle.calenderbydw.databinding.ActivityMainBinding
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.*
import java.io.*
import java.lang.Exception
import java.util.concurrent.*

class MainActivity : ComponentActivity() ,DataClient.OnDataChangedListener,MessageClient.OnMessageReceivedListener,
                                            CapabilityClient.OnCapabilityChangedListener{

    private lateinit var binding: ActivityMainBinding

//    private lateinit var tileUiClient :TileUiClient

    private lateinit var mGeneratorExecutor :ScheduledExecutorService
    private lateinit var mDataItemGeneratorFuture: ScheduledFuture<*>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        타일 새로 고침
//        TileService.getUpdater(applicationContext).requestUpdate(CalendarTile::class.java)
//        val rootLayout = findViewById<FrameLayout>(R.id.tile_container)
//        tileUiClient = TileUiClient(
//            context = this,
//            component = ComponentName(this,CalendarTile::class.java),
//            parentView = rootLayout
//        )
//        tileUiClient.connect()

        mGeneratorExecutor =ScheduledThreadPoolExecutor(1)


    }

    override fun onResume() {
        super.onResume()
//        mDataItemGeneratorFuture =
//                    mGeneratorExecutor.scheduleWithFixedDelay(
//                        DataItemGenerator(applicationContext),1,5,TimeUnit.SECONDS
//                    )


        Wearable.getDataClient(this).addListener(this)
        Wearable.getMessageClient(this).addListener(this)
        Wearable.getCapabilityClient(this)
            .addListener(this, Uri.parse("wear://"), CapabilityClient.FILTER_REACHABLE)
    }

    override fun onPause() {
        super.onPause()
        Wearable.getDataClient(this).removeListener(this)
        Wearable.getMessageClient(this).removeListener(this)
        Wearable.getCapabilityClient(this).removeListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
//        tileUiClient.close()
    }

    override fun onDataChanged(p0: DataEventBuffer) {
       Log.d("도원","onDataChanged main")
       Log.d("도원","p0 ${p0.count}")
        for (event in p0){
           Log.d("도원","event ${event.type}")
            if (event.type == DataEvent.TYPE_CHANGED){
                 Log.d("도원","event.dataItem.uri ${event.dataItem.uri}")
                 Log.d("도원","event.dataItem.uri.path ${event.dataItem.uri.path}")
                val path = event.dataItem.uri.path
                if ("/taskdata".equals(path)){
                    val dataMapItem =DataMapItem.fromDataItem(event.dataItem)
                    val dbString :String? =dataMapItem.dataMap.getString("taskDBPath")
                    val dbAsset : ByteArray =dataMapItem.dataMap.getAsset("taskDB").let {
                        asset ->bytesArrayFromAsset(asset!!)
                    }
                    Log.d("도원", applicationContext.getDatabasePath("test").path)
                    Log.d("도원","${dbAsset}")
//                    val newFile =File(applicationContext.getDatabasePath("test").path)
//                    writeBytesToFile(newFile,dbAsset)

//                    CoroutineScope(Dispatchers.IO).launch {
//
//                    }

                }
            }
        }
    }

    fun bytesArrayFromAsset(asset :Asset)  : ByteArray{
        val assetInputStream : InputStream= Tasks.await(Wearable.getDataClient(applicationContext).getFdForAsset(asset))
            .inputStream

        return assetInputStream.readBytes()

    }

    //bytes 를 파일로 만드는 method
    fun writeBytesToFile(theFile : File,bytes : ByteArray){
        var bos : BufferedOutputStream? = null
        try{
            val fos : FileOutputStream = FileOutputStream(theFile)
            bos = BufferedOutputStream(fos)
            bos.write(bytes)
        }catch (e : IOException){
            Log.d("도원"," 에러에러   ${e.localizedMessage}")
        }finally {
            try {
                bos?.flush()
                bos?.close()
            }catch (e1 : Exception){
                Log.d("도원"," 에러에러2   ${e1.localizedMessage}")
            }
        }
    }

    override fun onMessageReceived(p0: MessageEvent) {
        Log.d("도원"," onMessageReceived   ")

    }

    override fun onCapabilityChanged(p0: CapabilityInfo) {
        Log.d("도원"," onCapabilityChanged   ")
    }

    private class DataItemGenerator(context: Context) : Runnable {

        var count = 0
        val context=context
        override fun run() {
            val putDataMapRequest = PutDataMapRequest.create("/taskDB")

//            putDataMapRequest.dataMap.putInt(
//                "db", count++
//            )
            val request = putDataMapRequest.asPutDataRequest()
            request.setUrgent()

            val dataItemTask = Wearable.getDataClient(context).putDataItem(request)
            try {
                // Block on a task and get the result synchronously (because this is on a background
                // thread).
                val dataItem = Tasks.await(dataItemTask)
            } catch (exception: ExecutionException) {
            } catch (exception: InterruptedException) {
            }
        }
    }


}