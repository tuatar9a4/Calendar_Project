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
import java.lang.Exception

class MyDatePicker : Activity() {
    private lateinit var RCYear :WearableRecyclerView
    private lateinit var RCMonth :WearableRecyclerView
    private lateinit var RCDay :WearableRecyclerView
    private lateinit var  yearAdapter :PickerAdapter
    private lateinit var monthAdapter : PickerAdapter
    private lateinit var dayAdapter : PickerAdapter

    private lateinit var tvSelectDateTime :TextView
    private lateinit var btnCompelete :Button
    private lateinit var btnCancel : Button
    private var count =0;

    private val date =DateTime(System.currentTimeMillis()).toLocalDate();

    private var selectYear ="2022";
    private var selectMonth ="12";
    private var selectDay ="02";

    private var itemHeight :Int=0

    private lateinit var datSettingJob: Job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.my_spinner_activity)
        tvSelectDateTime=findViewById(R.id.tvSelectDateTime)
        RCYear=findViewById(R.id.RCYear);
        RCYear.setHasFixedSize(true)
        RCMonth=findViewById(R.id.RCMonth)
        RCMonth.setHasFixedSize(true);
        RCDay=findViewById(R.id.RCDay)
        RCDay.layoutManager= LinearLayoutManager(applicationContext, LinearLayoutManager.VERTICAL,false)
        itemHeight=TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,15f,applicationContext.getResources().getDisplayMetrics()).toInt()
        datSettingJob = CoroutineScope(Dispatchers.Main).launch {
            delay(100)
            setDayText(selectMonth.toInt())
        }
        btnCancel=findViewById(R.id.btnCancel)
        btnCancel.setOnClickListener {
            finish()
        }
        btnCompelete=findViewById(R.id.btnCompelete)
        btnCompelete.setOnClickListener {
            val intent =Intent()
            intent.putExtra(Consts.TASKCREATEDAET,"${selectYear}.${selectMonth}.${selectDay}")
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
                        selectYear=selectView.text.toString()
                        tvSelectDateTime.text=tvSelectDateTime.text.toString().replaceRange(0,4,selectYear)
                    }else{
                        val selectView=this.findViewById<TextView>(R.id.tvYear)
                        selectView.setTextColor(Color.parseColor("#FFFFFF"))
                    }
                }

            }
        }
        RCYear.layoutManager=llmanger

        val yearText = ArrayList<String>()
        for (a in (date.year-50)..(date.year+51)){
            yearText.add(a.toString())
        }
        yearAdapter = PickerAdapter(yearText)
        RCYear.adapter=yearAdapter
        yearAdapter.setClickListener(object : PickerAdapter.OnDateItemClickListener{
            override fun OnDateItemClick(v: View, pos: Int) {
                Log.d("도원","Click : ${pos}")
                highText(RCYear,yearAdapter,pos)
            }
        })
        RCYear.addItemDecoration(DateTimePickerDecoration(this,itemHeight))
        RCYear.scrollToPosition(50);
        RCYear.smoothScrollToPosition(50);

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
                        selectMonth=if(selectView.text.toString().length==1) "0${selectView.text.toString()}" else selectView.text.toString()
                        if (tvSelectDateTime.text.toString().substring(5,7) != selectMonth){
                            try{
                                datSettingJob.cancel()
                                datSettingJob = CoroutineScope(Dispatchers.Main).launch {
                                    delay(100)
                                    setDayText(selectMonth.toInt())
                                }
                            }catch (e : Exception){
                                Log.d("도원","Expction : ${e.localizedMessage}");
                            }
                        }
                        tvSelectDateTime.text=tvSelectDateTime.text.toString().replaceRange(5,7,selectMonth)

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
                highText(RCMonth,monthAdapter,pos)
            }
        })
        RCMonth.addItemDecoration(DateTimePickerDecoration(this,itemHeight))
        Log.d("도원","date.monthOfYear : ${date.monthOfYear}")
        RCMonth.scrollToPosition(date.monthOfYear);
        RCMonth.smoothScrollToPosition(date.monthOfYear-1);
        monthAdapter.setSelectedPosition(date.monthOfYear-1);
//        monthAdapter.notifyDataSetChanged();
//        monthAdapter.setSelectedPosition(0)

    }

    private fun settingDaySelector(){
//        val firstDate =date.withDayOfMonth(1)
//        val selectMonth =if (firstDate.monthOfYear.toString().length==1) "0${firstDate.monthOfYear.toString()}" else firstDate.monthOfYear.toString()
//        val dayText = ArrayList<String>()
//        for (a in 0 until 31){
//            var temp =firstDate.plusDays(a).toString("MM.dd")
//            val tmpe2 = temp.split(".")
//            temp = if (tmpe2[1].length==1) "0${tmpe2[1]}" else tmpe2[1]
//            Log.d("도원","temp : ${temp}  | firstDate.monthOfYear.toString() : ${firstDate.monthOfYear.toString()} || tmpe2[0] : ${tmpe2[0]}")
//            if (selectMonth != tmpe2[0]){
//                break
//            }
//            dayText.add(temp)
//        }
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
                        selectDay=if(selectView.text.toString().length==1) "0${selectView.text.toString()}" else selectView.text.toString()
                        tvSelectDateTime.text=tvSelectDateTime.text.toString().replaceRange(8,10,selectDay)
                    }else{
                        val selectView=this.findViewById<TextView>(R.id.tvYear)
                        selectView.setTextColor(Color.parseColor("#FFFFFF"))
                    }
                }
            }
        }
        RCDay.layoutManager= llmanger
        val dayText = ArrayList<String>()
        dayAdapter = PickerAdapter(dayText)
        RCDay.adapter=dayAdapter
        dayAdapter.setClickListener(object : PickerAdapter.OnDateItemClickListener{
            override fun OnDateItemClick(v: View, pos: Int) {
                Log.d("도원","Click : ${pos}")
                highText(RCDay,dayAdapter,pos)
            }
        })
        RCDay.addItemDecoration(DateTimePickerDecoration(this,itemHeight))

        try{
            datSettingJob.cancel()
            datSettingJob = CoroutineScope(Dispatchers.Main).launch {
                delay(100)
                setDayText(date.monthOfYear.toInt())
//                dayAdapter.setSelectedPosition(date.monthOfYear-1);
//                RCDay.scrollToPosition(date.dayOfMonth-2)
//                dayAdapter.setSelectedPosition(date.dayOfMonth-2)
            }
        }catch (e : Exception){
            Log.d("도원","Expction : ${e.localizedMessage}");
        }

    }

    private fun setDayText(month : Int){
        val firstDate = DateTime().withMonthOfYear(month).withDayOfMonth(1)
        val selectMonth =if (firstDate.monthOfYear.toString().length==1) "0${firstDate.monthOfYear.toString()}" else firstDate.monthOfYear.toString()
        val dayText = ArrayList<String>()
        for (a in 0 until 31){
            var temp =firstDate.plusDays(a).toString("MM.dd")
            val tmpe2 = temp.split(".")
            temp = if (tmpe2[1].length==1) "0${tmpe2[1]}" else tmpe2[1]
            if (selectMonth != tmpe2[0]){
                break
            }
            dayText.add(temp)
        }
        dayAdapter.setItems(dayText)
        if (selectMonth==if (date.monthOfYear.toString().length==1) "0${date.monthOfYear.toString()}" else date.monthOfYear.toString()){
            RCDay.scrollToPosition(date.dayOfMonth-1);
            RCDay.smoothScrollToPosition(date.dayOfMonth-1);
        }

    }

    public fun highText( rcView : WearableRecyclerView,pickAdapter :PickerAdapter,pos: Int) {
        rcView.scrollToPosition(pos);
        rcView.smoothScrollToPosition(pos);
        pickAdapter.setSelectedPosition(pos);
//        pickAdapter.notifyDataSetChanged();
    }

}