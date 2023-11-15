package com.devd.calenderbydw

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)


       Handler(Looper.getMainLooper()).postDelayed(Runnable {
           val i=Intent(applicationContext,MainActivity::class.java)
           startActivity(i)
           finish()
       },1000L)
    }
}