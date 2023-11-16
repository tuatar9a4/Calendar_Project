package com.devd.calenderbydw.ui.task.tasklist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.devd.calenderbydw.R
import com.devd.calenderbydw.databinding.FragmentTaskListBinding
import com.devd.calenderbydw.utils.EventObserver
import com.devd.calenderbydw.utils.autoCleared
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TaskListFragment : Fragment() {

    private var binding by autoCleared<FragmentTaskListBinding>()
    private val viewModel: TaskListViewModel by viewModels()
    private val navArgs by navArgs<TaskListFragmentArgs>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTaskListBinding.inflate(inflater, container, false)
        setToolbarFunc()
        setTopDateRecyclerview()
        setObserver()
        viewModel.getCalendarList(
            getString(R.string.holidayEncodingKey),
            navArgs.year,
            navArgs.month,
            navArgs.day
        )
        return binding.root
    }

    private fun setObserver() {
        viewModel.taskListDates.observe(viewLifecycleOwner, EventObserver {
            binding.rcScheduledDate.scrollToPosition(it)
        })
    }

    private fun setToolbarFunc() {
        binding.taskListToolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.createTask -> {
                    findNavController().navigate(R.id.action_taskListFragment_to_createTaskFragment2)
                }
            }
            return@setOnMenuItemClickListener false
        }
    }

    private fun setTopDateRecyclerview() {
        binding.rcScheduledDate.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.rcScheduledDate.adapter = viewModel.topDateAdapter
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

}