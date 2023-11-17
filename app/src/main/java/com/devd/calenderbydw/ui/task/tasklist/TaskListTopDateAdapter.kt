package com.devd.calenderbydw.ui.task.tasklist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil.ItemCallback
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.devd.calenderbydw.R
import com.devd.calenderbydw.data.local.calendar.CalendarDayData
import com.devd.calenderbydw.databinding.TaskListTopDateItemBinding
import com.devd.calenderbydw.utils.changeWeekIntToString

class TaskListTopDateAdapter : ListAdapter<CalendarDayData, TaskListTopDateAdapter.TaskListTopDateVH>(diff) {
    private var selectPos :Int = 0
    private var topDateClickListener : OnTopDateClickListener? =null

    fun setOnTopDateClickListener(listener: OnTopDateClickListener){
        topDateClickListener = listener
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskListTopDateVH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.task_list_top_date_item,parent,false)
        return TaskListTopDateVH(TaskListTopDateItemBinding.bind(view),topDateClickListener)
    }

    override fun onBindViewHolder(holder: TaskListTopDateVH, position: Int) {
        holder.binding(currentList[holder.bindingAdapterPosition],selectPos)
    }

    fun setSelectPos(newPos :Int){
        val oldPos = selectPos
        selectPos = newPos
        notifyItemChanged(oldPos)
        notifyItemChanged(newPos)
    }

    interface OnTopDateClickListener{
        fun onDateClick(pos:Int,year :String,month:String, day:String)
    }

    class TaskListTopDateVH(private val binding : TaskListTopDateItemBinding,private val listener : OnTopDateClickListener?) : ViewHolder(binding.root){

        fun binding(item :CalendarDayData,pos:Int){
            binding.tvDay.text = item.day
            binding.tvWeek.text = item.weekCount.changeWeekIntToString()
            if(pos == bindingAdapterPosition){
                binding.ivCurrentIcon.visibility= View.VISIBLE
            }else{
                binding.ivCurrentIcon.visibility= View.GONE
            }
            binding.root.setOnClickListener {
                listener?.onDateClick(
                    bindingAdapterPosition,
                    item.year,
                    item.month,
                    item.day
                )
            }
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