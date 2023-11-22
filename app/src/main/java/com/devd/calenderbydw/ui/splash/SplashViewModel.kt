package com.devd.calenderbydw.ui.splash

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devd.calenderbydw.data.remote.CallResult
import com.devd.calenderbydw.data.remote.holiday.HolidayItem
import com.devd.calenderbydw.repository.CalendarRepository
import com.devd.calenderbydw.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val calendarRepository: CalendarRepository
) :ViewModel() {

    private val _completeCalendarCreate = MutableLiveData<Event<Boolean>>()
    val completeCalendarCreate :LiveData<Event<Boolean>> get() = _completeCalendarCreate
    fun checkCalendarDB(serviceKey:String,updateCount : (progress:Int)-> Unit){
        viewModelScope.launch {
            val calendar = Calendar.getInstance()
            calendar.time = Date()
            calendarRepository.getHolidayOfYearApi(serviceKey,calendar.get(Calendar.YEAR),false,0).run {
                if((calendarRepository.checkHolidayDBOfYear(calendar.get(Calendar.YEAR))?.size?:0) == this.size){
                    val items = calendarRepository.getCalendarAllData()
                    if(items.isEmpty()){
                        createCalendarData(updateCount,calendar.get(Calendar.YEAR),this)
                    }else{
                        _completeCalendarCreate.value=Event(true)
                    }
                }else{
                    createCalendarData(updateCount,calendar.get(Calendar.YEAR),this)
                }
            }
        }
    }

    private fun createCalendarData(updateCount : (progress:Int)-> Unit, currentYear:Int, holidayList : List<HolidayItem>){
        viewModelScope.launch {
            val items = calendarRepository.getCalendarAllData()
            if(items.isEmpty()){
                calendarRepository.insertCalendarDateInDB(holidayList,currentYear){ progress->
                    updateCount(progress)
                    if(progress>=100){
                        _completeCalendarCreate.value=Event(true)
                    }
                }
            }else{
                calendarRepository.updateCalendarChangeHoliday(items,holidayList,currentYear).run {
                    _completeCalendarCreate.value=Event(true)
                }

            }
        }
    }

}