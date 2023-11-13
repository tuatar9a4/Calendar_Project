package com.devd.calenderbydw.retrofit

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.devd.calenderbydw.R
import com.devd.calenderbydw.item.HolidayItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.select.Elements

class HolidayRetrofit {

//    private val holidayRetro: Retrofit =Retrofit.Builder()
//        .baseUrl("http://apis.data.go.kr")
//        .addConverterFactory(GsonConverterFactory.create())
//        .build()

//    private val holidayRetroService: HolidayRetroService =holidayRetro.create(HolidayRetroService::class.java)

    private val holidayLiveData :MutableLiveData<ArrayList<HolidayItem>> =MutableLiveData()
    private val holidayListItems =ArrayList<HolidayItem>()
    suspend fun getHoliday(context:Context,year :String)= withContext(Dispatchers.IO){
        val jsoup =Jsoup.connect("http://apis.data.go.kr/B090041/openapi/service/SpcdeInfoService/getRestDeInfo?serviceKey=${context.getString(R.string.encoding_key)}&solYear=${year}&numOfRows=100")
        val doc = jsoup.get()

        val elements: Elements = doc.select("body").select("items").select("item")
        synchronized(holidayListItems){
            holidayListItems.clear()
            for (ele in elements){
                ele.run {
                    val holidayDate =select("locdate").eachText()[0]
                    val temp =HolidayItem(holidayDate.substring(0,4).toInt(),holidayDate.substring(4,6).toInt(),holidayDate.substring(6).toInt(),
                        (if (select("isHoliday").eachText()[0]=="Y") 1 else 0),select("dateName").eachText()[0])
                    holidayListItems.add(temp)
                }
            }
            holidayLiveData.postValue(holidayListItems)
        }
    }


    fun getHolidayItems() : MutableLiveData<ArrayList<HolidayItem>>{
        return holidayLiveData
    }



//    interface HolidayRetroService{
//        @GET("/B090041/openapi/service/SpcdeInfoService/getRestDeInfo")
//        fun getHoliday(@Query("serviceKey")serviceKey:String , @Query("solYear")solYear:String) : Call<ResponseBody>
//    }
}