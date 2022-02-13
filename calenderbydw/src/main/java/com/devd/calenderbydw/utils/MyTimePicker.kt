package com.devd.calenderbydw.utils

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.wear.widget.WearableLinearLayoutManager
import androidx.wear.widget.WearableRecyclerView
import com.devd.calenderbydw.R
import com.devd.calenderbydw.adapters.PickerAdapter
import kotlinx.coroutines.*
import org.joda.time.DateTime
import org.joda.time.DateTimeFieldType
import org.joda.time.LocalTime

class MyTimePicker : Activity() {
    private lateinit var RCAmPm :WearableRecyclerView
    private lateinit var RCHour :WearableRecyclerView
    private lateinit var RCMinute :WearableRecyclerView
    private lateinit var ampmAdapter :PickerAdapter
    private lateinit var hourAdapter : PickerAdapter
    private lateinit var minuteAdapter : PickerAdapter

    private lateinit var tvSelectTime :TextView
    private lateinit var btnCompelete :Button
    private lateinit var btnCancel : Button
    private var count =0;

    private val date =DateTime(System.currentTimeMillis()).toLocalDate();
    private val time =LocalTime.now()

    private var selectAMPM ="AM";
    private var selectHour ="03";
    private var selectMinute ="22";

    private var itemHeight :Int=0

    private lateinit var datSettingJob: Job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.my_spinner_activity)
        tvSelectTime=findViewById(R.id.tvSelectDateTime)
        tvSelectTime.text = LocalTime.now().toString("aa hh:mm")
        RCAmPm=findViewById(R.id.RCYear);
        RCAmPm.setHasFixedSize(true)
        RCHour=findViewById(R.id.RCMonth)
        RCHour.setHasFixedSize(true);
        RCMinute=findViewById(R.id.RCDay)
        Log.d("도원","time : ${DateTime.now().toLocalDateTime().toString("aa.hh.mm")}")
        RCMinute.layoutManager= LinearLayoutManager(applicationContext, LinearLayoutManager.VERTICAL,false)
        itemHeight=TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,15f,applicationContext.getResources().getDisplayMetrics()).toInt()
//        datSettingJob = CoroutineScope(Dispatchers.Main).launch {
//            delay(100)
//            setDayText(selectHour.toInt())
//        }
        btnCancel=findViewById(R.id.btnCancel)
        btnCancel.setOnClickListener {
            finish()
        }
        btnCompelete=findViewById(R.id.btnCompelete)
        btnCompelete.setOnClickListener {
            val intent =Intent()
            intent.putExtra(Consts.TASKCREATETIME,"${selectAMPM}.${selectHour}.${selectMinute}")
            setResult(RESULT_OK,intent)
            finish()
        }
        settingYearSelector()
        settingMonthSelector()
        settingDaySelector()

    }

    private fun settingYearSelector(){
        val llmanger =CenterRecyclerManager(this,itemHeight)
        llmanger.layoutCallback = object : WearableLinearLayoutManager.LayoutCallback(){
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
                        selectAMPM=selectView.text.toString()
                        tvSelectTime.text=tvSelectTime.text.toString().replaceRange(0,2,selectAMPM)
                    }else{
                        val selectView=this.findViewById<TextView>(R.id.tvYear)
                        selectView.setTextColor(Color.parseColor("#FFFFFF"))
                    }
                }

            }
        }
        RCAmPm.layoutManager=llmanger

        val yearText = arrayListOf<String>("AM","PM")
//        for (a in (date.year-50)..(date.year+51)){
//            yearText.add(a.toString())
//        }
        ampmAdapter = PickerAdapter(yearText)
        RCAmPm.adapter=ampmAdapter
        ampmAdapter.setClickListener(object : PickerAdapter.OnDateItemClickListener{
            override fun OnDateItemClick(v: View, pos: Int) {
                Log.d("도원","Click : ${pos}")
                highText(RCAmPm,ampmAdapter,pos)
            }
        })
        RCAmPm.addItemDecoration(DateTimePickerDecoration(this,itemHeight))
        Log.d("도원","ㅇㅇㅇ ${time.plusHours(2).get(DateTimeFieldType.clockhourOfDay())}")
        if (time.get(DateTimeFieldType.clockhourOfDay())<12){
            RCAmPm.scrollToPosition(0)
            RCAmPm.smoothScrollToPosition(0)
        }else{
            RCAmPm.scrollToPosition(1)
            RCAmPm.smoothScrollToPosition(1)
        }


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
                    progressToCenter = Math.min(progressToCenter, 0.65f)
//                    adapter.setTextColor(parent.getChildAdapterPosition(this)-4)
                    scaleX = 1 - progressToCenter
                    scaleY = 1 - progressToCenter
                    if (scaleX>0.92f && scaleY>0.92f){
                        val selectView=this.findViewById<TextView>(R.id.tvYear)
                        selectView.setTextColor(Color.parseColor("#0000FF"))
                        selectHour=if(selectView.text.toString().length==1) "0${selectView.text.toString()}" else selectView.text.toString()
//                        if (tvSelectTime.text.toString().substring(5,7) != selectHour){
//                            try{
//                                datSettingJob.cancel()
//                                datSettingJob = CoroutineScope(Dispatchers.Main).launch {
//                                    delay(100)
//                                    setDayText(selectHour.toInt())
//                                }
//                            }catch (e : Exception){
//                                Log.d("도원","Expction : ${e.localizedMessage}");
//                            }
//                        }
                        tvSelectTime.text=tvSelectTime.text.toString().replaceRange(3,5,selectHour)

                    }else{
                        val selectView=this.findViewById<TextView>(R.id.tvYear)
                        selectView.setTextColor(Color.parseColor("#FFFFFF"))
                    }
                }
            }
        }
        RCHour.layoutManager= llmanger

        val monthText = ArrayList<String>()
        for (a in 1..12){
            monthText.add(if(a.toString().length==1) "0${a}" else "${a}")
        }
        hourAdapter = PickerAdapter(monthText)
        RCHour.adapter=hourAdapter
        hourAdapter.setClickListener(object : PickerAdapter.OnDateItemClickListener{
            override fun OnDateItemClick(v: View, pos: Int) {
                highText(RCHour,hourAdapter,pos)
            }
        })
        RCHour.addItemDecoration(DateTimePickerDecoration(this,itemHeight))
        RCHour.scrollToPosition(time.hourOfDay-1);
        RCHour.smoothScrollToPosition(time.hourOfDay-1);
        hourAdapter.setSelectedPosition(time.hourOfDay-1);
//        monthAdapter.notifyDataSetChanged();
//        monthAdapter.setSelectedPosition(0)

    }

    private fun settingDaySelector(){
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
                    progressToCenter = Math.min(progressToCenter, 0.65f)
//                    adapter.setTextColor(parent.getChildAdapterPosition(this)-4)
                    scaleX = 1 - progressToCenter
                    scaleY = 1 - progressToCenter
                    if (scaleX>0.92f && scaleY>0.92f){
                        val selectView=this.findViewById<TextView>(R.id.tvYear)
                        selectView.setTextColor(Color.parseColor("#0000FF"))
                        selectMinute=if(selectView.text.toString().length==1) "0${selectView.text.toString()}" else selectView.text.toString()
                        tvSelectTime.text=tvSelectTime.text.toString().replaceRange(6,8,selectMinute)
                    }else{
                        val selectView=this.findViewById<TextView>(R.id.tvYear)
                        selectView.setTextColor(Color.parseColor("#FFFFFF"))
                    }
                }
            }
        }
        RCMinute.layoutManager= llmanger
        val dayText = ArrayList<String>()
        for (a in 0..59){
            dayText.add(if(a.toString().length==1) "0${a}" else "${a}")
        }
        minuteAdapter = PickerAdapter(dayText)
        RCMinute.adapter=minuteAdapter
        minuteAdapter.setClickListener(object : PickerAdapter.OnDateItemClickListener{
            override fun OnDateItemClick(v: View, pos: Int) {
                Log.d("도원","Click : ${pos}")
                highText(RCMinute,minuteAdapter,pos)
            }
        })
        RCMinute.addItemDecoration(DateTimePickerDecoration(this,itemHeight))
        RCMinute.scrollToPosition(time.minuteOfHour);
        RCMinute.smoothScrollToPosition(time.minuteOfHour);
//        try{
//            datSettingJob.cancel()
//            datSettingJob = CoroutineScope(Dispatchers.Main).launch {
//                delay(100)
//                setDayText(date.monthOfYear.toInt())
////                dayAdapter.setSelectedPosition(date.monthOfYear-1);
////                RCDay.scrollToPosition(date.dayOfMonth-2)
////                dayAdapter.setSelectedPosition(date.dayOfMonth-2)
//            }
//        }catch (e : Exception){
//            Log.d("도원","Expction : ${e.localizedMessage}");
//        }

    }

    public fun highText( rcView : WearableRecyclerView,pickAdapter :PickerAdapter,pos: Int) {
        rcView.scrollToPosition(pos);
        rcView.smoothScrollToPosition(pos);
        pickAdapter.setSelectedPosition(pos);
//        pickAdapter.notifyDataSetChanged();
    }

}