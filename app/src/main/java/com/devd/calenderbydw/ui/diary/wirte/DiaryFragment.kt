package com.devd.calenderbydw.ui.diary.wirte

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.devd.calenderbydw.R
import com.devd.calenderbydw.databinding.FragmentDiaryBinding
import com.devd.calenderbydw.ui.dialog.CommonDialog
import com.devd.calenderbydw.ui.dialog.CustomBottomSheetDialog
import com.devd.calenderbydw.ui.dialog.StickerBottomSheetDialog
import com.devd.calenderbydw.utils.ConstVariable.FEEL_TYPE_ETC
import com.devd.calenderbydw.utils.ConstVariable.WEATHER_TYPE_ETC
import com.devd.calenderbydw.utils.autoCleared
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DiaryFragment : Fragment() {
    private var binding by autoCleared<FragmentDiaryBinding>()
    private val diaryViewModel: DiaryViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDiaryBinding.inflate(inflater, container, false)
        setToolbar()
        setEditTextsFunc()
        setWriteButtonFunc()
        setDiaryOptionClick()
        setObserver()
        return binding.root
    }

    private fun setObserver() {
        diaryViewModel.insertResult.observe(viewLifecycleOwner) {
            Toast.makeText(requireContext(),"일기를 작성했습니다.",Toast.LENGTH_SHORT).show()
            findNavController().previousBackStackEntry?.savedStateHandle?.set(
                "update",true
            )
            findNavController().popBackStack()
        }
        CoroutineScope(Dispatchers.IO).launch {
            launch {
                diaryViewModel.checkCanWrite.collectLatest {
                    val textColor = if (it) {
                        requireContext().getColor(R.color.commonDayColor)
                    } else {
                        requireContext().getColor(R.color.grayDayColor)
                    }
                    binding.tvWriteDiary.isClickable = it
                    binding.tvWriteDiary.setTextColor(textColor)
                }
            }
            launch {
                diaryViewModel.weatherType.collectLatest {
                    if (it == WEATHER_TYPE_ETC.toString()) {
                        binding.tvTodayWeather.visibility = View.GONE
                        binding.edtTodayWeather.visibility = View.VISIBLE
                    } else {
                        binding.tvTodayWeather.visibility = View.VISIBLE
                        binding.edtTodayWeather.visibility = View.GONE
                    }
                }
            }
            launch {
                diaryViewModel.feelType.collectLatest {
                    if (it == FEEL_TYPE_ETC.toString()) {
                        binding.tvTodayFeeling.visibility = View.GONE
                        binding.edtTodayFeeling.visibility = View.VISIBLE
                    } else {
                        binding.tvTodayFeeling.visibility = View.VISIBLE
                        binding.edtTodayFeeling.visibility = View.GONE
                    }
                }
            }
            launch {
                diaryViewModel.currentStickerName.collectLatest {
                    activity?.runOnUiThread {
                        setStickerImage(it)
                    }
                }
            }
        }
    }

    private fun setToolbar() {
        binding.diaryToolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setWriteButtonFunc() {
        binding.tvWriteDiary.setOnClickListener {
            CommonDialog.Builder().apply {
                message = "일기를 작성 하시 겠습니까?"
                negativeBtnString = "더 쓰기"
                positiveBtnString = "작성"
                positiveBtnClickListener = object : CommonDialog.CommonDialogClickListener {
                    override fun onClick() {
                        diaryViewModel.sendDiaryData()
                    }
                }
            }.build().show(parentFragmentManager,"writeDialog")
        }
    }

    private fun setDiaryOptionClick() {
        binding.ivChangeWeather.setOnClickListener {
            CustomBottomSheetDialog.Builder().apply {
                title = "오늘의 날씨"
                sheetItem(diaryViewModel.weatherList)
                itemClickListener = object : CustomBottomSheetDialog.BottomSheetClickListener {
                    override fun onItemClick(type: Int, text: String) {
                        if (type != WEATHER_TYPE_ETC) {
                            binding.tvTodayWeather.text = text
                        } else {
                            binding.edtTodayWeather.setText("")
                        }
                        diaryViewModel.setCustomWeather(type)
                    }
                }
            }.build().show(parentFragmentManager, "weatherDialog")
        }
        binding.ivChangeFeeling.setOnClickListener {
            CustomBottomSheetDialog.Builder().apply {
                title = "오늘의 기분"
                sheetItem(diaryViewModel.feelList)
                itemClickListener = object : CustomBottomSheetDialog.BottomSheetClickListener {
                    override fun onItemClick(type: Int, text: String) {
                        if (type != FEEL_TYPE_ETC) {
                            binding.tvTodayFeeling.text = text
                        } else {
                            binding.edtTodayFeeling.setText("")
                        }
                        diaryViewModel.setCustomFeel(type)
                    }
                }
            }.build().show(parentFragmentManager, "feelDialog")
        }
        binding.ivSticker.setOnClickListener {
            if(diaryViewModel.stickerList.isNotEmpty()){
                StickerBottomSheetDialog.Builder().apply {
                    stickerList = diaryViewModel.stickerList
                    stickerClickListener = object :StickerBottomSheetDialog.StickerClickListener{
                        override fun onStickerClick(stickerId: String) {
                            diaryViewModel.setStickerId(stickerId)
                        }
                    }
                }.build().show(parentFragmentManager,"stickerDialog")
            }
        }
    }

    private fun setStickerImage(stickerId :String){
        val key =requireContext().getString(R.string.oracleBucketKey)
        Glide.with(requireContext())
            .load("${requireContext().getString(R.string.oracle_bucket_image_path,key)}${stickerId}")
            .into(binding.ivSticker)
    }

    private fun setEditTextsFunc() {
        binding.edtTodayWeather.doOnTextChanged { text, _, _, _ ->
            diaryViewModel.setCustomWeatherStr(text.toString())
        }
        binding.edtTodayFeeling.doOnTextChanged { text, _, _, _ ->
            diaryViewModel.setCustomFeelStr(text.toString())
        }
        binding.edtTodayDiary.doOnTextChanged { text, _, _, _ ->
            diaryViewModel.setContents(text.toString())
        }
    }

}