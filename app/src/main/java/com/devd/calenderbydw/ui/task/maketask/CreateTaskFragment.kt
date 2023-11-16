package com.devd.calenderbydw.ui.task.maketask

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.devd.calenderbydw.R
import com.devd.calenderbydw.databinding.FragmentCreateTaskBinding
import com.devd.calenderbydw.utils.autoCleared
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class CreateTaskFragment : Fragment() {
    private var binding by autoCleared<FragmentCreateTaskBinding>()
    private val viewModel : CreateTaskViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCreateTaskBinding.inflate(inflater,container,false)
        setToolbarFunc()
        setEditTextFunc()
        setObserver()
        return binding.root
    }

    private fun setObserver(){
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.taskTitle.collectLatest {
                    Timber.d("CreateTaskBtn title => ${it}")
                    context?.let { context ->
                        val taskColor = if(it.isEmpty()){
                            context.getColor(R.color.grayColor)
                        }else{
                            context.getColor(R.color.black)
                        }
                        binding.tvInsertTask.isEnabled = it.isNotEmpty()
                        binding.tvInsertTask.setTextColor(taskColor)
                    }
                }
            }
        }
    }
    private fun setToolbarFunc(){
        binding.taskToolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        binding.tvInsertTask.setOnClickListener {
            Timber.d("CreateTaskBtn => ")
        }
    }

    private fun setEditTextFunc(){
        binding.edtTaskTitle.doOnTextChanged { text, _, _, _ ->
            viewModel.setTitle(text?.toString()?:"")
        }

        binding.edtTaskContents.doOnTextChanged { text, _, _, _ ->
            viewModel.setContents(text?.toString()?:"")
        }
    }


}