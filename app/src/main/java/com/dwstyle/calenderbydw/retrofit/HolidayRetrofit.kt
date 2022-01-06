package com.dwstyle.calenderbydw.retrofit

import android.content.Context
import android.util.Log
import com.dwstyle.calenderbydw.R
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import org.jsoup.Jsoup
import org.jsoup.select.Elements
import org.w3c.dom.Document
import org.w3c.dom.Element
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import java.net.URL

class HolidayRetrofit {

    private val holidayRetro: Retrofit =Retrofit.Builder()
        .baseUrl("http://apis.data.go.kr")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val holidayRetroService: HolidayRetroService =holidayRetro.create(HolidayRetroService::class.java)

    suspend fun getHoliday(context:Context)= withContext(Dispatchers.IO){
        val bodyMap = HashMap<String,String>()
        bodyMap["ServiceKey"]=context.getString(R.string.encoding_key)
        bodyMap["solYear"]="2021"
        bodyMap["numOfRows"]="100"
        val check = URL("http://apis.data.go.kr/B090041/openapi/service/SpcdeInfoService/getRestDeInfo?serviceKey=${context.getString(R.string.encoding_key)}&solYear=2015")
        val jsoup =Jsoup.connect("http://apis.data.go.kr/B090041/openapi/service/SpcdeInfoService/getRestDeInfo?serviceKey=${context.getString(R.string.encoding_key)}&solYear=2015")
        val doc = jsoup.get()
        Log.d("도원","성공 , ${doc.toString()}")

        val elements: Elements = doc.select("body").select("items").select("item")
        Log.d("도원","성공 , ${elements.size}")

        for (ele in elements){
            ele.run {
                Log.d("도원","dateKind :  ${select("dateKind").eachText()[0]} \n dateName : ${select("dateName").eachText()[0]} \n isHoliday : ${select("isHoliday").eachText()[0]} \n" +
                        "locdate : ${select("locdate").eachText()[0]}")
            }
        }

//        holidayRetroService.getHoliday(context.getString(R.string.encoding_key),"2021").enqueue(object :Callback<ResponseBody>{
//
//            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
//                if (response.isSuccessful){
//
//                    Log.d("도원","성공 , ${response.body().toString()}")
//                }else{
//                    Log.d("도원","실패 ,${response.errorBody().toString()}")
//
//                }
//            }
//
//            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
//                Log.d("도원","실패 이유  ${t.localizedMessage}")
//            }
//        })

    }



    interface HolidayRetroService{
        @GET("/B090041/openapi/service/SpcdeInfoService/getRestDeInfo")
        fun getHoliday(@Query("serviceKey")serviceKey:String , @Query("solYear")solYear:String) : Call<ResponseBody>
    }
}