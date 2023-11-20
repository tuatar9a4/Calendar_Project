package com.devd.calenderbydw.ui.task.tasklist

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
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.devd.calenderbydw.R
import com.devd.calenderbydw.data.local.calendar.YearMonthDayData
import com.devd.calenderbydw.databinding.FragmentTaskListBinding
import com.devd.calenderbydw.utils.EventObserver
import com.devd.calenderbydw.utils.autoCleared
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class TaskListFragment : Fragment() {

    private var binding by autoCleared<FragmentTaskListBinding>()
    private val viewModel: TaskListViewModel by viewModels()
    private val navArgs by navArgs<TaskListFragmentArgs>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getTaskListOfDat()
        setCollectFlows()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTaskListBinding.inflate(inflater, container, false)
        setToolbarFunc()
        setTopDateRecyclerview()
        setScheduleRecyclerView()
        setObserver()
        if (viewModel.topDateAdapter.itemCount == 0) {
            viewModel.getCalendarList(
                getString(R.string.holidayEncodingKey),
                navArgs.year,
                navArgs.month,
                navArgs.day
            )
        }
        return binding.root
    }

    private fun setObserver() {
        viewModel.taskDateResult.observe(viewLifecycleOwner, EventObserver {
            binding.rcScheduledDate.scrollToPosition(it)
            viewModel.topDateAdapter.setSelectPos(it)
        })
        viewModel.taskListDate.observe(viewLifecycleOwner,EventObserver{ taskItems ->
            viewModel.scheduleItemAdapter.submitList(taskItems)
        })
    }

    private fun setCollectFlows() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.taskList?.collectLatest {
                    viewModel.scheduleItemAdapter.submitList(it)
                }
            }
        }
    }

    private fun getTaskListOfDat() {
        viewModel.selectYearToDay = YearMonthDayData(
            year = navArgs.year,
            month = navArgs.month,
            day = navArgs.day
        )
        viewModel.getSelectDateTaskList(
            navArgs.year.toString(),
            navArgs.month.toString(),
            navArgs.day.toString()
        )
    }

    private fun setToolbarFunc() {
        binding.taskListToolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.createTask -> {
                    viewModel.selectYearToDay?.let { date ->
                        findNavController().navigate(
                            R.id.action_taskListFragment_to_createTaskFragment,
                            bundleOf(
                                "year" to date.year,
                                "month" to date.month,
                                "day" to date.day,
                            )
                        )
                    }
                }
            }
            return@setOnMenuItemClickListener false
        }
        if(viewModel.currentTopYearMonth.isNotEmpty()){
            binding.taskListToolbar.title = viewModel.currentTopYearMonth
        }
    }

    private fun setTopDateRecyclerview() {
        binding.rcScheduledDate.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.rcScheduledDate.adapter = viewModel.topDateAdapter

        binding.tvDay.text=viewModel.selectYearToDay?.let {
            "${it.year}.${it.month}.${it.day}"
        }?: kotlin.run {
            "${navArgs.year}.${navArgs.month}.${navArgs.day}"
        }

        viewModel.topDateAdapter.setOnTopDateClickListener(object :
            TaskListTopDateAdapter.OnTopDateClickListener {
            override fun onDateClick(pos: Int, year: String, month: String, day: String) {
                viewModel.topDateAdapter.setSelectPos(pos)
                viewModel.selectYearToDay = YearMonthDayData(
                    year = year.toInt(),
                    month = month.toInt(),
                    day = day.toInt()
                )
                viewModel.getSelectDateTaskListInit(year,month, day)
                binding.tvDay.text ="${year}.${month}.${day}"
            }
        })

        binding.rcScheduledDate.addOnScrollListener(object : OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val lastItemPosition =
                    (recyclerView.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
                val firstItemPosition =
                    (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()

                val lastItem = viewModel.topDateAdapter.currentList[lastItemPosition]
                val first = viewModel.topDateAdapter.currentList[firstItemPosition]
//                Timber.d("isRight? ${isScrollRight(dx)} :${lastItemPosition} || ${firstItemPosition}")
                if (viewModel.currentTopYearMonth != "${lastItem.year}${lastItem.month}" &&
                    isScrollRight(dx)
                ) {
                    viewModel.currentTopYearMonth = "${lastItem.year}${lastItem.month}"
                    binding.taskListToolbar.title = "${lastItem.year}${lastItem.month}"
                } else if (viewModel.currentTopYearMonth != "${first.year}${first.month}" &&
                    !isScrollRight(dx)
                ) {
                    viewModel.currentTopYearMonth = "${first.year}${first.month}"
                    binding.taskListToolbar.title = "${first.year}${first.month}"
                }
            }

            fun isScrollRight(x: Int) = x > 0
        })
    }

    private fun setScheduleRecyclerView(){
        binding.rcSelectTaskList.layoutManager=LinearLayoutManager(context,LinearLayoutManager.VERTICAL,false)
        binding.rcSelectTaskList.adapter = viewModel.scheduleItemAdapter
    }

}