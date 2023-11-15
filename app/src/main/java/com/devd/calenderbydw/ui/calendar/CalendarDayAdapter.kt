package com.devd.calenderbydw.ui.calendar

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil.ItemCallback
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.devd.calenderbydw.R
import com.devd.calenderbydw.data.local.CalendarDayData
import com.devd.calenderbydw.databinding.CustomCalendarDayItemBinding
import com.devd.calenderbydw.utils.ConstVariable.WEEK_SAT_DAY
import com.devd.calenderbydw.utils.ConstVariable.WEEK_SUN_DAY
import com.devd.calenderbydw.utils.getDeviceSize
import com.devd.calenderbydw.utils.getDpValue
import timber.log.Timber

class CalendarDayAdapter : ListAdapter<CalendarDayData, CalendarDayAdapter.CalendarDayVH>(diff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarDayVH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.custom_calendar_day_item, parent, false)
        val datContainerWidth = ((parent.context.getDeviceSize().width / 7) - (parent.context.getDpValue(20f)/7).toInt())
        val imageSize = (datContainerWidth / 3) - parent.context.getDpValue(3f).toInt()
        return CalendarDayVH(CustomCalendarDayItemBinding.bind(view), datContainerWidth, imageSize)
    }

    override fun onBindViewHolder(holder: CalendarDayVH, position: Int) {
        holder.dayBind(currentList[holder.bindingAdapterPosition])
    }

    inner class CalendarDayVH(
        private val binding: CustomCalendarDayItemBinding,
        private val deviceWidth: Int,
        private val imageSize: Int
    ) : ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun dayBind(item: CalendarDayData) {
            setContainerSize()
            Glide.with(itemView.context)
                .load(R.drawable.test_icon)
                .into(binding.ivSticker3)
            if(!item.isCurrentMonth){
                binding.tvDate.text = item.day
                binding.tvDate.setTextColor(itemView.context.getColor(R.color.grayDayColor))
            }else if(item.isHoliday){
                binding.tvDate.text = item.day
                binding.tvHoliday.text = "[${item.holidayName}]"
                binding.tvHoliday.visibility= View.VISIBLE
                binding.tvDate.setTextColor(itemView.context.getColor(R.color.sunDayColor))
            }else if(item.weekCount == WEEK_SUN_DAY){
                binding.tvDate.text = item.day
                binding.tvDate.setTextColor(itemView.context.getColor(R.color.sunDayColor))
            }else if(item.weekCount == WEEK_SAT_DAY){
                binding.tvDate.text = item.day
                binding.tvDate.setTextColor(itemView.context.getColor(R.color.satDayColor))
            }else{
                binding.tvDate.text = item.day
                binding.tvDate.setTextColor(itemView.context.getColor(R.color.commonDayColor))
            }
        }

        private fun setContainerSize(){
            binding.clDayContainer.clipToOutline = false
            binding.clDayContainer.clipToPadding = false
            binding.clDayContainer.clipChildren = false
            binding.clDayContainer.layoutParams.width = deviceWidth
            binding.ivSticker1.layoutParams.width = imageSize
            binding.ivSticker1.layoutParams.height = imageSize
            binding.ivSticker2.layoutParams.width = imageSize
            binding.ivSticker2.layoutParams.height = imageSize
            binding.ivSticker3.layoutParams.width = imageSize
            binding.ivSticker3.layoutParams.height = imageSize
            binding.tvHoliday.visibility= View.GONE
        }
    }

    companion object {
        val diff = object : ItemCallback<CalendarDayData>() {
            override fun areItemsTheSame(
                oldItem: CalendarDayData,
                newItem: CalendarDayData
            ): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: CalendarDayData,
                newItem: CalendarDayData
            ): Boolean {
                return oldItem.day == newItem.day &&
                        oldItem.isHoliday == newItem.isHoliday &&
                        oldItem.holidayName == newItem.holidayName
            }
        }
    }


}