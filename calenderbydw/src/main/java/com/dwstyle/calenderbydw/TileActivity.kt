package com.dwstyle.calenderbydw

import android.app.Activity
import android.content.ComponentName
import android.os.Bundle
import android.widget.FrameLayout
import androidx.wear.tiles.manager.TileUiClient
import com.dwstyle.calenderbydw.databinding.ActivityTileBinding

class TileActivity : Activity() {

    private lateinit var binding: ActivityTileBinding

    private lateinit var tileUiClient :TileUiClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val rootLayout = findViewById<FrameLayout>(R.id.tile_container)
        tileUiClient = TileUiClient(
            context = this,
            component = ComponentName(this,CalendarTile::class.java),
            parentView = rootLayout
        )
        tileUiClient.connect()

    }

    override fun onDestroy() {
        super.onDestroy()

        tileUiClient.close()
    }
}