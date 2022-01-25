package com.dwstyle.calenderbydw.utils

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewTreeObserver
import android.widget.Button
import android.widget.TextView
import androidx.core.view.get
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.wear.widget.WearableLinearLayoutManager
import androidx.wear.widget.WearableRecyclerView
import com.dwstyle.calenderbydw.R
import com.dwstyle.calenderbydw.adapters.PickerAdapter
import org.joda.time.DateTime

class MyDatePicker : Activity() {
    private lateinit var RCYear :RecyclerView
    private lateinit var RCMonth :WearableRecyclerView
    private lateinit var RCDay :RecyclerView
    private lateinit var  yearAdapter :PickerAdapter
    private lateinit var monthAdapter : PickerAdapter
    private lateinit var dayAdapter : PickerAdapter

    private lateinit var btnplus :Button
    private var count =0;

    private val date =DateTime(System.currentTimeMillis());

    private var selectYear ="2022";

    private var itemHeight :Int=0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.my_spinner_activity)

        RCYear=findViewById(R.id.RCYear);
//        RCYear.layoutManager= LinearLayoutManager(applicationContext, LinearLayoutManager.VERTICAL,false)
        RCYear.setHasFixedSize(true)
        RCYear.layoutManager= CenterRecyclerManager(this,itemHeight)
        RCMonth=findViewById(R.id.RCMonth)
        RCMonth.setHasFixedSize(true);
        RCDay=findViewById(R.id.RCDay)
        RCDay.layoutManager= LinearLayoutManager(applicationContext, LinearLayoutManager.VERTICAL,false)


        itemHeight=TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,15f,applicationContext.getResources().getDisplayMetrics()).toInt()
        btnplus=findViewById(R.id.btnplus)
        btnplus.setOnClickListener {
            if (count>600)count=0
            Log.d("도원","ScrollCount ${count}")
//            RCMonth.scrollTo(count,count)
            RCMonth.scrollBy(0,count)
            count+=20;
        }
        settingYearSelector()
        settingMonthSelector()
        settingDaySelector()
    }

    private fun settingYearSelector(){

        val yearText = ArrayList<String>()
        for (a in (date.year-50)..(date.year+51)){
            yearText.add(a.toString())
        }
        yearAdapter = PickerAdapter(yearText)

        RCYear.adapter=yearAdapter

        RCYear.addItemDecoration(DateTimePickerDecoration(this,itemHeight))
        RCYear.layoutManager=
            WearableLinearLayoutManager(this,object  : WearableLinearLayoutManager.LayoutCallback(){
                private var progressToCenter: Float = 0f
                override fun onLayoutFinished(child: View?, parent: RecyclerView?) {
                    if (parent==null){
                        return
                    }
                    child?.apply {
                        // Figure out % progress from top to bottom
                        val centerOffset = height.toFloat() / 2.0f / parent.height.toFloat()
                        val yRelativeToCenterOffset = y / parent.height + centerOffset

                        // Normalize for center
                        progressToCenter = Math.abs(0.5f - yRelativeToCenterOffset)
                        // Adjust to the maximum scale
                        progressToCenter = Math.min(progressToCenter, 0.7f)
//                    adapter.setTextColor(parent.getChildAdapterPosition(this)-4)
                        scaleX = 1 - progressToCenter
                        scaleY = 1 - progressToCenter
                        if (scaleX>0.92f && scaleY>0.92f){
                            val selectView=this.findViewById<TextView>(R.id.tvYear)
                            selectView.setTextColor(Color.parseColor("#0000FF"))
                            selectYear=selectView.text.toString()
                        }else{
                            val selectView=this.findViewById<TextView>(R.id.tvYear)
                            selectView.setTextColor(Color.parseColor("#FFFFFF"))
                        }
                    }
                }
            })
        RCYear.scrollToPosition(48)

    }

    private fun settingMonthSelector(){
        val llmanger =CenterRecyclerManager(this,itemHeight)
        llmanger.layoutCallback= object : WearableLinearLayoutManager.LayoutCallback() {
            private var progressToCenter: Float = 0f
            override fun onLayoutFinished(child: View?, parent: RecyclerView?) {
                if (parent==null){
                    return
                }
                child?.apply {
                    // Figure out % progress from top to bottom
                    val centerOffset = height.toFloat() / 2.0f / parent.height.toFloat()
                    val yRelativeToCenterOffset = y / parent.height + centerOffset

                    // Normalize for center
                    progressToCenter = Math.abs(0.5f - yRelativeToCenterOffset)
                    // Adjust to the maximum scale
                    progressToCenter = Math.min(progressToCenter, 0.7f)
//                    adapter.setTextColor(parent.getChildAdapterPosition(this)-4)
                    scaleX = 1 - progressToCenter
                    scaleY = 1 - progressToCenter
                    if (scaleX>0.92f && scaleY>0.92f){
                        val selectView=this.findViewById<TextView>(R.id.tvYear)
                        selectView.setTextColor(Color.parseColor("#0000FF"))
                        selectYear=selectView.text.toString()
                    }else{
                        val selectView=this.findViewById<TextView>(R.id.tvYear)
                        selectView.setTextColor(Color.parseColor("#FFFFFF"))
                    }
                }
            }
        }
        RCMonth.layoutManager= llmanger

        val monthText = ArrayList<String>()
        for (a in 1..12){
            monthText.add(a.toString())
        }
        monthAdapter = PickerAdapter(monthText)
        RCMonth.adapter=monthAdapter
        monthAdapter.setClickListener(object : PickerAdapter.OnDateItemClickListener{
            override fun OnDateItemClick(v: View, pos: Int) {
                Log.d("도원","Click : ${pos}")
                highText(pos)
            }
        })
        RCMonth.addItemDecoration(DateTimePickerDecoration(this,itemHeight))
        RCMonth.scrollToPosition(5)
        monthAdapter.setSelectedPosition(5)
        RCMonth.findViewHolderForAdapterPosition(4)?.itemView?.performClick()
//        RCMonth.layoutManager=
//            WearableLinearLayoutManager(this,object  : WearableLinearLayoutManager.LayoutCallback(){
//                private var progressToCenter: Float = 0f
//                override fun onLayoutFinished(child: View?, parent: RecyclerView?) {
//                    if (parent==null){
//                        return
//                    }
//                    child?.apply {
//                        // Figure out % progress from top to bottom
//                        val centerOffset = height.toFloat() / 2.0f / parent.height.toFloat()
//                        val yRelativeToCenterOffset = y / parent.height + centerOffset
//
//                        // Normalize for center
//                        progressToCenter = Math.abs(0.5f - yRelativeToCenterOffset)
//                        // Adjust to the maximum scale
//                        progressToCenter = Math.min(progressToCenter, 0.7f)
////                    adapter.setTextColor(parent.getChildAdapterPosition(this)-4)
//                        scaleX = 1 - progressToCenter
//                        scaleY = 1 - progressToCenter
//                        if (scaleX>0.92f && scaleY>0.92f){
//                            val selectView=this.findViewById<TextView>(R.id.tvYear)
//                            selectView.setTextColor(Color.parseColor("#0000FF"))
//                            selectYear=selectView.text.toString()
//                        }else{
//                            val selectView=this.findViewById<TextView>(R.id.tvYear)
//                            selectView.setTextColor(Color.parseColor("#FFFFFF"))
//                        }
//                    }
//                }
//            })
//        RCMonth.getChildAt(4).performClick()


    }

    private fun settingDaySelector(){
        val dayText = ArrayList<String>()
        for (a in 1..30){
            dayText.add(a.toString())
        }
        dayAdapter = PickerAdapter(dayText)
        RCDay.adapter=dayAdapter
        RCDay.scrollToPosition(date.dayOfMonth-2)
    }

    public fun highText( pos: Int) {
        RCMonth.scrollToPosition(pos);
        RCMonth.smoothScrollToPosition(pos);
        monthAdapter.setSelectedPosition(pos);
        monthAdapter.notifyDataSetChanged();
    }

}