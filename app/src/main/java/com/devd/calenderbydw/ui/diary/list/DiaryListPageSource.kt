package com.devd.calenderbydw.ui.diary.list

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.devd.calenderbydw.data.local.dao.DiaryDao
import com.devd.calenderbydw.data.local.entity.DiaryEntity
import timber.log.Timber

class DiaryListPageSource(private val diaryDao: DiaryDao) : PagingSource<Int, DiaryEntity>() {

    private companion object{
        const val INIT_PAGE_INDEX =0
    }

    override fun getRefreshKey(state: PagingState<Int, DiaryEntity>): Int? {
        val dd = state.anchorPosition?.let { achorPosition ->
            state.closestPageToPosition(achorPosition)?.prevKey?.plus(1)
                ?:state.closestPageToPosition(achorPosition)?.nextKey?.minus(1)
        }
        Timber.d("loadData getRefreshKey: ${dd}")
        return dd
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, DiaryEntity> {
        val position = params.key ?: INIT_PAGE_INDEX
        Timber.d("loadData : ${position},${params.loadSize}")
        val loadData  = diaryDao.getDiaryDataList(position,10)
        Timber.d("loadData : ${loadData}")
        return LoadResult.Page(
            data = loadData,
            prevKey = if(position == INIT_PAGE_INDEX) null else position-1,
            nextKey = if(loadData.isEmpty()) null else position+1
        )
    }
}