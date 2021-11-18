package com.dwstyle.calenderbydw.utils

import android.app.Activity
import android.content.Context
import android.graphics.Point
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowInsets

class LengthUtils {

    companion object LengthCompanion{

        fun getDeviceHeight(activity: Activity) :Int{
            val height :Int
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                val windowMetrics = activity.windowManager.currentWindowMetrics
                val windowInsets :WindowInsets = windowMetrics.windowInsets

                val insets =windowInsets.getInsetsIgnoringVisibility(
                    WindowInsets.Type.navigationBars())
                val insetsWidth =insets.right+insets.left
                val insetsHeight= insets.bottom+insets.top
                val bounds = windowMetrics.bounds
                height=bounds.height()-insetsHeight
            } else {
                val size = Point()
                val display = activity.windowManager.defaultDisplay
                display?.getSize(size)
                height=size.y
            }
            return height
        }

        fun getDeviceWidth(activity: Activity) :Int{
            val width :Int
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                val windowMetrics = activity.windowManager.currentWindowMetrics
                val windowInsets :WindowInsets = windowMetrics.windowInsets

                val insets =windowInsets.getInsetsIgnoringVisibility(
                    WindowInsets.Type.navigationBars())
                val insetsWidth =insets.right+insets.left
                val insetsHeight= insets.bottom+insets.top
                val bounds = windowMetrics.bounds
                width=bounds.width()-insetsWidth
            } else {
                val size = Point()
                val display = activity.windowManager.defaultDisplay
                display?.getSize(size)
                width=size.x
            }

            return width
        }
    }


}