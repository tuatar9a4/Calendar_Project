package com.devd.calenderbydw.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.devd.calenderbydw.data.local.dao.DiaryDao
import com.devd.calenderbydw.data.local.entity.DiaryEntity
import com.devd.calenderbydw.data.remote.api.CalendarService
import com.devd.calenderbydw.di.NetworkModule
import com.devd.calenderbydw.ui.diary.list.DiaryListPageSource
import com.devd.calenderbydw.utils.SafeNetCall
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

class DiaryRepository @Inject constructor(
    @NetworkModule.CalendarServer private val calendarService: CalendarService,
    private val diaryDao: DiaryDao
) : SafeNetCall() {

    suspend fun insertDiaryInfo(item: DiaryEntity) =
        diaryDao.insertDairy(item)

    fun getDiaryFlowList() = Pager(
        config = PagingConfig(
            pageSize = 10,
            enablePlaceholders = true,
        ), pagingSourceFactory = { DiaryListPageSource(diaryDao) }
    )

    suspend fun getDiaryStickers() = safeApiCall(Dispatchers.IO) {
        calendarService.getDiaryStickerList()
    }

}