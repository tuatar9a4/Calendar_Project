package com.devd.calenderbydw.utils

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.util.Log

class SharedDataUtils {
    companion object{
        private const val PREFERENCE_WIDGET_DATE="SETTING_WIDGET_MILLIS_DATE"

        private fun getSharedPreferences(mContext :Context) : SharedPreferences {
            return mContext.getSharedPreferences("calendar_data", MODE_PRIVATE)
        }

        fun setDateMillis(context: Context,dateMillis :Long) {
            val editor = getSharedPreferences(context).edit()
            editor.putLong(PREFERENCE_WIDGET_DATE, dateMillis).apply()
        }

        fun getDateMillis(context: Context) : Long {
            Log.d("MillsCheck","Check -> ${getSharedPreferences(context).getLong(PREFERENCE_WIDGET_DATE, 0)}")
            return getSharedPreferences(context).getLong(PREFERENCE_WIDGET_DATE, 0)
        }

        private  const val WIDGET_CLICK_DATE ="CLICK_WIDGET_DATE"

        fun setClickDate(context: Context,date :String) {
            val editor = getSharedPreferences(context).edit()
            editor.putString(WIDGET_CLICK_DATE, date).apply()
        }

        fun getClickDate(context: Context) : String? {
            return getSharedPreferences(context).getString(WIDGET_CLICK_DATE,null)
        }

    }
}