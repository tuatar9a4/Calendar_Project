package com.devd.calenderbydw.ui.diary.wirte

import android.icu.text.SimpleDateFormat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devd.calenderbydw.data.local.dialog.BottomSheetItem
import com.devd.calenderbydw.data.local.entity.DiaryEntity
import com.devd.calenderbydw.data.remote.CallResult
import com.devd.calenderbydw.data.remote.calendar.DiaryStickerResponse
import com.devd.calenderbydw.repository.CalendarDataStore
import com.devd.calenderbydw.repository.DataStoreKey.Companion.PREF_TODAY_ALREADY_WRITE_DIARY
import com.devd.calenderbydw.repository.DataStoreKey.Companion.PREF_WRITE_DIARY_LAST_DATE
import com.devd.calenderbydw.repository.DiaryRepository
import com.devd.calenderbydw.utils.ConstVariable.FEEL_TYPE_BAD
import com.devd.calenderbydw.utils.ConstVariable.FEEL_TYPE_BAD_TXT
import com.devd.calenderbydw.utils.ConstVariable.FEEL_TYPE_ETC
import com.devd.calenderbydw.utils.ConstVariable.FEEL_TYPE_GOOD
import com.devd.calenderbydw.utils.ConstVariable.FEEL_TYPE_GOOD_TXT
import com.devd.calenderbydw.utils.ConstVariable.FEEL_TYPE_HAPPY
import com.devd.calenderbydw.utils.ConstVariable.FEEL_TYPE_HAPPY_TXT
import com.devd.calenderbydw.utils.ConstVariable.FEEL_TYPE_SOSO
import com.devd.calenderbydw.utils.ConstVariable.FEEL_TYPE_SOSO_TXT
import com.devd.calenderbydw.utils.ConstVariable.WEATHER_TYPE_CLOUDY
import com.devd.calenderbydw.utils.ConstVariable.WEATHER_TYPE_CLOUDY_TXT
import com.devd.calenderbydw.utils.ConstVariable.WEATHER_TYPE_ETC
import com.devd.calenderbydw.utils.ConstVariable.WEATHER_TYPE_RAIN
import com.devd.calenderbydw.utils.ConstVariable.WEATHER_TYPE_RAIN_TXT
import com.devd.calenderbydw.utils.ConstVariable.WEATHER_TYPE_SOSO
import com.devd.calenderbydw.utils.ConstVariable.WEATHER_TYPE_SOSO_TXT
import com.devd.calenderbydw.utils.ConstVariable.WEATHER_TYPE_SUNNY
import com.devd.calenderbydw.utils.ConstVariable.WEATHER_TYPE_SUNNY_TXT
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
    private val _weatherType = MutableStateFlow(WEATHER_TYPE_SUNNY.toString())
    val weatherType :StateFlow<String> get() = _weatherType
    private val _feelType = MutableStateFlow(FEEL_TYPE_GOOD.toString())
    val feelType :StateFlow<String> get() = _feelType
    private var _customWeatherStr = MutableStateFlow("")
    private var _customFeelStr = MutableStateFlow("")

    private val _currentStickerName = MutableStateFlow("icon_sticker_happy.png")
    val currentStickerName :StateFlow<String> get() = _currentStickerName
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

    var stickerList = listOf<DiaryStickerResponse>()

    init {
        viewModelScope.launch {
            repository.getDiaryStickers().run {
                when(this){
                    is CallResult.Success->{
                        stickerList=this.data
                    }
                    else ->{

                    }
                }
            }
        }
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

    fun setStickerId(id :String){
        _currentStickerName.value = id
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
                stickerName = _currentStickerName.value,
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
            text = WEATHER_TYPE_SUNNY_TXT,
            isCheck = true,
        ),
        BottomSheetItem(
            type = WEATHER_TYPE_CLOUDY,
            text = WEATHER_TYPE_CLOUDY_TXT,
            isCheck = false,
        ),
        BottomSheetItem(
            type = WEATHER_TYPE_RAIN,
            text = WEATHER_TYPE_RAIN_TXT,
            isCheck = false,
        ),
        BottomSheetItem(
            type = WEATHER_TYPE_SOSO,
            text = WEATHER_TYPE_SOSO_TXT,
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
            text = FEEL_TYPE_GOOD_TXT,
            isCheck = true,
        ),
        BottomSheetItem(
            type = FEEL_TYPE_BAD,
            text = FEEL_TYPE_BAD_TXT,
            isCheck = false,
        ),
        BottomSheetItem(
            type = FEEL_TYPE_SOSO,
            text = FEEL_TYPE_SOSO_TXT,
            isCheck = false,
        ),
        BottomSheetItem(
            type = FEEL_TYPE_HAPPY,
            text = FEEL_TYPE_HAPPY_TXT,
            isCheck = false,
        ),
        BottomSheetItem(
            type = FEEL_TYPE_ETC,
            text = "직접 입력",
            isCheck = false,
        )
    )

}