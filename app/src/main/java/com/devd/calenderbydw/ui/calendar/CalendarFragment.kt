package com.devd.calenderbydw.ui.calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
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

    private lateinit var horizontalMarginItemDecoration: HorizontalMarginItemDecoration

    override fun onResume() {
        super.onResume()
        viewModel.checkToday()
    }

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
        if(viewModel.calendarAdapter.itemCount==0){
            viewModel.getHolidayYear(
                getString(R.string.holidayEncodingKey),
                true,
                2023
            )
        }
        return binding.root
    }

    private fun setObserver() {
        viewModel.updateCalendarData.observe(viewLifecycleOwner, EventObserver { result ->
            if (viewModel.firstUpdate) {
                viewModel.firstUpdate = !viewModel.firstUpdate
                binding.rcCustomCalendar.scrollToPosition(
                    viewModel.calendarAdapter.currentList.indexOfFirst {
                        it.year == viewModel.currentToday.year &&
                            it.month == viewModel.currentToday.month
                    }
                )
            }
        })
    }

    private fun setRecyclerView() {
        binding.rcCustomCalendar.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.rcCustomCalendar.addSingleItemDecoRation(horizontalMarginItemDecoration)
        binding.rcCustomCalendar.adapter = viewModel.calendarAdapter

        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(binding.rcCustomCalendar)

        binding.rcCustomCalendar.addOnScrollListener(SnapPagerScrollListener(
            snapHelper,
            SnapPagerScrollListener.ON_SCROLL,
            true,
            object : SnapPagerScrollListener.OnChangeListener {
                override fun onSnapped(position: Int, isRightScroll: Boolean) {
//                    Timber.d("UpdateHoliday onSnapped position : $position | isRightScroll $isRightScroll ")
                    val currentItem = viewModel.getAdapterCurrentList()[position]
                    binding.tvCurrentMonth.text = "${currentItem.year}.${currentItem.month}"
                    if (viewModel.getAdapterCurrentList().size > position + 3 && isRightScroll) {
                        viewModel.getHolidayYear(
                            getString(R.string.holidayEncodingKey),
                            true,
                            viewModel.calendarAdapter.currentList[position+3].year+1
                        )
                    } else if (!isRightScroll && position - 3 > 0) {
                        viewModel.getHolidayYear(
                            getString(R.string.holidayEncodingKey),
                            false,
                            viewModel.calendarAdapter.currentList[position-3].year-1
                        )
                    }
                    if (viewModel.getAdapterCurrentList().size > position + 1 && isRightScroll) {
                        val twoStepItem = viewModel.getAdapterCurrentList()[position + 1]
                        updateHolidayInCalendar(currentItem.year, twoStepItem.year)
                    } else if (!isRightScroll && position - 1 > 0) {
                        val twoStepItem = viewModel.getAdapterCurrentList()[position - 1]
                        updateHolidayInCalendar(currentItem.year, twoStepItem.year)
                    }
                }
            }
        ))

        viewModel.calendarAdapter.setOnCalendarClickListener(object :
            CalendarMonthAdapter.CalendarClickListener {
            override fun onMonthClick() {
            }

            override fun onDayClick(year: Int, month: Int, day: Int) {
                Timber.d("onDayClick onDayClickonDayClick")
                findNavController().navigate(
                    R.id.action_calendarFragment_to_taskListFragment,
                    bundleOf(
                        "year" to year,
                        "month" to month,
                        "day" to day
                    )
                )
//                if(binding.tvSelectedDate.text == "${year}.${month}.${day}"){
//                    binding.tvSelectedDate.text=""
//                    binding.rlTaskContainer.visibility=View.GONE
//                }else{
//                    binding.tvSelectedDate.text="${year}.${month}.${day}"
//                    binding.rlTaskContainer.visibility=View.VISIBLE
//                }
            }
        })
    }


    private fun updateHolidayInCalendar(originYear: Int, newYear: Int) {
        if (originYear != newYear) {
            viewModel.updateYearDb(
                getString(R.string.holidayEncodingKey),
                newYear
            )
        }
    }
}