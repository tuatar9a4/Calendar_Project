package com.dwstyle.calenderbydw

import android.app.Activity
import android.content.SharedPreferences
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.wear.widget.WearableLinearLayoutManager
import androidx.wear.widget.WearableRecyclerView
import com.dwstyle.calenderbydw.adapters.MyTaskAdapter
import com.dwstyle.calenderbydw.database.TaskDatabaseHelper
import com.dwstyle.calenderbydw.utils.TaskRecyclerViewDecoration
import org.joda.time.DateTime
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.HashSet

class MainActivity : Activity() {


//    private lateinit var tileUiClient :TileUiClient
    private lateinit var rcTask : WearableRecyclerView
    private lateinit var btnPre : Button
    private lateinit var btnNext : Button
    private lateinit var tvTopTitle :TextView

    private lateinit var dbHelper: TaskDatabaseHelper
    private lateinit var database: SQLiteDatabase

    private val currentPlusSevenDate = ArrayList<String>()
    private val taskLists = HashMap<String,ArrayList<String>>()

    private lateinit var taskAdapter :MyTaskAdapter

    private final val settingMillis : String ="SETTINGMILLS"
    private var fromWidget =false;
    private lateinit var currentDate :DateTime
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d("도원","WearApp Open ")
        if (intent.getStringExtra("widgetMonth")!=null){
            if (intent.getStringExtra("widgetMonth")=="fromWidget"){
                fromWidget=true
            }
        }else{
            fromWidget=false
        }
        var initMillis=0L
        if (fromWidget){
            val prss=applicationContext.getSharedPreferences("sharedData", MODE_PRIVATE)
            if (prss.getLong(settingMillis,0)==0L){
                initMillis=System.currentTimeMillis()
            }else{
                initMillis=prss.getLong(settingMillis,0)
            }
        }else{
            initMillis=System.currentTimeMillis()
        }
        initView()
        openTheDB()

        btnPre.setOnClickListener {
            currentDate=currentDate.minusWeeks(1)
            getTodayToSeven(currentDate.millis)
        }

        btnNext.setOnClickListener {
            currentDate=currentDate.plusWeeks(1)
            getTodayToSeven(currentDate.millis)
        }
        taskAdapter=MyTaskAdapter(this,currentPlusSevenDate,taskLists)
        rcTask.adapter=taskAdapter

        getTodayToSeven(initMillis)
        rcTask.addItemDecoration(TaskRecyclerViewDecoration(this))
        rcTask.apply {
            isCircularScrollingGestureEnabled = true
            bezelFraction = 0.5f
            scrollDegreesPerScreen = 90f
        }
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
        rcTask.layoutManager=WearableLinearLayoutManager(this,object  :WearableLinearLayoutManager.LayoutCallback(){
            private var progressToCenter: Float = 0f

            override fun onLayoutFinished(child: View?, parent: RecyclerView?) {
                if (parent==null){
                    return
                }
                child?.apply {
                    // Figure out % progress from top to bottom
                    val centerOffset = height.toFloat() / 2.0f / parent.height.toFloat()
                    val yRelativeToCenterOffset = y / parent.height + centerOffset

                    // Normalize for center
                    progressToCenter = Math.abs(0.5f - yRelativeToCenterOffset)
                    // Adjust to the maximum scale
                    progressToCenter = Math.min(progressToCenter, 0.65f)

                    scaleX = 1 - progressToCenter
                    scaleY = 1 - progressToCenter

                }
            }


        })

        tvTopTitle=findViewById(R.id.tvTopTitle)
        btnPre=findViewById(R.id.btnPre)
        btnNext=findViewById(R.id.btnNext)

    }

    fun openTheDB(){
        dbHelper= TaskDatabaseHelper(applicationContext,"wearTask.db",null,3)
        database=dbHelper.readableDatabase
    }

    //오늘 포함 7일 날짜 알기
    fun getTodayToSeven(initMillis : Long){
        currentPlusSevenDate.clear()
        taskLists.clear()
        currentDate =DateTime(initMillis)
        Log.d("도원","WEEK_OF_MONTH : ${currentDate.monthOfYear()}월 ${currentDate.toCalendar(Locale.getDefault()).get(Calendar.WEEK_OF_MONTH)}주")
        //현재 시간에 해당하는 날짜 +7
        currentPlusSevenDate.add(currentDate.toString("yyyy.MM.dd.E"))
        getTaskFromDateVerWeek(currentDate.toString("yyyy.MM.dd.E"))
        for (a in 1..6){
            currentPlusSevenDate.add(currentDate.plusDays(a).toString("yyyy.MM.dd.E"))
            getTaskFromDateVerWeek(currentDate.plusDays(a).toString("yyyy.MM.dd.E"))
        }

        getTaskFromDate(DateTime(initMillis).toString("yyyy.MM.dd.E"))

        taskLists.toSortedMap(Comparator { t, t2 -> if(t>t2) 1 else 2 })
        Log.d("도원","currentPlusSevenDate : ${currentPlusSevenDate}")
        Log.d("도원","taskLists : ${taskLists}")
        taskAdapter.setItems(currentPlusSevenDate,taskLists)
        tvTopTitle.text="${currentDate.monthOfYear}월 ${currentDate.toCalendar(Locale.getDefault()).get(Calendar.WEEK_OF_MONTH)}주 중"

    }

    //특정 날짜에 해당하는 Task 알기
    fun getTaskFromDate(dateStr :String){

        val dateSplit = dateStr.split(".")
        getTaskRepeatY(dateSplit[0],dateSplit[1])
        getTaskRepeatM(dateSplit[0],dateSplit[1])
        getTaskRepeatN(dateSplit[0],dateSplit[1])
    }

    fun getTaskFromDateVerWeek(dateStr :String){
        getTaskRepeatW(dateStr)

    }

    //년도 반복에서 얻기
    fun getTaskRepeatY(year:String,month:String){
        val corsor =database.rawQuery("SELECT month,day,text FROM myTaskTbl WHERE RepeatY==1 AND month == ${month}",null)
        var tempTaskList = ArrayList<String>()
        while (corsor.moveToNext()){
            if (taskLists["${year}.${corsor.getInt(0)}.${corsor.getInt(1)}"]==null){
                taskLists["${year}.${corsor.getInt(0)}.${corsor.getInt(1)}"]=
                    arrayListOf<String>(corsor.getString(2))
            }else{
                tempTaskList=taskLists["${year}.${corsor.getInt(0)}.${corsor.getInt(1)}"]!!
                tempTaskList.add(corsor.getString(2))
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
                    arrayListOf<String>(corsor.getString(1))
            }else{
                tempTaskList=taskLists["${year}.${month}.${corsor.getInt(0)}"]!!
                tempTaskList.add(corsor.getString(1))
                taskLists["${year}.${month}.${corsor.getInt(0)}"]=tempTaskList
            }
        }
    }

    private var weekRepeat = HashSet<String>()
    //주 반복에서 얻기
    fun getTaskRepeatW(weekStr :String){
        val corsor =database.rawQuery("SELECT week,text FROM myTaskTbl WHERE RepeatW==1",null)
        var tempTaskList = ArrayList<String>()
        weekRepeat.clear()
        val dateSplit = weekStr.split(".")
        weekStrTransInt(dateSplit[3])
        while (corsor.moveToNext()){
            checkWeekRepeat(corsor.getString(0).split("&"))
            if (weekRepeat.contains(weekStrTransInt(dateSplit[3]).toString())){
                if (taskLists["${dateSplit[0]}.${dateSplit[1]}.${dateSplit[2]}"]==null){
                    taskLists["${dateSplit[0]}.${dateSplit[1]}.${dateSplit[2]}"]=
                        arrayListOf<String>(corsor.getString(1))
                }else{
                    tempTaskList=taskLists["${dateSplit[0]}.${dateSplit[1]}.${dateSplit[2]}"]!!
                    tempTaskList.add(corsor.getString(1))
                    taskLists["${dateSplit[0]}.${dateSplit[1]}.${dateSplit[2]}"]=tempTaskList
                }
            }
        }

    }

    fun checkWeekRepeat(weekStr : List<String>){
        for ( a in 0..weekStr.size){
            when(a){
                0-> if (weekStr[a].equals("1")) weekRepeat.add("7") //SUN
                1-> if (weekStr[a].equals("1")) weekRepeat.add("1") //MON
                2-> if (weekStr[a].equals("1")) weekRepeat.add("2") //TUE
                3-> if (weekStr[a].equals("1")) weekRepeat.add("3") //WEN
                4-> if (weekStr[a].equals("1")) weekRepeat.add("4") //THU
                5-> if (weekStr[a].equals("1")) weekRepeat.add("5") //FRI
                6-> if (weekStr[a].equals("1")) weekRepeat.add("6") //SAT
            }
        }
    }

    fun weekStrTransInt(weekStr : String) :Int{
        return when(weekStr){
            "Sun"-> 7//SUN
            "일"  -> 7//SUN
            "Mon"-> 1 //MON
            "월" -> 1
            "Tue"-> 2 //Tue
            "화"-> 2 //Tue
            "Wen"-> 3 //WEN
            "수"-> 3 //WEN
            "Thu"-> 4 //THU
            "목"-> 4 //THU
            "Fri"-> 5 //FRI
            "금"-> 5 //FRI
            "Sat"-> 6 //SAT
            "토"-> 6 //SAT
            else -> 0
        }

    }

    //반복 없음에서 얻기
    fun getTaskRepeatN(year:String,month:String){
        val corsor =database.rawQuery("SELECT day,text,title FROM myTaskTbl WHERE RepeatN==1 AND year == ${year} AND month == ${month}",null)
        var tempTaskList = ArrayList<String>()
        while (corsor.moveToNext()){
            if (taskLists["${year}.${month}.${corsor.getInt(0)}"]==null){
                taskLists["${year}.${month}.${corsor.getInt(0)}"]=
                    arrayListOf<String>(corsor.getString(2))
            }else{
                tempTaskList=taskLists["${year}.${month}.${corsor.getInt(0)}"]!!
                tempTaskList.add(corsor.getString(2))
                taskLists["${year}.${month}.${corsor.getInt(0)}"]=tempTaskList
            }
        }
    }



    override fun onResume() {
        super.onResume()
        Log.d("도원","WearApp onResume ")
    }

}