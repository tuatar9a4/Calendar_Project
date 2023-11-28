package com.devd.calenderbydw.ui.diary.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.devd.calenderbydw.data.local.entity.DiaryEntity
import com.devd.calenderbydw.repository.CalendarDataStore
import com.devd.calenderbydw.repository.DataStoreKey
import com.devd.calenderbydw.repository.DiaryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DiaryListViewModel @Inject constructor(
    private val diaryRepository: DiaryRepository,
    private val dataStore: CalendarDataStore
) :ViewModel() {
    private val _todayWriteDiary =MutableLiveData<Boolean>()
    val todayWriteDiary :LiveData<Boolean> get() = _todayWriteDiary

    init {
        viewModelScope.launch{
            _todayWriteDiary.value = dataStore.getPreferBoolean(DataStoreKey.PREF_TODAY_ALREADY_WRITE_DIARY)?:false
        }
    }


    val item = diaryRepository.getDiaryFlowList().flow.cachedIn(viewModelScope)
    fun getDiaryList(): Flow<PagingData<DiaryEntity>> {
        return diaryRepository.getDiaryFlowList().flow.cachedIn(viewModelScope)
    }


    fun testAddDatas(){
        viewModelScope.launch {
            for(i in 0..20){
                diaryRepository.insertDiaryInfo(DiaryEntity(createDate = System.currentTimeMillis()+i))
            }
        }
    }
}