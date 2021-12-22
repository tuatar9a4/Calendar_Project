package com.dwstyle.calenderbydw.adapters

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.dwstyle.calenderbydw.CalendarWidget
import com.dwstyle.calenderbydw.R
import com.dwstyle.calenderbydw.database.TaskDatabaseHelper
import com.prolificinteractive.materialcalendarview.CalendarDay
import org.joda.time.DateTime
import org.joda.time.DateTimeConstants

class WidgetAdapter : RemoteViewsService(){


    override fun onGetViewFactory(intent: Intent?): RemoteViewsFactory {
        return StackRemoteViewsFactory(this.applicationContext,intent)

    }

//
    class StackRemoteViewsFactory(private val context:Context,private val intent : Intent?) :
        RemoteViewsService.RemoteViewsFactory {


        private val mCount = 49;
        private val mWidgetItem =ArrayList<String>()

        private var monthDayList= listOf<String>()
        private val dateT =DateTime().withDayOfMonth(1).withTimeAtStartOfDay().millis

        private var mAppWidgetId =0

        private lateinit var dbHelper : TaskDatabaseHelper
        private lateinit var database : SQLiteDatabase

        private lateinit var selectMonth : DateTime
        //일정 hashMap
        private val taskMap = HashMap<String,String>()


    override fun onCreate() {
            if (intent != null) {
                mAppWidgetId=intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,AppWidgetManager.INVALID_APPWIDGET_ID)
            }
            val weekStr = arrayListOf<String>("Sun","Mon","Tue","Wen","Thu","Fri","Sat")
            for (i in weekStr){
                mWidgetItem.add("${i}")
            }
            monthDayList=getMonthList(DateTime().withDayOfMonth(1))
            for (i in monthDayList){
                val tempDayStr = i.split(".")
                mWidgetItem.add("${i}")
            }
            getTaskInfo(monthDayList)

        }

        fun getMonthList(dateTime: DateTime): List<String> {
            val list = mutableListOf<String>()

            val date = dateTime.withDayOfMonth(1)
            val prev = getPrevOffSet(date)
            selectMonth=date
            val startValue = date.minusDays(prev)

            val totalDay = DateTimeConstants.DAYS_PER_WEEK * 6

            for (i in 0 until totalDay) {
                list.add(DateTime(startValue.plusDays(i)).toString("YYYY.MM.dd"))
            }

            return list
        }

        private fun getPrevOffSet(dateTime: DateTime): Int {
            var prevMonthTailOffset = dateTime.dayOfWeek

            if (prevMonthTailOffset >= 7) prevMonthTailOffset %= 7

            return prevMonthTailOffset
        }

        override fun onDataSetChanged() {

        }


        override fun onDestroy() {

        }

        override fun getCount(): Int {
            return mCount
        }

        override fun getViewAt(position: Int): RemoteViews {
            val rv=RemoteViews(context.packageName, R.layout.widget_calender_grid_item)
            if (mWidgetItem.get(position).equals("Mon") || mWidgetItem.get(position).equals("Tue") ||mWidgetItem.get(position).equals("Wen")
                ||mWidgetItem.get(position).equals("Thu") || mWidgetItem.get(position).equals("Fri") ||mWidgetItem.get(position).equals("Sat") ||
                    mWidgetItem.get(position).equals("Sun")){
                rv.setViewVisibility(R.id.tvTask1, View.GONE)
                rv.setViewVisibility(R.id.tvTask2, View.GONE)
                rv.setViewVisibility(R.id.tvTaskCnt,View.GONE)
                rv.setTextViewText(R.id.tvDate,mWidgetItem.get(position))
            }else{
                //일단 전부 안보이게 처리
                rv.setViewVisibility(R.id.tvTask1, View.INVISIBLE)
                rv.setViewVisibility(R.id.tvTask2, View.INVISIBLE)
                rv.setViewVisibility(R.id.tvTaskCnt,View.INVISIBLE)
                //날짜는 적어주고
                rv.setTextViewText(R.id.tvDate,mWidgetItem.get(position).split(".")[2])

                //해당 날짜가 taskMap에 들어가 있으면 작성
                if (taskMap.containsKey(mWidgetItem.get(position).toString())){
                    val temp = taskMap[mWidgetItem.get(position)].toString().split("&")
                    if (temp.size==1){
                        rv.setTextViewText(R.id.tvTask1,temp[0].toString())
                        rv.setViewVisibility(R.id.tvTask1, View.VISIBLE)
                    }else if(temp.size==2){
                        rv.setTextViewText(R.id.tvTask1,temp[0].toString())
                        rv.setViewVisibility(R.id.tvTask1, View.VISIBLE)
                        rv.setTextViewText(R.id.tvTask2,temp[1].toString())
                        rv.setViewVisibility(R.id.tvTask2, View.VISIBLE)
                    }else {
                        rv.setTextViewText(R.id.tvTask1,temp[0].toString())
                        rv.setViewVisibility(R.id.tvTask1, View.VISIBLE)
                        rv.setTextViewText(R.id.tvTask2,temp[1].toString())
                        rv.setViewVisibility(R.id.tvTask2, View.VISIBLE)
                        rv.setTextViewText(R.id.tvTaskCnt,"+${(temp.size-2)}")
                        rv.setViewVisibility(R.id.tvTaskCnt, View.VISIBLE)
                    }
//                    rv.setTextViewText(R.id.tvTask1,taskMap[mWidgetItem.get(position)].toString())
                }
            }


            //보내는 intent
            val fillIntent = Intent()
            fillIntent.putExtra(CalendarWidget.COLLECTION_VIEW_EXTRA,mWidgetItem.get(position))
            rv.setOnClickFillInIntent(R.id.calendarContainer,fillIntent)
//            rv.setOnClickPendingIntent(R.id.tvTask,getPenddingSelfIntent(context,"2234",mWidgetItem.get(position).toString()))

            return rv
        }

        override fun getLoadingView(): RemoteViews? {
            return null
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
            dbHelper= TaskDatabaseHelper(context,"task.db",null,2)
            database=dbHelper.readableDatabase
            getYearTask(database,monthDayList)
            getMonthTask(database,monthDayList)
            getDayTask(database,monthDayList)
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
                        if (tempMonth.equals(split[1]) &&
                            tempDay.equals(split[2])){
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
                        if (tempDay.equals(split[2])){
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
                    if (tempYear.equals(split[0]) && tempMonth.equals(split[1]) &&tempDay.equals(split[2])){
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




    }
}