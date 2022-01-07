package com.dwstyle.calenderbydw.retrofit

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.dwstyle.calenderbydw.R
import com.dwstyle.calenderbydw.item.HolidayItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import org.jsoup.Jsoup
import org.jsoup.select.Elements
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.net.URL

class HolidayRetrofit {

    private val holidayRetro: Retrofit =Retrofit.Builder()
        .baseUrl("http://apis.data.go.kr")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val holidayRetroService: HolidayRetroService =holidayRetro.create(HolidayRetroService::class.java)

    private val holidayLiveData :MutableLiveData<ArrayList<HolidayItem>> =MutableLiveData()
    private val holidayListItems =ArrayList<HolidayItem>()
    suspend fun getHoliday(context:Context,year :String)= withContext(Dispatchers.IO){
        val bodyMap = HashMap<String,String>()
        bodyMap["ServiceKey"]=context.getString(R.string.encoding_key)
        bodyMap["solYear"]="2021"
        bodyMap["numOfRows"]="100"
        val check = URL("http://apis.data.go.kr/B090041/openapi/service/SpcdeInfoService/getRestDeInfo?serviceKey=${context.getString(R.string.encoding_key)}&solYear=2015")
        val jsoup =Jsoup.connect("http://apis.data.go.kr/B090041/openapi/service/SpcdeInfoService/getRestDeInfo?serviceKey=${context.getString(R.string.encoding_key)}&solYear=${year}&numOfRows=100")
        val doc = jsoup.get()
//        Log.d("도원","성공 , ${doc.toString()}")

        val elements: Elements = doc.select("body").select("items").select("item")
        Log.d("도원","${year}_성공 , ${elements.size}")
        holidayListItems.clear()
        for (ele in elements){
            ele.run {
//                Log.d("도원","HolidayItem <= dateKind :  ${select("dateKind").eachText()[0]} \n dateName : ${select("dateName").eachText()[0]} \n isHoliday : ${select("isHoliday").eachText()[0]} \n" +
//                        "locdate : ${select("locdate").eachText()[0]}")
//                val temp =HolidayItem(1,2,3,4,"")
                val holidayDate =select("locdate").eachText()[0]
//                Log.d("도원","HolidayItem => year : ${holidayDate.substring(0,4)} | month : ${holidayDate.substring(4,6).toInt()} | day ${holidayDate.substring(6).toInt()}")
                val temp =HolidayItem(holidayDate.substring(0,4).toInt(),holidayDate.substring(4,6).toInt(),holidayDate.substring(6).toInt(),
                    (if (select("isHoliday").eachText()[0]=="Y") 1 else 0),select("dateName").eachText()[0])
                holidayListItems.add(temp)

            }
        }
        holidayLiveData.postValue(holidayListItems)

    }


    fun getHolidayItems() : MutableLiveData<ArrayList<HolidayItem>>{
        return holidayLiveData
    }



    interface HolidayRetroService{
        @GET("/B090041/openapi/service/SpcdeInfoService/getRestDeInfo")
        fun getHoliday(@Query("serviceKey")serviceKey:String , @Query("solYear")solYear:String) : Call<ResponseBody>
    }
}