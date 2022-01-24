package com.dwstyle.calenderbydw.utils

import android.app.Activity
import android.graphics.Point
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.WindowInsets
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.wear.widget.WearableLinearLayoutManager
import androidx.wear.widget.WearableRecyclerView
import com.dwstyle.calenderbydw.R
import com.dwstyle.calenderbydw.adapters.YearPickerAdapter

class MyDatePicker : Activity() {
    private lateinit var RCYear :RecyclerView
    private lateinit var RCMonth :RecyclerView
    private lateinit var RCDay :RecyclerView
    private lateinit var  adapter :YearPickerAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.my_spinner_activity)

        RCYear=findViewById(R.id.RCYear);
//        RCYear.layoutManager= LinearLayoutManager(applicationContext, LinearLayoutManager.VERTICAL,false)
        RCYear.setHasFixedSize(true)
        RCYear.layoutManager= CenterRecyclerManager(this)

        RCMonth=findViewById(R.id.RCMonth)
        RCMonth.layoutManager= LinearLayoutManager(applicationContext, LinearLayoutManager.VERTICAL,false)
        RCDay=findViewById(R.id.RCDay)
        RCDay.layoutManager= LinearLayoutManager(applicationContext, LinearLayoutManager.VERTICAL,false)

        val strs = ArrayList<String>()
        for (a in 1900..2100){
            strs.add(a.toString())
        }
        adapter = YearPickerAdapter(strs)
        RCYear.adapter=adapter
        val strs2 = ArrayList<String>()
        for (a in 1..12){
            strs2.add(a.toString())
        }
//        RCYear.addItemDecoration(TaskRecyclerViewDecoration(this))

//        RCYear.apply {
//            bezelFraction = 0.5f
//            scrollDegreesPerScreen = 90f
//
//        }
//        RCYear.layoutManager=
//            WearableLinearLayoutManager(this,object  : WearableLinearLayoutManager.LayoutCallback(){
//            private var progressToCenter: Float = 0f
//            override fun onLayoutFinished(child: View?, parent: RecyclerView?) {
//                if (parent==null){
//                    return
//                }
//                child?.apply {
//                    // Figure out % progress from top to bottom
//                    val centerOffset = height.toFloat() / 2.0f / parent.height.toFloat()
//                    val yRelativeToCenterOffset = y / parent.height + centerOffset
//
//                    // Normalize for center
//                    progressToCenter = Math.abs(0.5f - yRelativeToCenterOffset)
//                    // Adjust to the maximum scale
//                    progressToCenter = Math.min(progressToCenter, 0.9f)
////                    adapter.setTextColor(parent.getChildAdapterPosition(this)-4)
//                    scaleX = 1 - progressToCenter
//                    scaleY = 1 - progressToCenter
//
//
//                }
//            }
//
//
//        })

        val adapter2 = YearPickerAdapter(strs2)
        RCMonth.adapter=adapter2
        val strs3 = ArrayList<String>()
        for (a in 1..30){
            strs3.add(a.toString())
        }
        val adapter3 = YearPickerAdapter(strs3)
        RCDay.adapter=adapter3
    }



    suspend fun highText(pos : Int){
        RCYear.scrollToPosition(pos);
        RCYear.smoothScrollToPosition(pos);
        adapter.setSele(pos);
        adapter.notifyDataSetChanged();
    }

    private fun scrollSelectedItemToCenter(selectedPosition: Int) {
        val layoutManager = RCYear.layoutManager as? LinearLayoutManager

        // 가운데로 스크롤할 아이템이 왼쪽으로 부터 떨어진 거리.
        // == 스크린너비/2 - 아이템의너비/2
        val offset = (getDeviceHeight(this) / 2 - RCYear.getChildAt(selectedPosition).height / 2)

        // [selectedPosition]번째 아이템을 왼쪽 가장자리에서 offset 만큼 떨어진 위치로 스크롤한다.
        layoutManager?.scrollToPositionWithOffset(selectedPosition, offset)
    }

    fun getDeviceHeight(activity: Activity) :Int{
        val outMetrics = DisplayMetrics()
        val height :Int
        val width :Int
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            val windowMetrics = activity.windowManager.currentWindowMetrics
            val windowInsets : WindowInsets = windowMetrics.windowInsets

            val insets =windowInsets.getInsetsIgnoringVisibility(
                WindowInsets.Type.navigationBars())
            val insetsWidth =insets.right+insets.left
            val insetsHeight= insets.bottom+insets.top
            val bounds = windowMetrics.bounds
            width=bounds.width()-insetsWidth
            height=bounds.height()-insetsHeight
        } else {
            val size = Point()
            val display = activity.windowManager.defaultDisplay
            display?.getSize(size)
            height=size.y
            width=size.x
        }

        return height
    }
}