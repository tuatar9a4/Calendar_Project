package com.dwstyle.calenderbydw

import android.app.Activity
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dwstyle.calenderbydw.adapters.MyTaskAdapter
import com.dwstyle.calenderbydw.database.TaskDatabaseHelper
import com.dwstyle.calenderbydw.utils.TaskRecyclerViewDecoration
import org.joda.time.DateTime

class MainActivity : Activity() {


//    private lateinit var tileUiClient :TileUiClient
    private lateinit var rcTask : RecyclerView

    private lateinit var dbHelper: TaskDatabaseHelper
    private lateinit var database: SQLiteDatabase

    private val currentPlusSevenDate = ArrayList<String>()
    private val taskLists = HashMap<String,ArrayList<String>>()

    private lateinit var taskAdapter :MyTaskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d("도원","WearApp Open ")
        initView()
        openTheDB()
        getTodayToSeven()

        taskAdapter=MyTaskAdapter(this,currentPlusSevenDate,taskLists)
        rcTask.adapter=taskAdapter

        rcTask.addItemDecoration(TaskRecyclerViewDecoration(this))
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
        dbHelper= TaskDatabaseHelper(applicationContext,"wearTask.db",null,2)
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
            getTaskFromDateVerWeek(DateTime(System.currentTimeMillis()).plusDays(a).toString("yyyy.MM.dd.E"))
        }

        getTaskFromDate(DateTime(System.currentTimeMillis()).toString("yyyy.MM.dd.E"))

        taskLists.toSortedMap(Comparator { t, t2 -> if(t>t2) 1 else 2 })
        Log.d("도원","currentPlusSevenDate : ${currentPlusSevenDate}")
        Log.d("도원","taskLists : ${taskLists}")

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
        when(weekStr){
            "Sun"-> return 7//SUN
            "Mon"-> return 1 //MON
            "Tue"-> return 2 //Tue
            "Wen"-> return 3 //WEN
            "Thu"-> return 4 //THU
            "Fri"-> return 5 //FRI
            "Sat"-> return 6 //SAT
            else -> return 0
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