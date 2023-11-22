package com.devd.calenderbydw.ui.diary

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.devd.calenderbydw.R
import com.devd.calenderbydw.databinding.FragmentDiaryBinding
import com.devd.calenderbydw.utils.autoCleared
import okhttp3.internal.cacheGet

class DiaryFragment : Fragment() {
    private var binding by autoCleared<FragmentDiaryBinding>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDiaryBinding.inflate(inflater, container,false)
        return binding.root
    }

}