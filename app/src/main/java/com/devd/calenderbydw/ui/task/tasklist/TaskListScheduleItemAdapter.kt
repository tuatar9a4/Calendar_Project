package com.devd.calenderbydw.ui.task.tasklist

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil.ItemCallback
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.devd.calenderbydw.R
import com.devd.calenderbydw.data.local.entity.TaskDBEntity
import com.devd.calenderbydw.data.local.entity.TaskDBEntity.Companion.DAILY_REPEAT
import com.devd.calenderbydw.data.local.entity.TaskDBEntity.Companion.MONTH_REPEAT
import com.devd.calenderbydw.data.local.entity.TaskDBEntity.Companion.WEEK_REPEAT
import com.devd.calenderbydw.data.local.entity.TaskDBEntity.Companion.YEAR_REPEAT
import com.devd.calenderbydw.databinding.TaskListScheduleItemBinding

class TaskListScheduleItemAdapter : ListAdapter<TaskDBEntity, TaskListScheduleItemAdapter.TaskListScheduleVH>(diff) {

    private var taskItemClickListener : TaskItemClickLitener? =null

    fun setOnTaskItemClickListener(listener : TaskItemClickLitener){
        taskItemClickListener = listener
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskListScheduleVH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.task_list_schedule_item,parent,false)
        return TaskListScheduleVH(TaskListScheduleItemBinding.bind(view),taskItemClickListener)
    }

    override fun onBindViewHolder(holder: TaskListScheduleVH, position: Int) {
        holder.scheduleBind(currentList[holder.bindingAdapterPosition])
    }

    interface TaskItemClickLitener{
        fun onDeleteTask(id:Int)
        fun onModifyTask(taskItem :TaskDBEntity)
    }

    class TaskListScheduleVH(private val binding : TaskListScheduleItemBinding,private val listener : TaskItemClickLitener?) : ViewHolder(binding.root){
        @SuppressLint("SetTextI18n")
        fun scheduleBind(item : TaskDBEntity){
            binding.clScheduleContainer.clipToOutline = true

            binding.tvTitle.text = "${item.title} 하기!"
            setRepeatColor(item.repeatType)
            item.contents?.let {
                binding.tvContents.text=item.contents
                binding.tvContents.visibility= View.VISIBLE
                binding.tvContentsHolder.visibility= View.VISIBLE
            }?: kotlin.run {
                binding.tvContents.visibility= View.GONE
                binding.tvContentsHolder.visibility= View.GONE
            }

            binding.tvDeleteSchedule.setOnClickListener {
                listener?.onDeleteTask(item.id)
            }
            binding.tvModifySchedule.setOnClickListener {
                listener?.onModifyTask(item)
            }
        }
        private fun setRepeatColor(repeatType:Int){
            when(repeatType){
                DAILY_REPEAT->{
                    binding.repeatColor.backgroundTintList= ColorStateList.valueOf(
                        itemView.context.getColor(R.color.dayTopColor)
                    )
                    binding.tvRepeatType.text="반복 : 매일"
                }
                WEEK_REPEAT->{
                    binding.repeatColor.backgroundTintList= ColorStateList.valueOf(
                        itemView.context.getColor(R.color.weekTopColor)
                    )
                    binding.tvRepeatType.text="반복 : 매주"
                }
                MONTH_REPEAT->{
                    binding.repeatColor.backgroundTintList= ColorStateList.valueOf(
                        itemView.context.getColor(R.color.monthTopColor)
                    )
                    binding.tvRepeatType.text="반복 : 매달"
                }
                YEAR_REPEAT->{
                    binding.repeatColor.backgroundTintList= ColorStateList.valueOf(
                        itemView.context.getColor(R.color.yearTopColor)
                    )
                    binding.tvRepeatType.text="반복 : 매년"
                }
                else->{
                    binding.repeatColor.backgroundTintList= ColorStateList.valueOf(
                        itemView.context.getColor(R.color.schedule_card_bg)
                    )
                    binding.tvRepeatType.text=""
                }
            }
        }
    }

    companion object{
        val diff = object : ItemCallback<TaskDBEntity>(){
            override fun areItemsTheSame(oldItem: TaskDBEntity, newItem: TaskDBEntity): Boolean {
                return oldItem==newItem
            }

            override fun areContentsTheSame(oldItem: TaskDBEntity, newItem: TaskDBEntity): Boolean {
                return oldItem.id == newItem.id &&
                        oldItem.contents == newItem.contents &&
                        oldItem.repeatType == newItem.repeatType &&
                        oldItem.title == newItem.title
            }
        }
    }

}