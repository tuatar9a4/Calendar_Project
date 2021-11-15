package com.dwstyle.calenderbydw.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.graphics.Color
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dwstyle.calenderbydw.R
import com.dwstyle.calenderbydw.database.TaskDatabaseHelper
import com.dwstyle.calenderbydw.item.TaskItem
import com.prolificinteractive.materialcalendarview.CalendarDay
import java.text.DateFormat
import java.text.SimpleDateFormat

class DailyTaskAdapter(context :Context) : RecyclerView.Adapter<DailyTaskAdapter.DailyTaskVH>() {
    private var taskList = ArrayList<TaskItem>()
    private lateinit var calendarDay :CalendarDay
    private val context = context;

    val dpToInt= TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,5f,context.resources.displayMetrics)

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

        val layoutInflater =holder.itemView.layoutParams as RecyclerView.LayoutParams

        layoutInflater.topMargin=dpToInt.toInt()
        layoutInflater.bottomMargin = dpToInt.toInt()

        holder.itemView.layoutParams=layoutInflater
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
        val topViewOfTaskKind=itemView.findViewById<View>(R.id.topViewOfTaskKind);

        @SuppressLint("ResourceAsColor")
        fun bind(taskItem : ArrayList<TaskItem>, context: Context, position: Int){
            dbHelper= TaskDatabaseHelper(context,"task.db",null,1);
            database=dbHelper.readableDatabase
            if (taskItem[position].repeatY==1)topViewOfTaskKind.setBackgroundColor(context.getColor(R.color.yearTopColor))
            if (taskItem[position].repeatM==1)topViewOfTaskKind.setBackgroundColor(context.getColor(R.color.monthTopColor))
            if (taskItem[position].repeatW==1)topViewOfTaskKind.setBackgroundColor(context.getColor(R.color.weekTopColor))
            if (taskItem[position].repeatN==1)topViewOfTaskKind.setBackgroundColor(context.getColor(R.color.dayTopColor))

            tvTaskTitle.text = taskItem[position].text
            tvTaskDate.text = "Date : ${taskItem[position].month}.${taskItem[position].day}"

            val df : DateFormat =SimpleDateFormat("HH:mm")
            val str=df.format(taskItem[position].time)
            tvTaskTime.text ="Time : ${str}"

            //삭제하기
            ivDeleteTask.setOnClickListener {
                val pos=bindingAdapterPosition
                if (pos != -1){
                    clickListener?.onItemClick(itemView,taskItem[pos],pos)
                }
            }




        }


    }

}