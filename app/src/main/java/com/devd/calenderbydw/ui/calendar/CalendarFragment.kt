package com.devd.calenderbydw.ui.calendar

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Calendar
import java.util.Date

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
        val toDayCalendar = Calendar.getInstance().apply {
            time = Date()
        }
        viewModel.setMonthTaskList(
            toDayCalendar.get(Calendar.YEAR).toString(),
            (toDayCalendar.get(Calendar.MONTH) + 1).toString(),
            toDayCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        )
        setCollectTaskData()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCalendarBinding.inflate(inflater, container, false)
        setToolbarFunc()
        setRecyclerView()
        setObserver()
        if(viewModel.calendarAdapter.itemCount==0){
            val calendar = Calendar.getInstance().apply {
                time = Date()
            }
            viewModel.getHolidayYear(true, calendar.get(Calendar.YEAR))
        }
        return binding.root
    }

    private fun setObserver() {
        viewModel.updateCalendarData.observe(viewLifecycleOwner, EventObserver { result ->
            if (viewModel.firstUpdate) {
                viewModel.firstUpdate = !viewModel.firstUpdate
                scrollCurrentMonth()
            }
        })
    }

    private fun setCollectTaskData(){
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.monthTaskList?.collectLatest {
                    viewModel.addTaskDataInItem(it)
                }
            }
        }
    }

    private fun setToolbarFunc(){
        binding.tvCurrentMonth.setOnClickListener {
            scrollCurrentMonth()
        }
    }

    private fun scrollCurrentMonth(){
        viewModel.currentPos = viewModel.calendarAdapter.currentList.indexOfFirst {
            it.year == viewModel.currentToday.year &&
                    it.month == viewModel.currentToday.month
        }
        binding.rcCustomCalendar.scrollToPosition(viewModel.currentPos)
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
                @SuppressLint("SetTextI18n")
                override fun onSnapped(position: Int, isRightScroll: Boolean) {
//                    Timber.d("UpdateHoliday onSnapped position : $position | isRightScroll $isRightScroll ")
                    val currentItem = viewModel.getAdapterCurrentList()[position]
                    if(viewModel.currentPos == position) return
                    viewModel.currentPos = position
                    binding.tvCurrentMonth.text = "${currentItem.year}.${currentItem.month}"
                    viewModel.setMonthTaskList(
                        currentItem.year.toString(),
                        currentItem.month.toString(),
                        currentItem.dayList.last { it.isCurrentMonth }.day.toInt()
                    )
                    if (viewModel.getAdapterCurrentList().size > position + 3 && isRightScroll) {
                        viewModel.getHolidayYear(
                            true,
                            viewModel.calendarAdapter.currentList[position+3].year+1
                        )
                    } else if (!isRightScroll && position - 3 > 0) {
                        viewModel.getHolidayYear(
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