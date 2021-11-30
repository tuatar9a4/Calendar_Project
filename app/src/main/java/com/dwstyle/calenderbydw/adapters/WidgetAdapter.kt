package com.dwstyle.calenderbydw.adapters

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.dwstyle.calenderbydw.R

class WidgetAdapter : RemoteViewsService(){
    override fun onGetViewFactory(intent: Intent?): RemoteViewsFactory {

        return StackRemoteViewsFactory(this.applicationContext,intent)


    }


    class StackRemoteViewsFactory(private val context:Context,private val intent : Intent?) :
        RemoteViewsService.RemoteViewsFactory {

        private val mCount = 10;
        private val mWidgetItem =ArrayList<String>()


        override fun onCreate() {
            for (i in 0..mCount){
                mWidgetItem.add("${i} + ${i}")
            }

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