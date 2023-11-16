package com.devd.calenderbydw.ui.task.tasklist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil.ItemCallback
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.devd.calenderbydw.R
import com.devd.calenderbydw.data.local.calendar.CalendarData
import com.devd.calenderbydw.data.local.calendar.CalendarDayData
import com.devd.calenderbydw.data.local.calendar.YearMonthDayData
import com.devd.calenderbydw.databinding.TaskListTopDateItemBinding

class TaskListTopDateAdapter : ListAdapter<CalendarDayData, TaskListTopDateAdapter.TaskListTopDateVH>(diff) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskListTopDateVH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.task_list_top_date_item,parent,false)
        return TaskListTopDateVH(TaskListTopDateItemBinding.bind(view))
    }

    override fun onBindViewHolder(holder: TaskListTopDateVH, position: Int) {
        holder.binding(currentList[holder.bindingAdapterPosition])
    }

    class TaskListTopDateVH(private val binding : TaskListTopDateItemBinding) : ViewHolder(binding.root){

        fun binding(item :CalendarDayData){
            binding.tvDay.text = item.day.toString()
        }
    }

    companion object{
        val diff = object :ItemCallback<CalendarDayData>(){
            override fun areItemsTheSame(
                oldItem: CalendarDayData,
                newItem: CalendarDayData
            ): Boolean {
                return oldItem==newItem
            }

            override fun areContentsTheSame(
                oldItem: CalendarDayData,
                newItem: CalendarDayData
            ): Boolean {
                return oldItem.year == newItem.year &&
                        oldItem.month == newItem.month &&
                        oldItem.day == newItem.day
            }
        }
    }

}