package com.devd.calenderbydw.ui.diary.list

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
import androidx.recyclerview.widget.RecyclerView
import com.devd.calenderbydw.R
import com.devd.calenderbydw.data.local.entity.DiaryEntity
import com.devd.calenderbydw.databinding.FragmentDiaryListBinding
import com.devd.calenderbydw.ui.dialog.CommonDialog
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
        binding = FragmentDiaryListBinding.inflate(inflater, container, false)
        setToolbarFunc()
        setDiaryListAdapter()
        setObserver()
        return binding.root
    }

    private fun setObserver() {
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>("update")
            ?.observe(viewLifecycleOwner) {
                Timber.d("UpdateCheck -> ${it}")
                if (it) {
                    adapter.refresh()
                    refresh = true
                    viewModel.setTodayCanWrite(true)
                    findNavController().currentBackStackEntry?.savedStateHandle?.remove<Boolean>("update")
                }
            }

        viewModel.todayWriteDiary.observe(viewLifecycleOwner) {
            val writeBtnColor = if (it) {
                requireContext().getColor(R.color.grayDayColor)
            } else {
                requireContext().getColor(R.color.commonDayColor)
            }
            binding.btnDiaryWrite.setTextColor(writeBtnColor)
        }
    }

    private fun setToolbarFunc() {
        binding.btnDiaryWrite.setOnClickListener {
//                findNavController().navigate(R.id.action_diaryListFragment_to_diaryFragment)
            if (viewModel.todayWriteDiary.value == true) {
                CommonDialog.Builder().apply {
                    message = "이미 오늘의 일기를 작성하셨습니다."
                }.build().show(parentFragmentManager, "writeDiary")
            } else {
                findNavController().navigate(R.id.action_diaryListFragment_to_diaryFragment)
            }
        }
    }

    private fun setCollectItems() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.item.collectLatest {
                    adapter.submitData(viewLifecycleOwner.lifecycle, it)
                }
            }
        }
    }

    private fun setDiaryListAdapter() {
        binding.rcDiaryList.adapter = adapter
        adapter.setOnDiaryClickListener(object :DiaryListAdapter.DiaryClickListener{
            override fun onItemClick(item: DiaryEntity) {
                findNavController().navigate(R.id.action_diaryListFragment_to_diaryPageFragment,
                    bundleOf(
                        "diaryData" to item
                    )
                )
            }
        })
        adapter.stateRestorationPolicy =
            RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                if (refresh) {
                    binding.rcDiaryList.scrollToPosition(0)
                    refresh = false
                }
            }
        })
    }

    private fun testAddData() {
        viewModel.testAddDatas()
    }

}