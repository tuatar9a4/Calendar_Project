package com.devd.calenderbydw.ui.calendar

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil.ItemCallback
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.devd.calenderbydw.R
import com.devd.calenderbydw.data.local.CalendarDayData
import com.devd.calenderbydw.databinding.CustomCalendarDayItemBinding
import com.devd.calenderbydw.utils.getDeviceSize
import com.devd.calenderbydw.utils.getDpValue

class CalendarDayAdapter : ListAdapter<CalendarDayData, CalendarDayAdapter.CalendarDayVH>(diff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarDayVH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.custom_calendar_day_item, parent, false)
        val datContainerWidth = (parent.context.getDeviceSize().width / 7 - 5)
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
        fun dayBind(item: CalendarDayData) {
            setContainerSize()

            Glide.with(itemView.context)
                .load(R.drawable.test_icon)
                .into(binding.ivSticker3)

            binding.tvDate.text = item.day
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