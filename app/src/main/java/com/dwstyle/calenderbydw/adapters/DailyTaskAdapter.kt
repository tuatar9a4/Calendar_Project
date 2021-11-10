package com.dwstyle.calenderbydw.adapters

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dwstyle.calenderbydw.R
import com.dwstyle.calenderbydw.database.TaskDatabaseHelper
import com.dwstyle.calenderbydw.item.TaskItem
import com.prolificinteractive.materialcalendarview.CalendarDay

class DailyTaskAdapter(context :Context) : RecyclerView.Adapter<DailyTaskAdapter.DailyTaskVH>() {
    private var taskList = ArrayList<TaskItem>()
    private lateinit var calendarDay :CalendarDay
    private val context = context;

    interface OnItemClickListener{
        fun onItemClick(v :View,item :TaskItem,pos :Int)
    }

    var mListener :OnItemClickListener? =null

    fun setOnItemClickListener(listener : OnItemClickListener){
        this.mListener=listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DailyTaskVH {
        val inflater: LayoutInflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view: View = inflater.inflate(R.layout.item_task_card, parent, false)
            return DailyTaskVH(view, mListener)
    }

        override fun onBindViewHolder(holder: DailyTaskVH, position: Int) {
            holder.bind(taskList,context,position)
    }

    override fun getItemCount(): Int = taskList.size


    fun setTaskItem(newTaskItem: ArrayList<TaskItem>,calendarDay: CalendarDay) {
        taskList = deepCloneArrayList(newTaskItem);
        this.calendarDay=calendarDay;
        notifyDataSetChanged()
    }

    private fun deepCloneArrayList(newTaskItem: ArrayList<TaskItem>) : ArrayList<TaskItem> {
            taskList.clear()
            for (tempItem in newTaskItem)
            {
                taskList.add(tempItem)
            }
            return taskList
    }

    fun deleteItemOfList(pos :Int){
        taskList.removeAt(pos)
        notifyDataSetChanged()
    }

    public class DailyTaskVH :RecyclerView.ViewHolder{

        private lateinit var dbHelper : TaskDatabaseHelper
        private lateinit var database : SQLiteDatabase
        private var clickListener : OnItemClickListener? =null
        constructor(itemView: View,mListener : OnItemClickListener?) : super(itemView) {
            this.clickListener=mListener ?: null
        }

        val tvTaskTitle=itemView.findViewById<TextView>(R.id.tvTaskTitle);
        val tvTaskDate=itemView.findViewById<TextView>(R.id.tvTaskDate);
        val tvTaskTime=itemView.findViewById<TextView>(R.id.tvTaskTime);
        val tvTaskRemainder=itemView.findViewById<ImageView>(R.id.tvTaskRemainder);
        val ivDeleteTask = itemView.findViewById<ImageView>(R.id.ivDeleteTask)

        fun bind(taskItem : ArrayList<TaskItem>, context: Context, position: Int){
            dbHelper= TaskDatabaseHelper(context,"task.db",null,1);
            database=dbHelper.readableDatabase
            tvTaskTitle.text = taskItem[position].text
            tvTaskDate.text = "${taskItem[position].month}.${taskItem[position].day}"
            tvTaskTitle.setOnClickListener {
                var c2: Cursor =database.rawQuery("SELECT * FROM myTaskTbl  WHERE _id = ${taskItem[position]._id} ",null)
                Log.d("도원","c2 : "+c2.count);
                while (c2.moveToNext()){
                    Log.d("도원","c2 : "+c2.getInt(0));
                    Log.d("도원","c2 : "+c2.getInt(1));
                    Log.d("도원","c2 : "+c2.getInt(2));
                    Log.d("도원","c2 : "+c2.getInt(3));
                }
            }

            ivDeleteTask.setOnClickListener {
                val pos=bindingAdapterPosition
                if (pos != -1){
                    clickListener?.onItemClick(itemView,taskItem[pos],pos)
                }
            }

//            val pos = bindingAdapterPosition
//            if (pos != -1){
//                itemView.setOnClickListener {
//                    clickListener?.onItemClick(itemView,taskItem[pos],pos)
//
//                }
//            }


        }


    }

}