package com.devd.calenderbydw.ui.calendar

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil.ItemCallback
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.devd.calenderbydw.R
import com.devd.calenderbydw.data.local.calendar.CalendarDayData
import com.devd.calenderbydw.databinding.CustomCalendarDayItemBinding
import com.devd.calenderbydw.utils.ConstVariable.WEEK_SAT_DAY
import com.devd.calenderbydw.utils.ConstVariable.WEEK_SUN_DAY
import com.devd.calenderbydw.utils.getDeviceSize
import com.devd.calenderbydw.utils.getDpValue
import timber.log.Timber

class CalendarDayAdapter(
    private val listener : CalendarMonthAdapter.CalendarClickListener?=null
) : ListAdapter<CalendarDayData, CalendarDayAdapter.CalendarDayVH>(diff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarDayVH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.custom_calendar_day_item, parent, false)
        val datContainerWidth = ((parent.context.getDeviceSize().width / 7) - (parent.context.getDpValue(20f)/7).toInt())
        val imageSize = (datContainerWidth / 3) - parent.context.getDpValue(3f).toInt()
        return CalendarDayVH(CustomCalendarDayItemBinding.bind(view), datContainerWidth, imageSize,listener)
    }

    override fun onBindViewHolder(holder: CalendarDayVH, position: Int) {
        holder.dayBind(currentList[holder.bindingAdapterPosition])
    }

    class CalendarDayVH(
        private val binding: CustomCalendarDayItemBinding,
        private val deviceWidth: Int,
        private val imageSize: Int,
        private val listener : CalendarMonthAdapter.CalendarClickListener?
    ) : ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun dayBind(item: CalendarDayData) {
            setContainerSize()
            setTodayBackground(item.toDay)
            setDateTextView(
                item.isCurrentMonth,
                item.day,
                item.isHoliday,
                item.holidayName,
                item.weekCount
            )
            Glide.with(itemView.context)
                .load(R.drawable.test_icon)
                .into(binding.ivSticker3)
            setOnclickListener(item)
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

        private fun setTodayBackground(today:Boolean){
            if(today){
                binding.clDayContainer.backgroundTintList = ColorStateList.valueOf(itemView.context.getColor(R.color.palette_light_green_opacity_50))
            }else{
                binding.clDayContainer.backgroundTintList = null
            }
//            if(selectPos == bindingAdapterPosition){
//                binding.tvDate.background = itemView.context.getDrawable(R.drawable.main_home_btn_img)
//            }else{
//                binding.tvDate.background = null
//            }
        }

        private fun setDateTextView(isCurrentMonth :Boolean,day:String,isHoliday:Boolean,holidayName:String?,weekCount:Int){
            if(!isCurrentMonth){
                binding.tvDate.text = day
                binding.tvDate.setTextColor(itemView.context.getColor(R.color.grayDayColor))
            }else if(isHoliday){
                binding.tvDate.text = day
                binding.tvHoliday.text = "[${holidayName}]"
                binding.tvHoliday.visibility= View.VISIBLE
                binding.tvDate.setTextColor(itemView.context.getColor(R.color.sunDayColor))
            }else if(weekCount == WEEK_SUN_DAY){
                binding.tvDate.text = day
                binding.tvDate.setTextColor(itemView.context.getColor(R.color.sunDayColor))
            }else if(weekCount == WEEK_SAT_DAY){
                binding.tvDate.text = day
                binding.tvDate.setTextColor(itemView.context.getColor(R.color.satDayColor))
            }else{
                binding.tvDate.text = day
                binding.tvDate.setTextColor(itemView.context.getColor(R.color.commonDayColor))
            }
        }

        private fun setOnclickListener(item :CalendarDayData){
            binding.root.setOnClickListener {
                listener?.onDayClick(item.year.toInt(),item.month.toInt(),item.day.toInt())
//                val adapter =bindingAdapter
//                if(adapter is CalendarDayAdapter){
//                    if(adapter.selectPos == bindingAdapterPosition){
//                        adapter.selectPos = -1
//                    }else{
//                        val originPos = adapter.selectPos
//                        adapter.selectPos = bindingAdapterPosition
//                        adapter.notifyItemChanged(originPos)
//                    }
//                    adapter.notifyItemChanged(bindingAdapterPosition)
//                }
            }
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
                        oldItem.holidayName == newItem.holidayName &&
                        oldItem.toDay == newItem.toDay &&
                        oldItem.isCurrentMonth == newItem.isCurrentMonth
            }
        }
    }


}