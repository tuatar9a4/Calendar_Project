package com.devd.calenderbydw

import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.Button
import android.widget.TextView
import androidx.core.view.InputDeviceCompat
import androidx.core.view.MotionEventCompat
import androidx.core.view.ViewConfigurationCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.wear.tiles.TileService
import androidx.wear.widget.WearableLinearLayoutManager
import androidx.wear.widget.WearableRecyclerView
import com.devd.calenderbydw.adapters.MyTaskAdapter
import com.devd.calenderbydw.database.TaskDatabase
import com.devd.calenderbydw.database.TaskDatabaseHelper
import com.devd.calenderbydw.item.CreateTaskData
import com.devd.calenderbydw.utils.Consts
import com.devd.calenderbydw.utils.SendSyncData
import com.devd.calenderbydw.utils.TaskRecyclerViewDecoration
import com.google.android.gms.wearable.Asset
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import java.io.File
import java.lang.Exception
import java.nio.file.Files
import java.time.Instant
import java.util.*
import java.util.concurrent.CancellationException
import kotlin.Comparator
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.HashSet
import kotlin.math.roundToInt

class MainActivity : Activity() {


    //    private lateinit var tileUiClient :TileUiClient
    private lateinit var rcTask: WearableRecyclerView
    private lateinit var btnPre: Button
    private lateinit var btnNext: Button
    private lateinit var btnCreate: Button
    private lateinit var tvTopTitle: TextView
    private lateinit var tvToday: TextView

    private lateinit var taskDatabase: TaskDatabase

    private val currentPlusSevenDate = ArrayList<String>()
    private val taskLists = HashMap<String, ArrayList<String>>()

    private lateinit var taskAdapter: MyTaskAdapter

    private final val settingMillis: String = "SETTINGMILLS"
    private var fromWidget = false;
    private lateinit var currentDate: DateTime
    private lateinit var toDayDateTime: DateTime

    private val dataClient by lazy { Wearable.getDataClient(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        TileService.getUpdater(applicationContext).requestUpdate(CalendarTile::class.java)
        if (intent.getStringExtra("widgetMonth") != null) {
            if (intent.getStringExtra("widgetMonth") == "fromWidget") {
                fromWidget = true
            }
        } else {
            fromWidget = false
        }
        var initMillis = 0L
        if (fromWidget) {
            val prss = applicationContext.getSharedPreferences("sharedData", MODE_PRIVATE)
            if (prss.getLong(settingMillis, 0) == 0L) {
                initMillis = System.currentTimeMillis()
            } else {
                initMillis = prss.getLong(settingMillis, 0)
            }
        } else {
            initMillis = System.currentTimeMillis()
        }
        toDayDateTime = DateTime(System.currentTimeMillis())
        initView()
        openTheDB()

        btnPre.setOnClickListener {
            currentDate = currentDate.minusWeeks(1)
            getTodayToSeven(currentDate.millis)
        }

        btnNext.setOnClickListener {
            currentDate = currentDate.plusWeeks(1)
            getTodayToSeven(currentDate.millis)
        }
        taskAdapter = MyTaskAdapter(this, currentPlusSevenDate, taskLists)
        rcTask.adapter = taskAdapter

        getTodayToSeven(initMillis)
        rcTask.addItemDecoration(TaskRecyclerViewDecoration(this))
        rcTask.apply {
            bezelFraction = 0.5f
            scrollDegreesPerScreen = 90f
        }

        rcTask.setOnGenericMotionListener(View.OnGenericMotionListener { view, ev ->
            if (ev.action == MotionEvent.ACTION_SCROLL &&
                ev.isFromSource(InputDeviceCompat.SOURCE_ROTARY_ENCODER)
            ) {
                val delta = -ev.getAxisValue(MotionEventCompat.AXIS_SCROLL) *
                        ViewConfigurationCompat.getScaledVerticalScrollFactor(
                            ViewConfiguration.get(applicationContext), applicationContext
                        )
//                // Swap these axes to scroll horizontally instead
//                view.scrollBy(0, delta.roundToInt()/2)
                rcTask.smoothScrollBy(0, delta.roundToInt())
                true
            } else {
                false
            }
        })

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
    fun initView() {
        rcTask = findViewById(R.id.rcTask)
        rcTask.requestFocus()
        rcTask.layoutManager = WearableLinearLayoutManager(this,
            object : WearableLinearLayoutManager.LayoutCallback() {
                private var progressToCenter: Float = 0f

                override fun onLayoutFinished(child: View?, parent: RecyclerView?) {
                    if (parent == null) {
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

        tvTopTitle = findViewById(R.id.tvTopTitle)
        tvToday = findViewById(R.id.tvToday)
        btnPre = findViewById(R.id.btnPre)
        btnNext = findViewById(R.id.btnNext)
        btnCreate = findViewById(R.id.btnCreate)
//        btnCreate.setOnClickListener {
//            val intent = Intent(applicationContext, CreateSimpleTaskActivity::class.java)
//            startActivityForResult(intent, Consts.REVICETASKINFOCODE)
//        }

    }

    fun openTheDB() {
        taskDatabase = TaskDatabase.buildDatabase(applicationContext)
//        dbHelper= TaskDatabaseHelper(applicationContext,"wearTask.db",null,3)
//        database=dbHelper.writableDatabase
////        dropTable("myTaskTbl")
//        dbHelper.onCreate(database)
    }

    //오늘 포함 7일 날짜 알기
    fun getTodayToSeven(initMillis: Long) {
        currentPlusSevenDate.clear()
        taskLists.clear()
        currentDate = DateTime(initMillis)
        //현재 시간에 해당하는 날짜 +7
        for (a in 0..6) {
            currentPlusSevenDate.add(currentDate.plusDays(a).toString("yyyy.MM.dd.E"))
//            getTaskFromDateVerWeek(currentDate.plusDays(a).toString("yyyy.MM.dd.E"))
        }

        CoroutineScope(Dispatchers.IO).launch {
            taskDatabase.taskDao().getAllTaskForWidget().let { taskDbList ->
                val currentDateString = currentDate.toString("yyyy.MM.dd.E").split(".")
                taskDbList.forEach {
                    //year.month.day = title&_id
                    when (it.repeatType) {
                        0 -> {// 반복 없음
                            if (it.year == currentDateString[0] && it.month == currentDateString[1]) {
                                val keyDate = "${it.year}." +
                                        "${settingMonthDay(it.month.toInt())}." +
                                        settingMonthDay(it.day.toInt())
                                taskLists[keyDate]?.add("${it.title}&${it.id}") ?: kotlin.run {
                                    taskLists[keyDate] = arrayListOf("${it.title}&${it.id}")
                                }
                            }
                        }

                        1 -> {// 매일
                            val keyDate = "${it.year}." +
                                    "${settingMonthDay(it.month.toInt())}." +
                                    settingMonthDay(it.day.toInt())
                            taskLists[keyDate]?.add("${it.title}&${it.id}") ?: kotlin.run {
                                taskLists[keyDate] = arrayListOf("${it.title}&${it.id}")
                            }
                        }

                        2 -> {// 매주
                            val keyDate = "${it.year}." +
                                    "${settingMonthDay(it.month.toInt())}." +
                                    settingMonthDay(it.day.toInt())
                            taskLists[keyDate]?.add("${it.title}&${it.id}") ?: kotlin.run {
                                taskLists[keyDate] = arrayListOf("${it.title}&${it.id}")
                            }
                        }

                        3 -> {// 매달
                            val keyDate = "${it.year}." +
                                    "${settingMonthDay(it.month.toInt())}." +
                                    settingMonthDay(it.day.toInt())
                            taskLists[keyDate]?.add("${it.title}&${it.id}") ?: kotlin.run {
                                taskLists[keyDate] = arrayListOf("${it.title}&${it.id}")
                            }
                        }

                        4 -> {// 매년
                            if (it.month == currentDateString[1]) {
                                val keyDate = "${it.year}." +
                                        "${settingMonthDay(it.month.toInt())}." +
                                        settingMonthDay(it.day.toInt())
                                taskLists[keyDate]?.add("${it.title}&${it.id}") ?: kotlin.run {
                                    taskLists[keyDate] = arrayListOf("${it.title}&${it.id}")
                                }
                            }
                        }
                    }
                }

//                getTaskFromDate(DateTime(initMillis).toString("yyyy.MM.dd.E"))

                taskLists.toSortedMap(Comparator { t, t2 -> if (t > t2) 1 else 2 })
                taskAdapter.setItems(currentPlusSevenDate, taskLists)
            }
        }
        tvTopTitle.text = "${currentDate.monthOfYear}월 ${
            currentDate.toCalendar(Locale.getDefault()).get(Calendar.WEEK_OF_MONTH)
        }주 중"

        tvToday.text =
            "today(${toDayDateTime.year}.${toDayDateTime.monthOfYear}.${toDayDateTime.dayOfMonth})"
    }

    //특정 날짜에 해당하는 Task 알기
    fun getTaskFromDate(dateStr: String) {

//        val dateSplit = dateStr.split(".")
//        getTaskRepeatY(dateSplit[0], dateSplit[1])
//        getTaskRepeatM(dateSplit[0], dateSplit[1])
//        getTaskRepeatN(dateSplit[0], dateSplit[1])
    }

    fun getTaskFromDateVerWeek(dateStr: String) {
//        getTaskRepeatW(dateStr)

    }

    fun settingMonthDay(str: Int) = if (str.toString().length == 1) "0${str}" else "${str}"

    //년도 반복에서 얻기
//    fun getTaskRepeatY(year: String, month: String) {
//        val corsor = database.rawQuery(
//            "SELECT month,day,title,_id FROM myTaskTbl WHERE repeatY==1 AND month == ${month}",
//            null
//        )
//        var tempTaskList = ArrayList<String>()
//        while (corsor.moveToNext()) {
//
//            if (taskLists["${year}.${settingMonthDay(corsor.getInt(0))}.${
//                    settingMonthDay(
//                        corsor.getInt(
//                            1
//                        )
//                    )
//                }"] == null
//            ) {
//                taskLists["${year}.${settingMonthDay(corsor.getInt(0))}.${
//                    settingMonthDay(
//                        corsor.getInt(
//                            1
//                        )
//                    )
//                }"] =
//                    arrayListOf<String>(corsor.getString(2) + "&" + corsor.getInt(3))
//            } else {
//                tempTaskList = taskLists["${year}.${settingMonthDay(corsor.getInt(0))}.${
//                    settingMonthDay(
//                        corsor.getInt(1)
//                    )
//                }"]!!
//                tempTaskList.add(corsor.getString(2) + "&" + corsor.getInt(3))
//                taskLists["${year}.${settingMonthDay(corsor.getInt(0))}.${
//                    settingMonthDay(
//                        corsor.getInt(
//                            1
//                        )
//                    )
//                }"] = tempTaskList
//            }
//        }
//    }

    //딜 반복에서 얻기
//    fun getTaskRepeatM(year: String, month: String) {
//        val corsor = database.rawQuery("SELECT day,title,_id FROM myTaskTbl WHERE repeatM==1", null)
//        var tempTaskList = ArrayList<String>()
//        while (corsor.moveToNext()) {
//            if (taskLists["${year}.${settingMonthDay(month.toInt())}.${
//                    settingMonthDay(
//                        corsor.getInt(
//                            0
//                        )
//                    )
//                }"] == null
//            ) {
//                taskLists["${year}.${settingMonthDay(month.toInt())}.${
//                    settingMonthDay(
//                        corsor.getInt(
//                            0
//                        )
//                    )
//                }"] =
//                    arrayListOf<String>(corsor.getString(1) + "&" + corsor.getInt(2))
//            } else {
//                tempTaskList = taskLists["${year}.${settingMonthDay(month.toInt())}.${
//                    settingMonthDay(
//                        corsor.getInt(0)
//                    )
//                }"]!!
//                tempTaskList.add(corsor.getString(1) + "&" + corsor.getInt(2))
//                taskLists["${year}.${settingMonthDay(month.toInt())}.${
//                    settingMonthDay(
//                        corsor.getInt(
//                            0
//                        )
//                    )
//                }"] = tempTaskList
//            }
//        }
//    }

    private var weekRepeat = HashSet<String>()

    //주 반복에서 얻기
//    fun getTaskRepeatW(weekStr: String) {
//        val corsor =
//            database.rawQuery("SELECT week,title,_id FROM myTaskTbl WHERE repeatW==1", null)
//        var tempTaskList = ArrayList<String>()
//        weekRepeat.clear()
//        val dateSplit = weekStr.split(".")
//        while (corsor.moveToNext()) {
//            checkWeekRepeat(corsor.getString(0).split("&"))
//            if (weekRepeat.contains(weekStrTransInt(dateSplit[3]).toString())) {
//                if (taskLists["${dateSplit[0]}.${settingMonthDay(dateSplit[1].toInt())}.${
//                        settingMonthDay(
//                            dateSplit[2].toInt()
//                        )
//                    }"] == null
//                ) {
//                    taskLists["${dateSplit[0]}.${settingMonthDay(dateSplit[1].toInt())}.${
//                        settingMonthDay(
//                            dateSplit[2].toInt()
//                        )
//                    }"] =
//                        arrayListOf<String>(corsor.getString(1) + "&" + corsor.getInt(2))
//                } else {
//                    tempTaskList =
//                        taskLists["${dateSplit[0]}.${settingMonthDay(dateSplit[1].toInt())}.${
//                            settingMonthDay(dateSplit[2].toInt())
//                        }"]!!
//                    tempTaskList.add(corsor.getString(1) + "&" + corsor.getInt(2))
//                    taskLists["${dateSplit[0]}.${settingMonthDay(dateSplit[1].toInt())}.${
//                        settingMonthDay(
//                            dateSplit[2].toInt()
//                        )
//                    }"] = tempTaskList
//                }
//            }
//        }
//
//    }

    fun checkWeekRepeat(weekStr: List<String>) {
        for (a in 0..weekStr.size) {
            when (a) {
                0 -> if (weekStr[a].equals("1")) weekRepeat.add("7") //SUN
                1 -> if (weekStr[a].equals("1")) weekRepeat.add("1") //MON
                2 -> if (weekStr[a].equals("1")) weekRepeat.add("2") //TUE
                3 -> if (weekStr[a].equals("1")) weekRepeat.add("3") //WEN
                4 -> if (weekStr[a].equals("1")) weekRepeat.add("4") //THU
                5 -> if (weekStr[a].equals("1")) weekRepeat.add("5") //FRI
                6 -> if (weekStr[a].equals("1")) weekRepeat.add("6") //SAT
            }
        }
    }

    fun weekStrTransInt(weekStr: String): Int {
        return when (weekStr) {
            "Sun" -> 1//SUN
            "일" -> 1//SUN
            "Mon" -> 2 //MON
            "월" -> 2
            "Tue" -> 3 //Tue
            "화" -> 3 //Tue
            "Wen" -> 4 //WEN
            "수" -> 4 //WEN
            "Thu" -> 5 //THU
            "목" -> 5 //THU
            "Fri" -> 6 //FRI
            "금" -> 6 //FRI
            "Sat" -> 7 //SAT
            "토" -> 7 //SAT
            else -> 0
        }

    }

    //반복 없음에서 얻기
    fun getTaskRepeatN(year: String, month: String) {
//        val corsor = database.rawQuery(
//            "SELECT day,text,title,_id FROM myTaskTbl WHERE repeatN==1 AND year == ${year} AND month == ${month}",
//            null
//        )
//        var tempTaskList = ArrayList<String>()
//        while (corsor.moveToNext()) {
//            if (taskLists["${year}.${settingMonthDay(month.toInt())}.${
//                    settingMonthDay(
//                        corsor.getInt(
//                            0
//                        )
//                    )
//                }"] == null
//            ) {
//                taskLists["${year}.${settingMonthDay(month.toInt())}.${
//                    settingMonthDay(
//                        corsor.getInt(
//                            0
//                        )
//                    )
//                }"] =
//                    arrayListOf<String>(corsor.getString(2) + "&" + corsor.getInt(3))
//            } else {
//                tempTaskList = taskLists["${year}.${settingMonthDay(month.toInt())}.${
//                    settingMonthDay(
//                        corsor.getInt(0)
//                    )
//                }"]!!
//                tempTaskList.add(corsor.getString(2) + "&" + corsor.getInt(3))
//                taskLists["${year}.${settingMonthDay(month.toInt())}.${
//                    settingMonthDay(
//                        corsor.getInt(
//                            0
//                        )
//                    )
//                }"] = tempTaskList
//            }
//        }
    }


    override fun onResume() {
        super.onResume()
        Log.d("도원", "WearApp onResume ")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == Consts.REVICETASKINFOCODE) {

                Log.d(
                    "도원",
                    "데이터 ${data?.getParcelableExtra<CreateTaskData>(Consts.REVICETASKINFO)}"
                )
                data?.let {
//                    TaskDatabaseHelper.createTask(
//                        (it.getParcelableExtra<CreateTaskData>(Consts.REVICETASKINFO) as CreateTaskData),
//                        dbHelper.readableDatabase
//                    )
                    SendSyncData.changeDBToBytes(applicationContext, dataClient)
//                    changeDBToBytes()
                    getTodayToSeven((it.getParcelableExtra<CreateTaskData>(Consts.REVICETASKINFO) as CreateTaskData).time)
                }
            }

        }
    }

    //데이터 전송하기 전에 DB를 byteArray형태로 변경
    private fun changeDBToBytes() {
        //DB 경로를 구한 한다.
        val dbPath =
            TaskDatabaseHelper(applicationContext, "wearTask.db", null, 3).readableDatabase.path
        val dbFile = File(dbPath)
        val dbUri = Uri.fromFile(dbFile)
//        val realAsset = Asset.createFromUri(dbUri)
        val bytesFromDB = Files.readAllBytes(dbFile.toPath())
        val realAsset = Asset.createFromBytes(bytesFromDB)
        sendDBData(realAsset, dbPath)
    }


    //Asset 으로 만든 데이터 보내기
    private fun sendDBData(sendData: Asset, dBPtah: String) {
//        val dataMap : PutDataMapRequest = PutDataMapRequest.create("/taskdata")
//        dataMap.dataMap.putAsset("taskDB",sendData)
//        dataMap.dataMap.putString("taskDBPath",dBPtah)
//        Log.d("도원","dBPtah :  ${dBPtah}")
//        val request : PutDataRequest= dataMap.asPutDataRequest()
//        request.setUrgent()
//        val putTask : Task<DataItem> =Wearable.getDataClient(this).putDataItem(request)
        try {
            val putDataReq = PutDataMapRequest.create("/taskdata").apply {
                this.dataMap.putAsset("taskDB", sendData)
                this.dataMap.putString("taskDBPath", dBPtah)
                this.dataMap.putLong("KEY", Instant.now().epochSecond)

            }
                .asPutDataRequest()
                .setUrgent()

            val result = dataClient.putDataItem(putDataReq)

            Log.d("도원", "putDataReq.uri: $putDataReq.uri")
            Log.d("도원", "DataItem saved: $result")
        } catch (cancellationException: CancellationException) {
            Log.d("도원", "Saving DataItem failed: ${cancellationException.localizedMessage}")
            throw cancellationException
        } catch (exception: Exception) {
            Log.d("도원", "Saving DataItem failed: $exception")
        }
    }

}