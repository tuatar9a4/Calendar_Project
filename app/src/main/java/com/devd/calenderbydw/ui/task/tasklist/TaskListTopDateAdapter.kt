package com.devd.calenderbydw.ui.task.tasklist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil.ItemCallback
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.devd.calenderbydw.R
import com.devd.calenderbydw.data.local.entity.CalendarDayEntity
import com.devd.calenderbydw.data.local.entity.TaskDBEntity
import com.devd.calenderbydw.data.local.entity.TaskDBEntity.Companion.DAILY_REPEAT
import com.devd.calenderbydw.data.local.entity.TaskDBEntity.Companion.MONTH_REPEAT
import com.devd.calenderbydw.data.local.entity.TaskDBEntity.Companion.WEEK_REPEAT
import com.devd.calenderbydw.data.local.entity.TaskDBEntity.Companion.YEAR_REPEAT
import com.devd.calenderbydw.databinding.TaskListTopDateItemBinding
import com.devd.calenderbydw.utils.changeWeekIntToString
import timber.log.Timber

class TaskListTopDateAdapter : ListAdapter<CalendarDayEntity, TaskListTopDateAdapter.TaskListTopDateVH>(diff) {
    private var selectPos :Int = 0
    private var topDateClickListener : OnTopDateClickListener? =null
    private var taskDBEntity : List<TaskDBEntity>? = null
    fun setOnTopDateClickListener(listener: OnTopDateClickListener){
        topDateClickListener = listener
    }
    fun setTaskDBEntity(item :List<TaskDBEntity>){
        taskDBEntity = item
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskListTopDateVH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.task_list_top_date_item,parent,false)
        return TaskListTopDateVH(TaskListTopDateItemBinding.bind(view),topDateClickListener)
    }

    override fun onBindViewHolder(holder: TaskListTopDateVH, position: Int) {
        holder.binding(currentList[holder.bindingAdapterPosition],taskDBEntity,selectPos)
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

        fun binding(item :CalendarDayEntity,taskDBEntity: List<TaskDBEntity>?,pos:Int){
            binding.tvDay.text = item.day
            binding.tvWeek.text = item.weekCount.changeWeekIntToString()
            if(pos == bindingAdapterPosition){
                binding.ivCurrentIcon.visibility= View.VISIBLE
            }else{
                binding.ivCurrentIcon.visibility= View.GONE
            }

            taskDBEntity?.let {
                val exsist = it.any { task ->
                    if (item.dateTimeLong < task.createDate) {
                        false
                    } else {
                        when (task.repeatType) {
                            DAILY_REPEAT -> {
                                true
                            }

                            WEEK_REPEAT -> {
                                item.weekCount == task.weekCount
                            }

                            MONTH_REPEAT -> {
                                item.day == task.day
                            }

                            YEAR_REPEAT -> {
                                item.month == task.month && item.day == task.day
                            }

                            else -> {
                                item.year == task.year && item.month == task.month && item.day == task.day
                            }
                        }
                    }
                }
                if(exsist){
                    binding.ivTaskIcon.visibility=View.VISIBLE
                }else{
                    binding.ivTaskIcon.visibility = View.INVISIBLE
                }
            } ?: kotlin.run {
                binding.ivTaskIcon.visibility = View.INVISIBLE
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
        val diff = object :ItemCallback<CalendarDayEntity>(){
            override fun areItemsTheSame(
                oldItem: CalendarDayEntity,
                newItem: CalendarDayEntity
            ): Boolean {
                return oldItem==newItem
            }

            override fun areContentsTheSame(
                oldItem: CalendarDayEntity,
                newItem: CalendarDayEntity
            ): Boolean {
                return oldItem.year == newItem.year &&
                        oldItem.month == newItem.month &&
                        oldItem.day == newItem.day
            }
        }
    }

}