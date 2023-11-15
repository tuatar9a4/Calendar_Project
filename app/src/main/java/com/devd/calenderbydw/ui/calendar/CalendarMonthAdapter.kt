package com.devd.calenderbydw.ui.calendar

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil.ItemCallback
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.devd.calenderbydw.R
import com.devd.calenderbydw.data.local.CalendarData
import com.devd.calenderbydw.databinding.CustomCalendarItemBinding

class CalendarMonthAdapter : ListAdapter<CalendarData, CalendarMonthAdapter.CalendarVH>(diff) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarVH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.custom_calendar_item,parent,false)
        return CalendarVH(CustomCalendarItemBinding.bind(view))
    }

    override fun onBindViewHolder(holder: CalendarVH, position: Int) {
        holder.binding(currentList[holder.bindingAdapterPosition])
    }


    inner class CalendarVH(private val binding : CustomCalendarItemBinding) : ViewHolder(binding.root){

        fun binding(item : CalendarData){
            val datAdapter = CalendarDayAdapter()
            binding.rcMainCalendar.layoutManager = GridLayoutManager(itemView.context,7)
            datAdapter.submitList(item.dayList)
            binding.rcMainCalendar.adapter = datAdapter
        }
    }

    companion object{
        val diff = object :ItemCallback<CalendarData>(){
            override fun areItemsTheSame(oldItem: CalendarData, newItem: CalendarData): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: CalendarData, newItem: CalendarData): Boolean {
                return oldItem.month == newItem.month &&
                        oldItem.dayList == newItem.dayList
            }
        }
    }

}