package com.dwstyle.calenderbydw.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dwstyle.calenderbydw.R
import com.dwstyle.calenderbydw.item.TaskItem

class DailyTaskAdapter(context :Context) : RecyclerView.Adapter<DailyTaskAdapter.DailyTaskVH>() {
    private var taskList = ArrayList<TaskItem>()
    private val context = context;

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DailyTaskVH {
        val inflater: LayoutInflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view: View = inflater.inflate(R.layout.item_task_card, parent, false)
            return DailyTaskVH(view)
        }

        override fun onBindViewHolder(holder: DailyTaskVH, position: Int) {
            holder.tvTaskTitle.text = taskList[position].text
            holder.tvTaskDate.text = "${taskList[position].month}.${taskList[position].day}"

    }

    override fun getItemCount(): Int = taskList.size

    fun setTaskItem(newTaskItem: ArrayList<TaskItem>) {
        taskList = deepCloneArrayList(newTaskItem);
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


    companion object{

    }

    public class DailyTaskVH(itemView: View) :RecyclerView.ViewHolder(itemView){
        val tvTaskTitle=itemView.findViewById<TextView>(R.id.tvTaskTitle);
        val tvTaskDate=itemView.findViewById<TextView>(R.id.tvTaskDate);
        val tvTaskTime=itemView.findViewById<TextView>(R.id.tvTaskTime);
        val tvTaskRemainder=itemView.findViewById<ImageView>(R.id.tvTaskRemainder);




    }

}