package com.devd.calenderbydw.repository

import com.devd.calenderbydw.data.local.dao.HolidayDao
import com.devd.calenderbydw.data.remote.CallResult
import com.devd.calenderbydw.data.remote.api.HolidayService
import com.devd.calenderbydw.data.remote.holiday.HolidayItem
import com.devd.calenderbydw.di.NetworkModule
import com.devd.calenderbydw.utils.SafeNetCall
import kotlinx.coroutines.Dispatchers
import timber.log.Timber
import javax.inject.Inject

class HolidayRepository @Inject constructor(
    @NetworkModule.HolidayServer private val holidayService: HolidayService,
    private val holidayDao: HolidayDao
) : SafeNetCall() {

    suspend fun getHolidayOfYear(serviceKey: String, year: Int,checkUpdate:Boolean,dbHolidaySize :Int) : List<HolidayItem> {
        safeApiCall(Dispatchers.IO) {
            holidayService.getHolidayOfMonth(serviceKey, year)
        }.run {
           return when (this) {
                is CallResult.Success -> {
                    if(checkUpdate){
                        if(dbHolidaySize != this.data.body.items.item.size){
                            holidayDao.insertHolidayItemList(this.data.body.items.item.map { it.toHolidayDbItem() })
                        }
                    }else{
                        holidayDao.insertHolidayItemList(this.data.body.items.item.map { it.toHolidayDbItem() })
                    }
                    this.data.body.items.item
                }
                else -> {
                    listOf()
                }
            }
        }
    }

    suspend fun getHolidayOfYearInDB(serviceKey: String, year: Int) : List<Any> {
        val holidayData = holidayDao.selectHolidayItemOfYear(year)
        return holidayData.ifEmpty {
            getHolidayOfYear(serviceKey, year, false, 0)
        }
    }

    suspend fun checkHolidayDBOfYear(year: Int)=holidayDao.selectHolidayItemOfYear(year).ifEmpty { null }

}