package com.dwstyle.calenderbydw.adapters

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.dwstyle.calenderbydw.R
import org.joda.time.DateTime
import org.joda.time.DateTimeConstants

class WidgetAdapter : RemoteViewsService(){


    override fun onGetViewFactory(intent: Intent?): RemoteViewsFactory {

        return StackRemoteViewsFactory(this.applicationContext,intent)


    }


    class StackRemoteViewsFactory(private val context:Context,private val intent : Intent?) :
        RemoteViewsService.RemoteViewsFactory {


        private val mCount = 49;
        private val mWidgetItem =ArrayList<String>()

        private var monthDayList= listOf<String>()
        private val dateT =DateTime().withDayOfMonth(1).withTimeAtStartOfDay().millis



        override fun onCreate() {
            val weekStr = arrayListOf<String>("Sun","Mon","Tue","Wen","Thu","Fri","Sat")
            for (i in weekStr){
                mWidgetItem.add("${i}")
            }
            monthDayList=getMonthList(DateTime().withDayOfMonth(1))
            for (i in monthDayList){
                val tempDayStr = i.split(".")
                mWidgetItem.add("${tempDayStr[1]}")
            }

        }

        fun getMonthList(dateTime: DateTime): List<String> {
            val list = mutableListOf<String>()

            val date = dateTime.withDayOfMonth(1)
            val prev = getPrevOffSet(date)

            val startValue = date.minusDays(prev)

            val totalDay = DateTimeConstants.DAYS_PER_WEEK * 6

            for (i in 0 until totalDay) {
                list.add(DateTime(startValue.plusDays(i)).toString("MM.dd.E"))
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
                rv.setViewVisibility(R.id.tvTask, View.GONE)
            }else{
                rv.setViewVisibility(R.id.tvTask, View.VISIBLE)
            }
            rv.setTextViewText(R.id.tvDate,mWidgetItem.get(position).toString())
            val extras : Bundle = Bundle()
            extras.putInt("dd",position);
            val fillIntent = Intent()
            fillIntent.putExtras(extras)
            rv.setOnClickFillInIntent(R.id.tvDate,fillIntent)

            return rv
        }

        override fun getLoadingView(): RemoteViews? {
            return null
        }

        override fun getViewTypeCount(): Int {
            return 2
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun hasStableIds(): Boolean {
            return false
        }

    }
}