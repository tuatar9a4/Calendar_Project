package com.dwstyle.calenderbydw

import android.app.Activity
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.wear.widget.WearableLinearLayoutManager
import androidx.wear.widget.WearableRecyclerView
import com.dwstyle.calenderbydw.adapters.MyTaskAdapter
import com.dwstyle.calenderbydw.database.TaskDatabaseHelper
import com.dwstyle.calenderbydw.databinding.ActivityMainBinding
import com.dwstyle.calenderbydw.utils.ReceiveDataToFile
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import java.io.*
import java.lang.Exception
import java.util.concurrent.*

class MainActivity : Activity() {


//    private lateinit var tileUiClient :TileUiClient
    private lateinit var rcTask : RecyclerView

    private lateinit var dbHelper: TaskDatabaseHelper
    private lateinit var database: SQLiteDatabase

    private val currentPlusSevenDate = ArrayList<String>()
    private val taskLists = HashMap<String,ArrayList<String>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d("도원","WearApp Open ")
        initView()
        openTheDB()


        val menuItem = ArrayList<String>()
        menuItem.add("1")
        menuItem.add("22")
        menuItem.add("333")
        menuItem.add("4444")
        rcTask.adapter=MyTaskAdapter(applicationContext,menuItem)

//        타일 새로 고침
//        TileService.getUpdater(applicationContext).requestUpdate(CalendarTile::class.java)
//        val rootLayout = findViewById<FrameLayout>(R.id.tile_container)
//        tileUiClient = TileUiClient(
//            context = this,
//            component = ComponentName(this,CalendarTile::class.java),
//            parentView = rootLayout
//        )
//        tileUiClient.connect()

    }
    //view 들 초기화
    fun initView(){
        rcTask=findViewById(R.id.rcTask)
        rcTask.layoutManager=LinearLayoutManager(this)

    }

    fun openTheDB(){
        dbHelper= TaskDatabaseHelper(applicationContext,"wearTask.db",null,1)
        database=dbHelper.readableDatabase
    }

    //오늘 포함 7일 날짜 알기
    fun getTodayToSeven(){
        currentPlusSevenDate.clear()
        taskLists.clear()
        //현재 시간에 해당하는 날짜 +7
        currentPlusSevenDate.add(DateTime(System.currentTimeMillis()).toString("yyyy.MM.dd"))
        getTaskFromDateVerWeek(DateTime(System.currentTimeMillis()).toString("yyyy.MM.dd.E"))
        for (a in 1..6){
            currentPlusSevenDate.add(DateTime(System.currentTimeMillis()).plusDays(a).toString("yyyy.MM.dd"))
        }

        getTaskFromDate(DateTime(System.currentTimeMillis()).toString("yyyy.MM.dd.E"))
    }

    //특정 날짜에 해당하는 Task 알기
    fun getTaskFromDate(dateStr :String){

        val dateSplit = dateStr.split(".")
        getTaskRepeatY(dateSplit[0],dateSplit[1])
        getTaskRepeatM(dateSplit[0],dateSplit[1])
        getTaskRepeatN(dateSplit[0],dateSplit[1])
    }

    fun getTaskFromDateVerWeek(dateStr :String){
        val dateSplit = dateStr.split(".")
        getTaskRepeatW(dateStr)

    }

    //년도 반복에서 얻기
    fun getTaskRepeatY(year:String,month:String){
        val corsor =database.rawQuery("SELECT month,day,text FROM myTaskTbl WHERE RepeatY==1 AND month == ${month}",null)
        var tempTaskList = ArrayList<String>()
        while (corsor.moveToNext()){
            if (taskLists["${year}.${corsor.getInt(0)}.${corsor.getInt(1)}"]==null){
                taskLists["${year}.${corsor.getInt(0)}.${corsor.getInt(1)}"]=
                    arrayListOf<String>("${corsor.getInt(3)}")
            }else{
                tempTaskList=taskLists["${year}.${corsor.getInt(0)}.${corsor.getInt(1)}"]!!
                tempTaskList.add("${corsor.getInt(2)}")
                taskLists["${year}.${corsor.getInt(0)}.${corsor.getInt(1)}"]=tempTaskList
            }
        }
    }

    //딜 반복에서 얻기
    fun getTaskRepeatM(year:String,month:String){
        val corsor =database.rawQuery("SELECT day,text FROM myTaskTbl WHERE RepeatM==1",null)
        var tempTaskList = ArrayList<String>()
        while (corsor.moveToNext()){
            if (taskLists["${year}.${month}.${corsor.getInt(0)}"]==null){
                taskLists["${year}.${month}.${corsor.getInt(0)}"]=
                    arrayListOf<String>("${corsor.getInt(1)}")
            }else{
                tempTaskList=taskLists["${year}.${month}.${corsor.getInt(0)}"]!!
                tempTaskList.add("${corsor.getInt(1)}")
                taskLists["${year}.${month}.${corsor.getInt(0)}"]=tempTaskList
            }
        }
    }
    //주 반복에서 얻기
    fun getTaskRepeatW(weekStr :String){
        val corsor =database.rawQuery("SELECT day,text FROM myTaskTbl WHERE RepeatW==1",null)
        var tempTaskList = ArrayList<String>()
        while (corsor.moveToNext()){
            if (taskLists["${weekStr[0]}.${weekStr[1]}.${weekStr[2]}"]==null){
                taskLists["${weekStr[0]}.${weekStr[1]}.${weekStr[2]}"]=
                    arrayListOf<String>("${corsor.getInt(1)}")
            }else{
                tempTaskList=taskLists["${weekStr[0]}.${weekStr[1]}.${weekStr[2]}"]!!
                tempTaskList.add("${corsor.getInt(1)}")
                taskLists["${weekStr[0]}.${weekStr[1]}.${weekStr[2]}"]=tempTaskList
            }
        }

    }
    //반복 없음에서 얻기
    fun getTaskRepeatN(year:String,month:String){
        val corsor =database.rawQuery("SELECT day,text FROM myTaskTbl WHERE RepeatN==1 AND year == ${year} AND month == ${month}",null)
        var tempTaskList = ArrayList<String>()
        while (corsor.moveToNext()){
            if (taskLists["${year}.${month}.${corsor.getInt(0)}"]==null){
                taskLists["${year}.${month}.${corsor.getInt(0)}"]=
                    arrayListOf<String>("${corsor.getInt(1)}")
            }else{
                tempTaskList=taskLists["${year}.${month}.${corsor.getInt(0)}"]!!
                tempTaskList.add("${corsor.getInt(1)}")
                taskLists["${year}.${month}.${corsor.getInt(0)}"]=tempTaskList
            }
        }
    }


    override fun onResume() {
        super.onResume()
        Log.d("도원","WearApp onResume ")
    }

}