package com.devd.calenderbydw.ui.splash

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devd.calenderbydw.repository.CalendarRepository
import com.devd.calenderbydw.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val calendarRepository: CalendarRepository
) :ViewModel() {

    private val _completeCalendarCreate = MutableLiveData<Event<Boolean>>()
    val completeCalendarCreate :LiveData<Event<Boolean>> get() = _completeCalendarCreate
    fun checkCalendarDB(updateCount : (progress:Int)-> Unit){
        viewModelScope.launch {
            if((calendarRepository.getCalendarDataSize()?:0)<1){
                calendarRepository.insertCalendarDateInDB{ progress->
                    updateCount(progress)
                    if(progress>=100){
                        _completeCalendarCreate.value=Event(true)
                    }
                }
            }else{
                _completeCalendarCreate.value=Event(true)
            }
        }
    }

}