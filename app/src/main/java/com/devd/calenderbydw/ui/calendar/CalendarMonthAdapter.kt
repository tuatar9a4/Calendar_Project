package com.devd.calenderbydw.ui.calendar

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil.ItemCallback
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.recyclerview.widget.SimpleItemAnimator
import com.devd.calenderbydw.R
import com.devd.calenderbydw.data.local.calendar.CalendarData
import com.devd.calenderbydw.data.local.calendar.YearMonthDayData
import com.devd.calenderbydw.data.local.entity.CalendarMonthEntity
import com.devd.calenderbydw.databinding.CustomCalendarItemBinding
import timber.log.Timber

class CalendarMonthAdapter : ListAdapter<CalendarMonthEntity, CalendarMonthAdapter.CalendarVH>(diff) {

    private var calendarClickListener: CalendarClickListener? = null

    fun setOnCalendarClickListener(listener: CalendarClickListener) {
        calendarClickListener = listener
    }

    interface CalendarClickListener {
        fun onMonthClick()
        fun onDayClick(year :Int,month:Int,day :Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarVH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.custom_calendar_item, parent, false)
        return CalendarVH(CustomCalendarItemBinding.bind(view), calendarClickListener)
    }

    override fun onBindViewHolder(holder: CalendarVH, position: Int) {
        holder.binding(currentList[holder.bindingAdapterPosition])
    }

    class CalendarVH(
        private val binding: CustomCalendarItemBinding,
        private val listener: CalendarClickListener?
    ) : ViewHolder(binding.root) {
        private val datAdapter = CalendarDayAdapter(listener)
        fun binding(item: CalendarMonthEntity) {
            binding.calendarContainer.initCalendar(
                item.dayList,
                listener
            )
//            binding.rcMainCalendar.layoutManager = GridLayoutManager(itemView.context, 7)
//            binding.rcMainCalendar.itemAnimator = null
//            datAdapter.submitList(item.dayList)
//            binding.rcMainCalendar.adapter = datAdapter
        }

    }

    companion object {
        val diff = object : ItemCallback<CalendarMonthEntity>() {
            override fun areItemsTheSame(oldItem: CalendarMonthEntity, newItem: CalendarMonthEntity): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: CalendarMonthEntity, newItem: CalendarMonthEntity): Boolean {
                return oldItem.month == newItem.month &&
                        oldItem.dayList == newItem.dayList
            }
        }
    }

}