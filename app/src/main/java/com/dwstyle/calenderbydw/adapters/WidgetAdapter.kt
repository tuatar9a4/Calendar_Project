package com.dwstyle.calenderbydw.adapters

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.graphics.Color
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.dwstyle.calenderbydw.CalendarWidget
import com.dwstyle.calenderbydw.R
import com.dwstyle.calenderbydw.database.TaskDatabaseHelper
import com.dwstyle.calenderbydw.utils.SharedDataUtils
import org.joda.time.DateTime
import org.joda.time.DateTimeConstants

class WidgetAdapter : RemoteViewsService(){
    //#7b9acc 파랑
    //#FCF6F5 흰색

    override fun onGetViewFactory(intent: Intent?): RemoteViewsFactory {
        return StackRemoteViewsFactory(this.applicationContext,intent)

    }

//
    class StackRemoteViewsFactory(private val context:Context,private val intent : Intent?) :
        RemoteViewsFactory {


        private val mCount = 49
        private val mWidgetItem =ArrayList<String>()

        private var monthDayList= listOf<String>()

        private var mAppWidgetId =0

        private lateinit var dbHelper : TaskDatabaseHelper
        private lateinit var database : SQLiteDatabase

        private lateinit var selectMonth : DateTime
        //일정 hashMap
        private val taskMap = HashMap<String,String>()

        private var todayDate=""
        private var selectedDate : String? =null

        private var receiveTime:Long =0L

        override fun onCreate() {
            if (intent != null) {
                mAppWidgetId=intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,AppWidgetManager.INVALID_APPWIDGET_ID)
                receiveTime = intent.getLongExtra("showDate",0)
            }
            Log.d("도원","onCreate")
            val weekStr = arrayListOf("Sun","Mon","Tue","Wen","Thu","Fri","Sat")
            for (i in weekStr){
                mWidgetItem.add(i)
            }
            monthDayList=getMonthList(DateTime(receiveTime).withDayOfMonth(1))
            for (i in monthDayList){
                mWidgetItem.add(i)
            }
            getTaskInfo(monthDayList)

        }

        private fun getMonthList(dateTime: DateTime): List<String> {
            todayDate = DateTime(System.currentTimeMillis()).toString("YYYY.MM.dd.E")
            selectedDate=SharedDataUtils.getClickDate(context)
            val list = mutableListOf<String>()

            val date = dateTime.withDayOfMonth(1)
            val prev = getPrevOffSet(date)
            selectMonth=date
            val startValue = date.minusDays(prev)

            val totalDay = DateTimeConstants.DAYS_PER_WEEK * 6

            for (i in 0 until totalDay) {
                list.add(DateTime(startValue.plusDays(i)).toString("YYYY.MM.dd.E"))
            }

            return list
        }

        private fun getPrevOffSet(dateTime: DateTime): Int {
            var prevMonthTailOffset = dateTime.dayOfWeek

            if (prevMonthTailOffset >= 7) prevMonthTailOffset %= 7

            return prevMonthTailOffset
        }

        override fun onDataSetChanged() {
            mWidgetItem.clear()
            val weekStr = arrayListOf("Sun","Mon","Tue","Wen","Thu","Fri","Sat")
            for (i in weekStr){
                mWidgetItem.add(i)
            }
            monthDayList=getMonthList(DateTime(SharedDataUtils.getDateMillis(context)).withDayOfMonth(1))
            for (i in monthDayList){
                mWidgetItem.add(i)
            }
            getTaskInfo(monthDayList)
        }


        override fun onDestroy() {

        }

        override fun getCount(): Int {
            return mCount
        }

        override fun getViewAt(position: Int): RemoteViews {
//            Log.d("도원","getViewAt : "+ mWidgetItem[position])
            val rv=RemoteViews(context.packageName, R.layout.widget_calender_grid_item)
            if (mWidgetItem[position] == "Mon" || mWidgetItem[position] == "Tue" || mWidgetItem[position] == "Wen"
                || mWidgetItem[position] == "Thu" || mWidgetItem[position] == "Fri" || mWidgetItem[position] == "Sat" ||
                mWidgetItem[position] == "Sun"
            ){
                rv.setViewVisibility(R.id.tvTask1, View.GONE)
                rv.setViewVisibility(R.id.tvTask2, View.GONE)
                rv.setViewVisibility(R.id.tvTaskCnt,View.GONE)
                rv.setViewVisibility(R.id.viewLine,View.VISIBLE)
                //날짜 색상
                if (mWidgetItem[position]=="Sun"){
                    rv.setTextColor(R.id.tvDate, Color.parseColor("#FF0000"))
                }else if(mWidgetItem[position]=="Sat"){
                    rv.setTextColor(R.id.tvDate, Color.parseColor("#0000FF"))
                }
                rv.setTextViewText(R.id.tvDate, mWidgetItem[position])
            }else{
                //일단 전부 안보이게 처리
                rv.setViewVisibility(R.id.tvTask1, View.INVISIBLE)
                rv.setViewVisibility(R.id.tvTask2, View.INVISIBLE)
                rv.setViewVisibility(R.id.tvTaskCnt,View.INVISIBLE)
                rv.setViewVisibility(R.id.viewLine,View.INVISIBLE)
                val str= mWidgetItem[position].split(".")
                //날짜는 적어주고
                rv.setTextViewText(R.id.tvDate,str[2])
                //날짜 색상
                val monthStr =selectMonth.toString("MM")
                Log.d("도원","")
                //오늘
                if (todayDate==mWidgetItem[position]){
                    Log.d("도원","todayDate : ${todayDate}   | mWidgetItem[position] : ${mWidgetItem[position]}")
                    rv.setInt(R.id.tvDate,"setBackgroundResource",R.drawable.widget_today_background_circle)
                    rv.setTextColor(R.id.tvDate, Color.parseColor("#FFFFFF"))
                }else{
                    rv.setInt(R.id.tvDate,"setBackgroundResource",R.drawable.widget_basic_background_circle)
                    //다른달
                    if (str[1] != monthStr){
                        when {
                            str[3]=="일" -> {
                                rv.setTextColor(R.id.tvDate, Color.parseColor("#88FF0000"))
                            }
                            str[3]=="토" -> {
                                rv.setTextColor(R.id.tvDate, Color.parseColor("#880000FF"))
                            }
                            else -> {
                                rv.setTextColor(R.id.tvDate, Color.parseColor("#88000000"))
                            }
                        }
                    }else{
                        //같은달
                        when {
                            str[3]=="일" -> {
                                rv.setTextColor(R.id.tvDate, Color.parseColor("#FF0000"))
                            }
                            str[3]=="토" -> {
                                rv.setTextColor(R.id.tvDate, Color.parseColor("#0000FF"))
                            }
                            else -> {
                                rv.setTextColor(R.id.tvDate, Color.parseColor("#000000"))
                            }
                        }
                    }
                }
                //선택 배경
                selectedDate?.let {
                    if (it==mWidgetItem[position]){
                        rv.setInt(R.id.calendarContainer,"setBackgroundResource",R.drawable.widget_container_background_select)
                    }else{
                        rv.setInt(R.id.calendarContainer,"setBackgroundResource",R.drawable.widget_container_background_basic)
                    }
                }

                var taskStr=""
                //해당 날짜가 taskMap 에 들어가 있으면 작성
                if (taskMap.containsKey("${str[0]}.${str[1]}.${str[2]}") ){
                    taskStr=taskMap["${str[0]}.${str[1]}.${str[2]}"].toString()
//                    rv.setTextViewText(R.id.tvTask1,taskMap[mWidgetItem.get(position)].toString())
                }
                if (taskMap.containsKey(str[3])){
                    taskStr = if (taskStr!=""){
                             "${taskStr}&${taskMap[str[3]].toString()}"
                            }else{
                                taskMap[str[3]].toString()
                            }

                }
                if (taskStr!=""){
                    val temp = taskStr.split("&")
                    when (temp.size) {
                        1 -> {
                            rv.setTextViewText(R.id.tvTask1, temp[0])
                            rv.setViewVisibility(R.id.tvTask1, View.VISIBLE)
                        }
                        2 -> {
                            rv.setTextViewText(R.id.tvTask1, temp[0])
                            rv.setViewVisibility(R.id.tvTask1, View.VISIBLE)
                            rv.setTextViewText(R.id.tvTask2, temp[1])
                            rv.setViewVisibility(R.id.tvTask2, View.VISIBLE)
                        }
                        else -> {
                            rv.setTextViewText(R.id.tvTask1, temp[0])
                            rv.setViewVisibility(R.id.tvTask1, View.VISIBLE)
                            rv.setTextViewText(R.id.tvTask2, temp[1])
                            rv.setViewVisibility(R.id.tvTask2, View.VISIBLE)
                            rv.setTextViewText(R.id.tvTaskCnt,"+${(temp.size-2)}")
                            rv.setViewVisibility(R.id.tvTaskCnt, View.VISIBLE)
                        }
                    }
                }
            }

            //보내는 intent
            val fillIntent = Intent()
            fillIntent.putExtra(CalendarWidget.COLLECTION_VIEW_EXTRA, mWidgetItem[position])
            rv.setOnClickFillInIntent(R.id.calendarContainer,fillIntent)

            return rv
        }

        override fun getLoadingView(): RemoteViews? {
            return RemoteViews(context.packageName, R.layout.blank_layout)
        }

        override fun getViewTypeCount(): Int {
            return 1
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun hasStableIds(): Boolean {
            return true
        }

        private fun getTaskInfo( monthDayList : List<String>){
            taskMap.clear()
            dbHelper= TaskDatabaseHelper(context,"task.db",null,3)
            database=dbHelper.readableDatabase
            getYearTask(database,monthDayList)
            getMonthTask(database,monthDayList)
            getDayTask(database,monthDayList)
            getWeekTask(database,monthDayList)
        }

        private fun getYearTask(database : SQLiteDatabase,monthDayList:List<String>){
            val cursor =TaskDatabaseHelper.searchDBOfYearRepeat(database)
            if (cursor!=null) {
                while (cursor.moveToNext()) {
                    val tempMonth :String= if ((cursor.getInt(cursor.getColumnIndex("month")).toString().length)==1 )
                        "0${(cursor.getInt(cursor.getColumnIndex("month")).toString())}" else (cursor.getInt(cursor.getColumnIndex("month")).toString())
                    val tempDay :String= if ((cursor.getInt(cursor.getColumnIndex("day")).toString().length)==1 )
                                        "0${(cursor.getInt(cursor.getColumnIndex("day")).toString())}" else (cursor.getInt(cursor.getColumnIndex("day")).toString())
                    for (a in monthDayList){
                        val split =a.split(".")
                        if (tempMonth == split[1] &&
                            tempDay == split[2]){
                            val tempStr1 ="${split[0]}.${split[1]}.${tempDay}"
                            val tempStr2 = cursor.getString(cursor.getColumnIndex("title"))
                            if (taskMap[tempStr1] ==null){
                                taskMap[tempStr1] = tempStr2
                            }else{
                                taskMap[tempStr1] = "${taskMap[tempStr1]}&${tempStr2}"
                            }
                        }
                    }
                }
            }
        }

        private fun getMonthTask(database : SQLiteDatabase,monthDayList:List<String>){
            val cursor =TaskDatabaseHelper.searchDBOfMonthRepeat(database)
            if (cursor!=null) {
                while (cursor.moveToNext()) {
                    val tempDay :String= if ((cursor.getInt(cursor.getColumnIndex("day")).toString().length)==1 )
                        "0${(cursor.getInt(cursor.getColumnIndex("day")).toString())}" else (cursor.getInt(cursor.getColumnIndex("day")).toString())
                    for (a in monthDayList){
                        val split =a.split(".")
                        if (tempDay == split[2]){
                            val tempStr1 ="${split[0]}.${split[1]}.${tempDay}"
                            val tempStr2 = cursor.getString(cursor.getColumnIndex("title"))
                            if (taskMap[tempStr1] ==null){
                                taskMap[tempStr1] = tempStr2
                            }else{
                                taskMap[tempStr1] = "${taskMap[tempStr1]}&${tempStr2}"
                            }
                        }
                    }
                }
            }
        }

    private fun getDayTask(database : SQLiteDatabase,monthDayList:List<String>){
        val cursor =TaskDatabaseHelper.searchDBOfNoRepeat(database)
        if (cursor!=null) {
            while (cursor.moveToNext()) {
                val tempYear :String=cursor.getInt(cursor.getColumnIndex("year")).toString()
                val tempMonth :String= if ((cursor.getInt(cursor.getColumnIndex("month")).toString().length)==1 )
                    "0${(cursor.getInt(cursor.getColumnIndex("month")).toString())}" else (cursor.getInt(cursor.getColumnIndex("month")).toString())
                val tempDay :String= if ((cursor.getInt(cursor.getColumnIndex("day")).toString().length)==1)
                    "0${(cursor.getInt(cursor.getColumnIndex("day")).toString())}" else (cursor.getInt(cursor.getColumnIndex("day")).toString())
                for (a in monthDayList){
                    val split =a.split(".")
                    if (tempYear == split[0] && tempMonth == split[1] && tempDay == split[2]){
                        val tempStr1 ="${split[0]}.${split[1]}.${tempDay}"
                        val tempStr2 = cursor.getString(cursor.getColumnIndex("title"))
                        if (taskMap[tempStr1] ==null){
                            taskMap[tempStr1] = tempStr2
                        }else{
                            taskMap[tempStr1] = "${taskMap[tempStr1]}&${tempStr2}"
                        }
                    }
                }
            }
        }
    }


    private fun getWeekTask(database : SQLiteDatabase,monthDayList: List<String>){
        val cursor =TaskDatabaseHelper.searchDBOfWeekRepeat(database)
        cursor?.let {
            while (it.moveToNext()){
                val temp =it.getString(it.getColumnIndex("week")).split("&")
                val task =it.getString(it.getColumnIndex("title"))
                for (dayList in monthDayList){
                    for (a in temp.indices){
                        if (temp[a]=="1"){
                            when(a) {
                                0-> if (dayList.split(".")[3]=="일")putWeekTaskMap("일",task)//SUN
                                1-> if (dayList.split(".")[3]=="월")putWeekTaskMap("월",task) //MON
                                2-> if (dayList.split(".")[3]=="화")putWeekTaskMap("화",task)//TUE
                                3-> if (dayList.split(".")[3]=="수")putWeekTaskMap("수",task) //WEN
                                4-> if (dayList.split(".")[3]=="목")putWeekTaskMap("목",task)//THU
                                5-> if (dayList.split(".")[3]=="금")putWeekTaskMap("금",task) //FRI
                                6-> if (dayList.split(".")[3]=="토")putWeekTaskMap("토",task)//SAT
                            }
                        }
                    }
                }
            }

        }


    }

    private fun putWeekTaskMap(key:String,value : String){
        if (taskMap[key] ==null){
            taskMap[key] = value
        }else{
            if (!taskMap[key].equals(value)){
                taskMap[key] = "${taskMap[key]}&${value}"
            }
        }
    }




    }
}