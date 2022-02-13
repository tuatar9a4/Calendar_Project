package com.devd.calenderbydw.utils

import android.content.Context
import android.content.SharedPreferences

class SharedUtils(private val context: Context) {



        private fun getSharedPreference() : SharedPreferences =context.getSharedPreferences("sharedData",Context.MODE_PRIVATE)

}