package com.dwstyle.calenderbydw

import android.app.Activity
import android.content.ComponentName
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.wear.tiles.TileService
import androidx.wear.tiles.manager.TileUiClient
import com.dwstyle.calenderbydw.databinding.ActivityMainBinding
import com.google.android.gms.wearable.PutDataRequest

class MainActivity : ComponentActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var tileUiClient :TileUiClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_main)
//        타일 새로 고침
        TileService.getUpdater(applicationContext).requestUpdate(Test::class.java)
        val rootLayout = findViewById<FrameLayout>(R.id.tile_container)
        tileUiClient = TileUiClient(
            context = this,
            component = ComponentName(this,Test::class.java),
            parentView = rootLayout
        )
        tileUiClient.connect()


    }

    override fun onDestroy() {
        super.onDestroy()
        tileUiClient.close()
    }
}