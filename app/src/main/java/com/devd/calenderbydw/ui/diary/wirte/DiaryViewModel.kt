package com.devd.calenderbydw.ui.diary.wirte

import android.icu.text.SimpleDateFormat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devd.calenderbydw.data.local.dialog.BottomSheetItem
import com.devd.calenderbydw.data.local.entity.DiaryEntity
import com.devd.calenderbydw.repository.CalendarDataStore
import com.devd.calenderbydw.repository.DataStoreKey.Companion.PREF_TODAY_ALREADY_WRITE_DIARY
import com.devd.calenderbydw.repository.DataStoreKey.Companion.PREF_WRITE_DIARY_LAST_DATE
import com.devd.calenderbydw.repository.DiaryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class DiaryViewModel @Inject constructor(
    private val repository: DiaryRepository,
    private val dataStore: CalendarDataStore
) : ViewModel() {

    val weatherList = getWeatherBottomList()
    val feelList = getFeelBottomList()
    private val _insertResult = MutableLiveData<Boolean>()
    val insertResult :LiveData<Boolean> get() = _insertResult

    private val _contents = MutableStateFlow<String>("")
    private val _weatherType = MutableStateFlow("")
    val weatherType :StateFlow<String> get() = _weatherType
    private val _feelType = MutableStateFlow("")
    val feelType :StateFlow<String> get() = _feelType
    private var _customWeatherStr = MutableStateFlow("")
    private var _customFeelStr = MutableStateFlow("")

    val checkCanWrite = combine(
        _contents,
        _weatherType,
        _feelType,
        _customWeatherStr,
        _customFeelStr
    ) { (contents, weatherType, feelType, weatherStr, feelStr) ->
        return@combine contents.isNotEmpty() &&
                ((weatherType == WEATHER_TYPE_ETC.toString() && weatherStr.isNotEmpty()) || weatherType != WEATHER_TYPE_ETC.toString()) &&
                ((feelType == FEEL_TYPE_ETC.toString() && feelStr.isNotEmpty()) || feelType != FEEL_TYPE_ETC.toString())
    }

    fun setContents(contents: String) {
        _contents.value = contents
    }

    fun setCustomWeather(type:Int) {
        _weatherType.value=type.toString()
        weatherList.forEach {
            it.isCheck = it.type == type
        }
        setCustomWeatherStr("")
    }
    fun setCustomWeatherStr(str :String){
        _customWeatherStr.value = str
    }

    fun setCustomFeel(type:Int) {
        _feelType.value=type.toString()
        feelList.forEach {
            it.isCheck = it.type == type
        }
        setCustomFeelStr("")
    }
    fun setCustomFeelStr(str :String){
        _customFeelStr.value = str
    }

    private fun getSelectWeatherType() = weatherList.first { it.isCheck }.type
    private fun getSelectFeelType() = feelList.first { it.isCheck }.type


    fun sendDiaryData(){
        viewModelScope.launch {
            val dateFormat = SimpleDateFormat("yyyy.MM.dd")
            val playDate =dateFormat.format(Date(System.currentTimeMillis()))
            repository.insertDiaryInfo(DiaryEntity(
                year = playDate.split(".")[0].toInt(),
                month = playDate.split(".")[1].toInt(),
                day = playDate.split(".")[2].toInt(),
                diaryContents = _contents.value,
                weatherType = getSelectWeatherType(),
                customWeather = if(getSelectFeelType()== WEATHER_TYPE_ETC) _customWeatherStr.value else null,
                feelingType = getSelectFeelType(),
                customFeel = if(getSelectFeelType() == FEEL_TYPE_ETC) _customFeelStr.value else null,
                createDate = Date().time
            )).run {
                dataStore.setPreferString(PREF_WRITE_DIARY_LAST_DATE,playDate)
                dataStore.setPreferBoolean(PREF_TODAY_ALREADY_WRITE_DIARY,true)
                _insertResult.value=true
            }
        }
    }

    private fun getWeatherBottomList() = listOf<BottomSheetItem>(
        BottomSheetItem(
            type = WEATHER_TYPE_SUNNY,
            text = "맑음!",
            isCheck = true,
        ),
        BottomSheetItem(
            type = WEATHER_TYPE_CLOUDY,
            text = "흐림..",
            isCheck = false,
        ),
        BottomSheetItem(
            type = WEATHER_TYPE_RAIN,
            text = "비...",
            isCheck = false,
        ),
        BottomSheetItem(
            type = WEATHER_TYPE_SOSO,
            text = "보통",
            isCheck = false,
        ),
        BottomSheetItem(
            type = WEATHER_TYPE_ETC,
            text = "직접 입력",
            isCheck = false,
        )
    )

    private fun getFeelBottomList() = listOf<BottomSheetItem>(
        BottomSheetItem(
            type = FEEL_TYPE_GOOD,
            text = "좋음",
            isCheck = true,
        ),
        BottomSheetItem(
            type = FEEL_TYPE_BAD,
            text = "나쁨",
            isCheck = false,
        ),
        BottomSheetItem(
            type = FEEL_TYPE_SOSO,
            text = "그저그럼",
            isCheck = false,
        ),
        BottomSheetItem(
            type = FEEL_TYPE_HAPPY,
            text = "행복함",
            isCheck = false,
        ),
        BottomSheetItem(
            type = FEEL_TYPE_ETC,
            text = "직접 입력",
            isCheck = false,
        )
    )

    companion object {
        const val WEATHER_TYPE_SUNNY = 0
        const val WEATHER_TYPE_CLOUDY = 1
        const val WEATHER_TYPE_RAIN = 2
        const val WEATHER_TYPE_SOSO = 3
        const val WEATHER_TYPE_ETC = 4
        const val FEEL_TYPE_GOOD = 0
        const val FEEL_TYPE_BAD = 1
        const val FEEL_TYPE_SOSO = 2
        const val FEEL_TYPE_HAPPY = 3
        const val FEEL_TYPE_ETC = 4
    }
}