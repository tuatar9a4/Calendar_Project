package com.devd.calenderbydw

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.devd.calenderbydw.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navHostFragment: NavHostFragment
    private lateinit var navController: NavController
    private val backPressCallback: OnBackPressedCallback = getBackPressCallback()
    private var backKeyPressedTime: Long = 0
    private var backPressToast: Toast? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        Timber.d("dbpath :${applicationContext.getDatabasePath("calendar_db").absolutePath}")
        setBackPressFunc()
        setContentView(binding.root)
        setNavigation()
    }

    private fun setNavigation() {
        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        binding.bottomNav.setupWithNavController(navController)
        binding.bottomNav.setOnApplyWindowInsetsListener(null)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val visibility = if (
                destination.id == R.id.calendarFragment ||
                destination.id == R.id.diaryListFragment ||
                destination.id == R.id.myFragment
            ) {
                View.VISIBLE
            } else {
                View.GONE
            }
            binding.bottomNav.visibility = visibility
            binding.bottomNav.visibility = visibility
        }

        navController = navHostFragment.navController
    }

    private fun setBackPressFunc() {
        this.onBackPressedDispatcher.addCallback(this, backPressCallback)
    }

    private fun getBackPressCallback() = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (navController.navigateUp()) {
                return
            } else if (System.currentTimeMillis() - backKeyPressedTime > 2000) {
                backKeyPressedTime = System.currentTimeMillis()
                backPressToast =
                    Toast.makeText(applicationContext, "한번 더 눌러 종료", Toast.LENGTH_SHORT)
                backPressToast?.show()
            } else {
                backPressToast?.cancel()
                finishAffinity()
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        backPressCallback.remove()
    }
}