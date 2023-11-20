package com.devd.calenderbydw.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.viewModels
import com.devd.calenderbydw.MainActivity
import com.devd.calenderbydw.R
import com.devd.calenderbydw.databinding.ActivitySplashBinding
import com.devd.calenderbydw.utils.Event
import com.devd.calenderbydw.utils.EventObserver
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {
    private lateinit var binding : ActivitySplashBinding
    private val viewModel by viewModels<SplashViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater,null,false)
        setContentView(binding.root)
        viewModel.checkCalendarDB{progress ->
            runOnUiThread {
                binding.tvCalendarProgress.text="달력 생성중\n${progress}%"
            }
        }
         viewModel.completeCalendarCreate.observe(this,EventObserver{
                Handler(Looper.getMainLooper()).postDelayed(Runnable {
                    val i=Intent(applicationContext, MainActivity::class.java)
                    startActivity(i)
                    finish()
                },500L)
            })
    }
}