package com.devd.calenderbydw.ui.task.maketask

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.devd.calenderbydw.R
import com.devd.calenderbydw.databinding.FragmentCreateTaskBinding
import com.devd.calenderbydw.utils.autoCleared

class CreateTaskFragment : Fragment() {
    private var binding by autoCleared<FragmentCreateTaskBinding>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCreateTaskBinding.inflate(inflater,container,false)
        // Inflate the layout for this fragment
        return binding.root
    }

}