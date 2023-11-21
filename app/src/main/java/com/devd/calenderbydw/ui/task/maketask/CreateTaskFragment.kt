package com.devd.calenderbydw.ui.task.maketask

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.devd.calenderbydw.R
import com.devd.calenderbydw.data.local.entity.TaskDBEntity.Companion.DAILY_REPEAT
import com.devd.calenderbydw.data.local.entity.TaskDBEntity.Companion.MONTH_REPEAT
import com.devd.calenderbydw.data.local.entity.TaskDBEntity.Companion.NO_REPEAT
import com.devd.calenderbydw.data.local.entity.TaskDBEntity.Companion.WEEK_REPEAT
import com.devd.calenderbydw.data.local.entity.TaskDBEntity.Companion.YEAR_REPEAT
import com.devd.calenderbydw.databinding.FragmentCreateTaskBinding
import com.devd.calenderbydw.ui.dialog.CustomBottomSheetDialog
import com.devd.calenderbydw.utils.ConstVariable
import com.devd.calenderbydw.utils.EventObserver
import com.devd.calenderbydw.utils.autoCleared
import com.devd.calenderbydw.utils.getWeekToText
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Calendar

@AndroidEntryPoint
class CreateTaskFragment : Fragment() {
    private var binding by autoCleared<FragmentCreateTaskBinding>()
    private val navArgs by navArgs<CreateTaskFragmentArgs>()
    private val viewModel: CreateTaskViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.setOriginDate(navArgs.year, navArgs.month, navArgs.day)
        if(navArgs.type == ConstVariable.MODIFY_TASK && navArgs.taskData !=null){
            viewModel.setModifyTaskInfo(navArgs.taskData!!)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCreateTaskBinding.inflate(inflater, container, false)
        setToolbarFunc()
        setEditTextFunc()
        checkModifyState()
        setChangeTaskOptions()
        setDateText()
        setObserver()
        return binding.root
    }

    private fun setObserver() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.taskTitle.collectLatest {
                        context?.let { context ->
                            val taskColor = if (it.isEmpty()) {
                                context.getColor(R.color.gray_default)
                            } else {
                                context.getColor(R.color.black)
                            }
                            binding.tvInsertTask.isEnabled = it.isNotEmpty()
                            binding.tvInsertTask.setTextColor(taskColor)
                        }
                    }
                }
                launch {
                    viewModel.taskRepeatState.collectLatest { type ->
                        setRepeatView(type)
                    }
                }
                launch {
                    viewModel.taskYear.collectLatest {
                        viewModel.selectDate.year = it.toInt()
                        binding.tvTaskYear.text = it
                    }
                }
                launch {
                    viewModel.taskMonth.collectLatest {
                        binding.tvTaskMonth.text = it
                    }
                }
                launch {
                    viewModel.taskDay.collectLatest {
                        binding.tvTaskDay.text = it
                    }
                }
            }
        }
        viewModel.insertResult.observe(viewLifecycleOwner,EventObserver{
            findNavController().popBackStack()
        })
    }

    private fun checkModifyState(){
        if(navArgs.type == ConstVariable.MODIFY_TASK){
            binding.edtTaskTitle.setText(navArgs.taskData?.title?:"")
            binding.edtTaskContents.setText(navArgs.taskData?.contents?:"")
            binding.taskToolbar.title = "일정 수정기"
            binding.tvInsertTask.text = "수정"
        }
    }

    private fun setToolbarFunc() {
        binding.taskToolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        binding.tvInsertTask.setOnClickListener {
            if(navArgs.type==ConstVariable.MODIFY_TASK){
                viewModel.modifyTaskInDB()
            }else{
                viewModel.insertTaskInDB()
            }
        }
    }

    private fun setDateText() {
        binding.tvTaskYear.text = navArgs.year.toString()
        binding.tvTaskMonth.text = navArgs.month.toString()
        binding.tvTaskDay.text = navArgs.day.toString()
    }

    private fun setChangeTaskOptions() {
        binding.btnRepeat.setOnClickListener {
            CustomBottomSheetDialog.Builder().apply {
                title = "반복"
                bottomSheetItems = viewModel.taskRepeatSheetList
                itemClickListener = object : CustomBottomSheetDialog.BottomSheetClickListener {
                    override fun onItemClick(type: Int, text: String) {
                        viewModel.setChangeRepeatState(type)
                    }
                }
            }.build().show(parentFragmentManager, "repeatDialog")
        }

        binding.tvTaskYear.setOnClickListener {
            if(viewModel.taskRepeatState.value == DAILY_REPEAT ||
                viewModel.taskRepeatState.value == WEEK_REPEAT) return@setOnClickListener
            CustomBottomSheetDialog.Builder().apply {
                title = "년도"
                bottomSheetItems = viewModel.taskYearSheetList
                scrollPos = viewModel.taskYearSheetList.indexOfFirst { it.isCheck }
                itemClickListener = object : CustomBottomSheetDialog.BottomSheetClickListener {
                    override fun onItemClick(type: Int, text: String) {
                        if(text == binding.tvTaskYear.text.toString()) return
                        viewModel.setChangeSelectYear(text)
                    }
                }
            }.build().show(parentFragmentManager, "yearDialog")

        }
        binding.tvTaskMonth.setOnClickListener {
            if(viewModel.taskRepeatState.value == MONTH_REPEAT ||
                viewModel.taskRepeatState.value == WEEK_REPEAT) return@setOnClickListener
            CustomBottomSheetDialog.Builder().apply {
                title = "달"
                bottomSheetItems = viewModel.taskMonthSheetList
                scrollPos = viewModel.taskMonthSheetList.indexOfFirst { it.isCheck }
                itemClickListener = object : CustomBottomSheetDialog.BottomSheetClickListener {
                    override fun onItemClick(type: Int, text: String) {
                        if(text == binding.tvTaskMonth.text.toString()) return
                        viewModel.setChangeSelectMonth(text)
                    }
                }
            }.build().show(parentFragmentManager, "yearDialog")

        }
        binding.tvTaskDay.setOnClickListener {
            if(viewModel.taskRepeatState.value == YEAR_REPEAT ||
                viewModel.taskRepeatState.value == WEEK_REPEAT) return@setOnClickListener
            CustomBottomSheetDialog.Builder().apply {
                title = "일"
                bottomSheetItems = viewModel.taskDaySheetList
                scrollPos = viewModel.taskDaySheetList.indexOfFirst { it.isCheck }
                itemClickListener = object : CustomBottomSheetDialog.BottomSheetClickListener {
                    override fun onItemClick(type: Int, text: String) {
                        if(text == binding.tvTaskDay.text.toString()) return
                        viewModel.setChangeSelectDay(text)
                    }
                }
            }.build().show(parentFragmentManager, "yearDialog")

        }

    }


    @SuppressLint("SetTextI18n")
    private fun setRepeatView(type: Int) {
        if (type == NO_REPEAT) {
            binding.tvTaskDayHolder.text = "일에"
            binding.tvTaskRepeatHolder.visibility = View.GONE
        } else {
            binding.tvTaskDayHolder.text = "일 부터"
            binding.tvTaskRepeatHolder.visibility = View.VISIBLE
        }
        when (type) {
            NO_REPEAT -> {
                binding.btnRepeat.text = "반복 : 없음"
                binding.tvTaskRepeatHolder.text = ""
            }

            DAILY_REPEAT -> {
                binding.btnRepeat.text = "반복 : 매일"
                binding.tvTaskRepeatHolder.text = "매일"
            }

            WEEK_REPEAT -> {
                val calendar = Calendar.getInstance()
                calendar.set(
                    binding.tvTaskYear.text.toString().toInt(),
                    binding.tvTaskMonth.text.toString().toInt()-1,
                    binding.tvTaskDay.text.toString().toInt()
                )
                val weekStr = calendar.getWeekToText()
                binding.btnRepeat.text = "반복 : 매주"
                binding.tvTaskRepeatHolder.text = "매주 [${weekStr}]"
            }

            MONTH_REPEAT -> {
                binding.btnRepeat.text = "반복 : 매달"
                binding.tvTaskRepeatHolder.text = "매달"
            }

            YEAR_REPEAT -> {
                binding.btnRepeat.text = "반복 : 매년"
                binding.tvTaskRepeatHolder.text = "매년"
            }
        }
    }

    private fun setEditTextFunc() {
        binding.edtTaskTitle.doOnTextChanged { text, _, _, _ ->
            viewModel.setTitle(text?.toString() ?: "")
        }

        binding.edtTaskContents.doOnTextChanged { text, _, _, _ ->
            viewModel.setContents(text?.toString())
        }
    }


}