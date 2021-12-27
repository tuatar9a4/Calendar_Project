package com.dwstyle.calenderbydw.utils

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences

class SharedDataUtils {
    companion object{
        private val PREFERENCE_WIDGET_DATE="SACED_WIDGET_MILLIS_DATE"

        private fun getSharedPreferences(mContext :Context) : SharedPreferences {
            return mContext.getSharedPreferences("calendar_data", MODE_PRIVATE);
        }

        public fun setDateMillis(context: Context,dateMillis :Long) {
            val editor = getSharedPreferences(context).edit();
            editor.putLong(PREFERENCE_WIDGET_DATE, dateMillis).apply();
        }

        public fun getDateMillis(context: Context) : Long {
            return getSharedPreferences(context).getLong(PREFERENCE_WIDGET_DATE, 0);
        }

    }
}