package com.devd.calenderbydw.ui.calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import com.devd.calenderbydw.R
import com.devd.calenderbydw.databinding.FragmentCalendarBinding
import com.devd.calenderbydw.utils.EventObserver
import com.devd.calenderbydw.utils.HorizontalMarginItemDecoration
import com.devd.calenderbydw.utils.SnapPagerScrollListener
import com.devd.calenderbydw.utils.addSingleItemDecoRation
import com.devd.calenderbydw.utils.autoCleared
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class CalendarFragment : Fragment() {

    private var binding by autoCleared<FragmentCalendarBinding>()
    private val viewModel: CalendarViewModel by viewModels()

    private val calendarAdapter = CalendarMonthAdapter()
    private lateinit var horizontalMarginItemDecoration: HorizontalMarginItemDecoration
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        horizontalMarginItemDecoration = HorizontalMarginItemDecoration(10f, 10f, requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCalendarBinding.inflate(inflater, container, false)
        setRecyclerView()
        setObserver()
        viewModel.getHolidayYear(
            getString(R.string.holidayEncodingKey)
        )
        return binding.root
    }

    private fun setObserver() {
        viewModel.calendarLiveData.observe(viewLifecycleOwner, EventObserver { calendList ->
            calendarAdapter.submitList(calendList) {
                binding.rcCustomCalendar.scrollToPosition(((calendarAdapter.itemCount - 1) / 2))
            }
        })
    }

    private fun setRecyclerView() {
        binding.rcCustomCalendar.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.rcCustomCalendar.addSingleItemDecoRation(horizontalMarginItemDecoration)
        binding.rcCustomCalendar.adapter = calendarAdapter

        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(binding.rcCustomCalendar)

        binding.rcCustomCalendar.addOnScrollListener(SnapPagerScrollListener(
            snapHelper,
            SnapPagerScrollListener.ON_SCROLL,
            true,
            object : SnapPagerScrollListener.OnChangeListener {
                override fun onSnapped(position: Int, isRightScroll: Boolean) {
                    Timber.d("UpdateHoliday onSnapped position : $position | isRightScroll $isRightScroll ")
                    val currentItem = calendarAdapter.currentList[position]
                    binding.tvCurrentMonth.text = "${currentItem.year}.${currentItem.month}"
                    if (calendarAdapter.currentList.size > position + 2 && isRightScroll) {
                        val twoStepItem = calendarAdapter.currentList[position + 2]
                        updateHolidayInCalendar(currentItem.year,twoStepItem.year)
                    }else if(!isRightScroll && position - 2 > 0){
                        val twoStepItem = calendarAdapter.currentList[position -2]
                        updateHolidayInCalendar(currentItem.year,twoStepItem.year)
                    }
                }
            }
        ))
    }


    private fun updateHolidayInCalendar(originYear :Int, newYear :Int){
        if (originYear != newYear) {
            viewModel.updateYearDb(
                getString(R.string.holidayEncodingKey),
                newYear
            )
        }
    }
}