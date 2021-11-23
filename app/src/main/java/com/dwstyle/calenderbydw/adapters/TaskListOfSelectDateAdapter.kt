package com.dwstyle.calenderbydw.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dwstyle.calenderbydw.R
import com.dwstyle.calenderbydw.item.TaskItem
import java.text.SimpleDateFormat

class TaskListOfSelectDateAdapter(private val context:Context) : RecyclerView.Adapter<TaskListOfSelectDateAdapter.TaskListVH>() {


    private var selectTaskList :ArrayList<TaskItem> =ArrayList<TaskItem>()



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskListVH {
        val inflater: LayoutInflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view :View = inflater.inflate(R.layout.task_list_item_layout,parent,false)

        return TaskListVH(view,mListener)
    }

    override fun onBindViewHolder(holder: TaskListVH, position: Int) {

        holder.bind(selectTaskList[position],position,context)

    }

    override fun getItemCount(): Int =selectTaskList.size

    fun setTaskItem(taskList : ArrayList<TaskItem>){
        this.selectTaskList=taskList
        Log.d("도원","setSize : ${selectTaskList.size}")
        notifyDataSetChanged()
    }

    var mListener : OnDeleteClickListener? =null

    interface OnDeleteClickListener{
        fun OnDeleteClick(v :View,item: TaskItem,pos: Int)
    }

    fun setOnDeleteItemClickListener(listener : OnDeleteClickListener){
        this.mListener=listener

    }

    class TaskListVH: RecyclerView.ViewHolder{
        val viewTaskType:View =itemView.findViewById(R.id.viewTaskType);
        val tvTitle:TextView  =itemView.findViewById(R.id.tvTitle)
        val tvContents:TextView =itemView.findViewById(R.id.tvContents)
        val tvCreateDate:TextView=itemView.findViewById(R.id.tvCreateDate);
        val tvTaskTime:TextView =itemView.findViewById(R.id.tvTaskTime)
        val ivDeleteTask:ImageView = itemView.findViewById(R.id.ivDeleteTask)

        private var mListener :OnDeleteClickListener? =null
        constructor(itemView: View,mListener : OnDeleteClickListener?)  : super(itemView) {
            this.mListener=mListener ?: null
        }

        fun bind(taskItem: TaskItem,pos :Int,context: Context){
            tvTitle.text=taskItem.title
            tvContents.text=taskItem.text
            tvCreateDate.text="${taskItem.year}.${taskItem.month}.${taskItem.day}"
            tvTaskTime.text = SimpleDateFormat("HH:mm").format(taskItem.time)

            if (taskItem.repeatY==1)viewTaskType.setBackgroundColor(context.getColor(R.color.yearTopColor))
            if (taskItem.repeatM==1)viewTaskType.setBackgroundColor(context.getColor(R.color.monthTopColor))
            if (taskItem.repeatW==1)viewTaskType.setBackgroundColor(context.getColor(R.color.weekTopColor))
            if (taskItem.repeatN==1)viewTaskType.setBackgroundColor(context.getColor(R.color.dayTopColor))

            ivDeleteTask.setOnClickListener {
                val pos=bindingAdapterPosition
                if (pos !=-1){
                    mListener?.OnDeleteClick(itemView,taskItem,pos)
                }
            }

        }

    }
}
