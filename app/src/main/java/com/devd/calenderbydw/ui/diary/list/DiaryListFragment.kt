package com.devd.calenderbydw.ui.diary.list

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.devd.calenderbydw.R
import com.devd.calenderbydw.databinding.FragmentDiaryListBinding
import com.devd.calenderbydw.utils.autoCleared
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class DiaryListFragment : Fragment() {

    private var binding by autoCleared<FragmentDiaryListBinding>()
    private val viewModel by viewModels<DiaryListViewModel>()

    private val adapter = DiaryListAdapter()
    private var refresh = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setCollectItems()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDiaryListBinding.inflate(inflater,container,false)
        binding.rcDiaryList.adapter = adapter
        adapter.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        binding.btn.setOnClickListener {
            findNavController().navigate(R.id.action_diaryListFragment_to_diaryFragment)
//            testAddData()
        }
        binding.btn2.setOnClickListener {
            adapter.refresh()
            refresh=true

        }
        return binding.root
    }

    private fun setCollectItems(){
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED){
                viewModel.item.collect {
                    adapter.submitData(viewLifecycleOwner.lifecycle,it)
                    if(refresh){
                        binding.rcDiaryList.scrollToPosition(0)
                        refresh=false
                    }
                }
            }
        }
    }

    private fun testAddData(){
        viewModel.testAddDatas()
    }

}