package com.dwstyle.calenderbydw.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
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

        return TaskListVH(view,mListener,mListener2)
    }

    override fun onBindViewHolder(holder: TaskListVH, position: Int) {

        holder.bind(selectTaskList[position],position,context)

    }

    override fun getItemCount(): Int =selectTaskList.size

    fun setTaskItem(taskList : ArrayList<TaskItem>){
        this.selectTaskList=taskList
        notifyDataSetChanged()
    }

    var mListener : OnDeleteClickListener? =null

    interface OnDeleteClickListener{
        fun OnDeleteClick(v :View,item: TaskItem,pos: Int)
    }

    var mListener2: OnChangeClickListener? =null

    interface OnChangeClickListener{
        fun OnChangeClick(v: View, item : TaskItem , pos : Int)
    }

    fun setOnDeleteItemClickListener(listener : OnDeleteClickListener,listener2 : OnChangeClickListener){
        this.mListener=listener
        this.mListener2=listener2

    }

    class TaskListVH: RecyclerView.ViewHolder{
        private val viewTaskType:View =itemView.findViewById(R.id.viewTaskType);
        private val tvTitle:TextView  =itemView.findViewById(R.id.tvTitle)
        private val tvContents:TextView =itemView.findViewById(R.id.tvContents)
        private val tvCreateDate:TextView=itemView.findViewById(R.id.tvCreateDate);
        private val tvTaskTime:TextView =itemView.findViewById(R.id.tvTaskTime)
        private val ivDeleteTask:ImageView = itemView.findViewById(R.id.ivDeleteTask)
        private val rlTaskLayout:RelativeLayout =itemView.findViewById(R.id.rlTaskLayout)
        private val ivChangeTask =itemView.findViewById<ImageView>(R.id.ivChangeTask)

        private var mListener :OnDeleteClickListener? =null
        private var mListener2 :OnChangeClickListener? =null
        constructor(itemView: View,mListener : OnDeleteClickListener?,mListener2 : OnChangeClickListener?)  : super(itemView) {
            this.mListener=mListener ?: null
            this.mListener2=mListener2 ?: null
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

            ivChangeTask.setOnClickListener {
                val pos=bindingAdapterPosition
                if (pos != -1){
                    mListener2?.OnChangeClick(itemView,taskItem,pos)
                }

            }

            val linearLayout = rlTaskLayout.apply {
                measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
                clipToOutline= true
            }

        }

    }
}
