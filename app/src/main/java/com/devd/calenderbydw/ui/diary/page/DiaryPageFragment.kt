package com.devd.calenderbydw.ui.diary.page

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.devd.calenderbydw.R
import com.devd.calenderbydw.databinding.FragmentDiaryPageBinding
import com.devd.calenderbydw.utils.ConstVariable.FEEL_TYPE_BAD
import com.devd.calenderbydw.utils.ConstVariable.FEEL_TYPE_BAD_TXT
import com.devd.calenderbydw.utils.ConstVariable.FEEL_TYPE_GOOD
import com.devd.calenderbydw.utils.ConstVariable.FEEL_TYPE_GOOD_TXT
import com.devd.calenderbydw.utils.ConstVariable.FEEL_TYPE_HAPPY
import com.devd.calenderbydw.utils.ConstVariable.FEEL_TYPE_HAPPY_TXT
import com.devd.calenderbydw.utils.ConstVariable.FEEL_TYPE_SOSO
import com.devd.calenderbydw.utils.ConstVariable.FEEL_TYPE_SOSO_TXT
import com.devd.calenderbydw.utils.ConstVariable.WEATHER_TYPE_CLOUDY
import com.devd.calenderbydw.utils.ConstVariable.WEATHER_TYPE_CLOUDY_TXT
import com.devd.calenderbydw.utils.ConstVariable.WEATHER_TYPE_RAIN
import com.devd.calenderbydw.utils.ConstVariable.WEATHER_TYPE_RAIN_TXT
import com.devd.calenderbydw.utils.ConstVariable.WEATHER_TYPE_SOSO
import com.devd.calenderbydw.utils.ConstVariable.WEATHER_TYPE_SOSO_TXT
import com.devd.calenderbydw.utils.ConstVariable.WEATHER_TYPE_SUNNY
import com.devd.calenderbydw.utils.ConstVariable.WEATHER_TYPE_SUNNY_TXT
import com.devd.calenderbydw.utils.autoCleared
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DiaryPageFragment : Fragment() {

    private var binding by autoCleared<FragmentDiaryPageBinding>()
    private val navArgs by navArgs<DiaryPageFragmentArgs>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDiaryPageBinding.inflate(inflater,container,false)
        binding.diaryPageToolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        setWeatherFeelText()
        setToolbarDateText()
        setDiaryContents()
        setDiaryStickerImage()
        return binding.root
    }

    private fun setWeatherFeelText(){
        val weatherText = when(navArgs.diaryData.weatherType){
            WEATHER_TYPE_SUNNY->{
                    WEATHER_TYPE_SUNNY_TXT
                }
                WEATHER_TYPE_CLOUDY->{
                    WEATHER_TYPE_CLOUDY_TXT
                }
                WEATHER_TYPE_RAIN->{
                    WEATHER_TYPE_RAIN_TXT
                }
                WEATHER_TYPE_SOSO->{
                    WEATHER_TYPE_SOSO_TXT
                }
                else->{
                    navArgs.diaryData.customWeather?:"알수 없음"
                }
        }
        val feelText = when(navArgs.diaryData.feelingType){
          FEEL_TYPE_GOOD->{
                    FEEL_TYPE_GOOD_TXT
                }
                FEEL_TYPE_BAD->{
                    FEEL_TYPE_BAD_TXT
                }
                FEEL_TYPE_SOSO->{
                    FEEL_TYPE_SOSO_TXT
                }
                FEEL_TYPE_HAPPY->{
                    FEEL_TYPE_HAPPY_TXT
                }
                else->{
                    navArgs.diaryData.customFeel?:"알수 없음"
                }
        }

        binding.tvWeatherFeel.text = requireContext().getString(R.string.weather_fell_holder,
            weatherText,
            feelText)
    }
    private fun setToolbarDateText(){
        binding.diaryPageToolbar.title =
            requireContext().getString(R.string.diary_page_toolbar_holder,"${navArgs.diaryData.year}.${navArgs.diaryData.month}.${navArgs.diaryData.day}")
    }
    private fun setDiaryContents(){
        binding.tvDiaryContents.text = requireContext().getString(R.string.today_diary_holder,
            navArgs.diaryData.diaryContents)
    }
    private fun setDiaryStickerImage(){
        val bucketKey = requireContext().getString(R.string.oracleBucketKey)
        val baseUrl = requireContext().getString(R.string.oracle_bucket_image_path,bucketKey)
        Glide.with(requireContext())
            .load("${baseUrl}${navArgs.diaryData.stickerName}")
            .into(binding.ivDiarySticker)
    }
}