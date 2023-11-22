package com.devd.calenderbydw.ui.my

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.devd.calenderbydw.R
import com.devd.calenderbydw.databinding.FragmentMyBinding
import com.devd.calenderbydw.utils.autoCleared

class MyFragment : Fragment() {
    private var binding by autoCleared<FragmentMyBinding>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMyBinding.inflate(inflater,container,false)
        return binding.root
    }

}