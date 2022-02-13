package com.devd.calenderbydw.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.devd.calenderbydw.R
import com.devd.calenderbydw.database.TaskDatabaseHelper
import com.devd.calenderbydw.item.TaskItem
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
        fun onTaskClick(v :View,item :TaskItem,pos : Int)
    }

    var mListener :OnItemClickListener? =null

    fun setOnDeleteItemClickListener(listener : OnItemClickListener){
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

    class DailyTaskVH(itemView: View, mListener: OnItemClickListener?) :
        RecyclerView.ViewHolder(itemView) {

        private lateinit var dbHelper : TaskDatabaseHelper
        private lateinit var database : SQLiteDatabase
        private var clickListener : OnItemClickListener? =null

        val tvTaskTitle=itemView.findViewById<TextView>(R.id.tvTaskTitle);
        val tvTaskDate=itemView.findViewById<TextView>(R.id.tvTaskDate);
        val tvTaskTime=itemView.findViewById<TextView>(R.id.tvTaskTime);
        val tvTaskRemainder=itemView.findViewById<ImageView>(R.id.tvTaskRemainder);
        val ivDeleteTask = itemView.findViewById<ImageView>(R.id.ivDeleteTask)
        val topViewOfTaskKind=itemView.findViewById<View>(R.id.topViewOfTaskKind);
        val tvIsContents = itemView.findViewById<TextView>(R.id.tvIsContents)
        private val infoLayout=itemView.findViewById<LinearLayout>(R.id.infoLayout)
        private val rlTaskBox=itemView.findViewById<RelativeLayout>(R.id.rlTaskBox)

        @SuppressLint("ResourceAsColor")
        fun bind(taskItem : ArrayList<TaskItem>, context: Context, position: Int){
            infoLayout.visibility=View.GONE
            rlTaskBox.clipToOutline=true
            dbHelper= TaskDatabaseHelper(context,"task.db",null,3);
            database=dbHelper.readableDatabase
            if (taskItem[position].repeatY==1)topViewOfTaskKind.setBackgroundColor(context.getColor(R.color.yearTopColor))
            if (taskItem[position].repeatM==1)topViewOfTaskKind.setBackgroundColor(context.getColor(R.color.monthTopColor))
            if (taskItem[position].repeatW==1)topViewOfTaskKind.setBackgroundColor(context.getColor(R.color.weekTopColor))
            if (taskItem[position].repeatN==1)topViewOfTaskKind.setBackgroundColor(context.getColor(R.color.dayTopColor))

            tvTaskTitle.text = taskItem[position].title
            tvIsContents.text =
                if (taskItem[position].text.toString().equals("내용 없음"))"내용 없음"
                else "${taskItem[position].text}"
            tvTaskDate.text = "Create Date : ${taskItem[position].month}.${taskItem[position].day}"

            val df : DateFormat =SimpleDateFormat("HH:mm")
            val str=df.format(taskItem[position].time)
            tvTaskTime.text ="Time : ${str}"

            itemView.setOnClickListener(View.OnClickListener {
                val pos=bindingAdapterPosition
                if (pos != -1){
                    clickListener?.onTaskClick(itemView,taskItem[pos],pos)
                }
            })

            //삭제하기
            ivDeleteTask.setOnClickListener {
                val pos=bindingAdapterPosition
                if (pos != -1){
                    clickListener?.onItemClick(itemView,taskItem[pos],pos)
                }
            }




        }

        init {
            this.clickListener=mListener ?: null
        }


    }

}