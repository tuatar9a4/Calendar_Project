package com.devd.calenderbydw.ui.calendar

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devd.calenderbydw.CreateTaskActivity
import com.devd.calenderbydw.R
import com.devd.calenderbydw.adapters.DailyTaskAdapter
import com.devd.calenderbydw.calendardacorator.*
import com.devd.calenderbydw.database.TaskDatabaseHelper
import com.devd.calenderbydw.databinding.FragmentCalendarBinding
import com.devd.calenderbydw.item.*
import com.devd.calenderbydw.retrofit.HolidayRetrofit
import com.devd.calenderbydw.utils.CustomAlertDialog
import com.devd.calenderbydw.utils.ShowTaskDialog
import com.devd.calenderbydw.utils.WidgetUtils
import com.devd.calenderbydw.utils.autoCleared
import com.google.android.gms.wearable.Wearable
import com.jakewharton.threetenabp.AndroidThreeTen
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener
import com.prolificinteractive.materialcalendarview.OnMonthChangedListener
import com.prolificinteractive.materialcalendarview.format.TitleFormatter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.Date
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.HashSet

class CalendarFragment : Fragment() {

    private var binding by autoCleared<FragmentCalendarBinding>()

    private lateinit var selectedDate : CalendarDay
    private val monthlyTaskList : ArrayList<TaskItem> =ArrayList<TaskItem>()
    private val yearRepeatTaskList : ArrayList<YearRepeatTaskItem> =ArrayList()
    private val monthRepeatTaskList : ArrayList<MonthRepeatTaskItem> =ArrayList()
    private val weekRepeatTaskList : ArrayList<WeekRepeatTaskItem> =ArrayList()
    private val noRepeatTaskList : ArrayList<NoRepeatTaskItem> =ArrayList()

    private val finishSearch = MutableLiveData<Boolean>()
    private val dailyTaskList :ArrayList<TaskItem> = ArrayList()
    private lateinit var dailyTaskAdapter: DailyTaskAdapter

    //년도 반복 ,달마다 반복, 주마다 반복, 반복 없음 task , 공휴일
    private val dayOfRepeatYear=HashSet<String>()
    private val dayOfRepeatMonth=HashSet<String>()
    private val dayOfRepeatWeek=HashSet<String>()
    private val dayOfRepeatNo=HashSet<String>()
    private val holidayOfDay=HashMap<String,String>()
    private var currentY=0;
    private var oldY=0;
    private var justDb=false;


    private lateinit var dbHelper : TaskDatabaseHelper
    private lateinit var database : SQLiteDatabase

    //수정시 수정 후 돌아올때
    private lateinit var resultLauncher : ActivityResultLauncher<Intent>

    private val holidayRetrofit : HolidayRetrofit = HolidayRetrofit()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState !=null){
            selectedDate= savedInstanceState.getParcelable<CalendarDay>("calendarDate")!!
        }else{
            selectedDate=CalendarDay.today()
            currentY=selectedDate.year
            oldY=selectedDate.year
        }
//        Log.d("도원","selectedDate1  : ${selectedDate.month}.${selectedDate.day}");
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCalendarBinding.inflate(inflater,container,false)
        AndroidThreeTen.init(context)
        initView()
        dbHelper= TaskDatabaseHelper(context,"task.db",null,3)
//        dbHelper.createMonthTBL("myTaskTbl")
        database=dbHelper.writableDatabase
        dbHelper.onUpgrade(database,database.version,3)
//        dropTable(view,"myTaskTbl");
//        dbHelper.onCreate(database)
//        dropTable(view,"y2022");
        bindData()

        var check =false;
        for (a in selectedDate.year-1..selectedDate.year+1){
            if (!TaskDatabaseHelper.isExistsTable(dbHelper.readableDatabase,"holiday${a}Tbl")){
                checkHolidayForCreateDB(a,requireContext())
                check=true;
            }
        }
        if (!check){
            initCalendarSetting()
        }
        return binding.root
    }

    private fun bindData(){
        holidayRetrofit.getHolidayItems().observe(viewLifecycleOwner
        ) { t: ArrayList<HolidayItem> ->
            synchronized(t) {
                if (t.size > 0) {
                    if (!TaskDatabaseHelper.isExistsTable(
                            dbHelper.readableDatabase,
                            "holiday${t[0].year}Tbl"
                        )
                    ) {
                        dbHelper.createHolidayTbl(
                            dbHelper.writableDatabase,
                            "holiday${t[0].year}Tbl"
                        )
                        for (item in t) {
                            TaskDatabaseHelper.createHoliday(
                                item,
                                dbHelper.writableDatabase,
                                "holiday${t[0].year}Tbl"
                            )
                        }
                    }
                }
            }
            if (!justDb) {
                initCalendarSetting()
                justDb = true
            }
        }
    }

    private fun initCalendarSetting(){
        //달력 배경 색
        binding.calendarView.background=requireContext().getDrawable(R.drawable.calendar_background)

        binding.calendarView.setTileHeightDp(40)    // 타일 높이
        binding.calendarView.setTileWidthDp(50)     // 타일 넓이
        //달력 날짜 선택시 이벤트
        binding.calendarView.setOnDateChangedListener(OnDateSelectedListener { widget, date, selected ->
            selectedDate=date
            settingSelectedDate(selectedDate)
            searchTaskInRepeatWeek(date.month,date.day,date)
            searchTaskInDay(date.month,date.day,date)
        })

        binding.calendarView.setOnMonthChangedListener(OnMonthChangedListener { widget, date ->
            //년도 바뀔때 마다 앞 뒤 년도 공휴일 가져오기
            currentY=date.year
            Log.d("도원","${currentY} || ${oldY}")
            if (currentY!=oldY){
                justDb=true
                for (a in currentY-1..currentY+1){
                    if (!TaskDatabaseHelper.isExistsTable(dbHelper.readableDatabase,"holiday${a}Tbl")){
                        checkHolidayForCreateDB(a,requireContext())
                    }
                }

            }
            oldY=currentY
            //달이 변경 될때마다 데코레이터 새로 초기화 해줘야함 안그러면 중복됨
            setDecorateForCalender(date.year,date.month,binding.calendarView.currentDate)

        })
        //현재 날짜 선택

        binding.calendarView.selectedDate= selectedDate

        //타이틀 포맷 변경 yyyy년 MM월
        binding.calendarView.setTitleFormatter(TitleFormatter {
            val calendarFormat =SimpleDateFormat("yyyy 년 MM 월")
            var date :Date =Date.valueOf(it.date.toString())
            val yearMonthFormat =calendarFormat.format(date)
            return@TitleFormatter yearMonthFormat
        })
        //일요일 토요일 범위 벗어난 평일 데코레이트
        setDecorateForCalender(selectedDate.year,selectedDate.month,binding.calendarView.currentDate)
        searchTaskInRepeatWeek(selectedDate.month,selectedDate.day,selectedDate)
        searchTaskInDay(selectedDate.month,selectedDate.day,selectedDate)
        settingSelectedDate(selectedDate)
        //월의 최값의 이전 날들, 최대값의 이후 날들 표시
        binding.calendarView.showOtherDates=MaterialCalendarView.SHOW_OTHER_MONTHS

        //타이틀 클릭 리스너
        binding.calendarView.setOnTitleClickListener(View.OnClickListener {
            selectedDate=CalendarDay.today()
            //타이틀 변경
            settingSelectedDate(selectedDate)
            //날짜 이동
            binding.calendarView.selectedDate = selectedDate
            //달력 이동
            binding.calendarView.currentDate = selectedDate
        })
    }

    //view 초기화
    fun initView(){
        binding.rcTaskList.layoutManager=LinearLayoutManager(context,LinearLayoutManager.VERTICAL,false)
        dailyTaskAdapter=DailyTaskAdapter(requireContext())
        binding.rcTaskList.adapter=dailyTaskAdapter

        dailyTaskAdapter.setOnDeleteItemClickListener(object : DailyTaskAdapter.OnItemClickListener{
            //삭제 선택시
            override fun onItemClick(v: View, item: TaskItem, pos: Int) {
                CustomAlertDialog(context!!).taskDeleteDialog( DialogInterface.OnClickListener { dialog, which ->
                    TaskDatabaseHelper.deleteTask(item._id.toString(),dbHelper.writableDatabase,context!!,Wearable.getDataClient(context!!))
                    dailyTaskAdapter.deleteItemOfList(pos)
                    setDecorateForCalender(selectedDate.year,selectedDate.month,selectedDate)
                    context?.let {
                        WidgetUtils.updateWidgetData(it)
                        WidgetUtils.changeDBToBytes(it)
                    }
                    dialog.dismiss()
                })
            }
            //task Dialog에서 삭제 및 수정 선택시
            override fun onTaskClick(v: View, item: TaskItem, pos: Int) {
                val taskDialog =ShowTaskDialog(context!!)
                taskDialog.showTask(item, {
                    //삭제
                    CustomAlertDialog(context!!).taskDeleteDialog( DialogInterface.OnClickListener { dialog, which ->
                        TaskDatabaseHelper.deleteTask(item._id.toString(),dbHelper.writableDatabase,context!!,Wearable.getDataClient(context!!))
                        dailyTaskAdapter.deleteItemOfList(pos)
                        setDecorateForCalender(selectedDate.year,selectedDate.month,selectedDate)
                        context?.let {
                            WidgetUtils.updateWidgetData(it)
                            WidgetUtils.changeDBToBytes(it)
                        }
                        dialog.dismiss()
                        taskDialog.dismissDialog()
                    })
                }, {
                    //체인지
                    val intent = Intent(context,CreateTaskActivity::class.java)
                    intent.putExtra("type","change")
                    intent.putExtra("taskForChange",item)
                    resultLauncher.launch(intent)
                    taskDialog.dismissDialog()
                })
            }
        })

        resultLauncher=registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
            ActivityResultCallback {
                //Task 등록 데이터 받는 곳
                if (it.resultCode == AppCompatActivity.RESULT_OK){
                    val intent = it.data
//                    TaskDatabaseHelper.changeTask(intent.getParcelableExtra<TaskItem>("changeItem")!!,dbHelper.writableDatabase)
                    Log.d("도원","d뭐여 ${intent?.getParcelableExtra<TaskItem>("changeItem")}")
                    if (intent?.getParcelableExtra<TaskItem>("changeItem")!=null){
                        TaskDatabaseHelper.changeTask(intent.getParcelableExtra<TaskItem>("changeItem")!!,dbHelper.writableDatabase,requireContext(), Wearable.getDataClient(requireContext()))
                        searchTaskInRepeatWeek(selectedDate.month,selectedDate.day,selectedDate)
                        searchTaskInDay(selectedDate.month,selectedDate.day,selectedDate)
                        setDecorateForCalender(selectedDate.year,selectedDate.month,binding.calendarView.currentDate)
                        context?.let {
                            WidgetUtils.updateWidgetData(it)
                            WidgetUtils.changeDBToBytes(it)
                        }
                    }
                }
            })

    }

    fun refreshTaskList(calendarDay: CalendarDay?){
        if (calendarDay!=null){
            selectedDate=calendarDay
//            tvSelectedDate.text="${selectedDate.year}.${selectedDate.month}.${selectedDate.day}"
            binding.calendarView.selectedDate = selectedDate
        }
        setDecorateForCalender(selectedDate.year,selectedDate.month,binding.calendarView.currentDate)
        searchTaskInRepeatWeek(selectedDate.month,selectedDate.day,selectedDate)
        searchTaskInDay(selectedDate.month,selectedDate.day,selectedDate)
    }

    fun getHolidayList(year :Int ,month :Int){
        holidayOfDay.clear()
        val c2 :Cursor? =TaskDatabaseHelper.searchHoliday(dbHelper.readableDatabase,year,month,"holiday${year}Tbl")
        if (c2 !=null){
            while (c2.moveToNext()){
                holidayOfDay[c2.getInt(2).toString()]=c2.getString(3)
            }
        }
    }

    //삭제 Dialog
//    private fun taskDeleteDialog(item :TaskItem,pos :Int){
//        val builder  =AlertDialog.Builder(context)
//
//        builder.setMessage("일정을 삭제 하시겠습니까?")
//        builder.setPositiveButton("확인", DialogInterface.OnClickListener { dialog, which ->
////            deleteTask(item._id.toString())
//            TaskDatabaseHelper.deleteTask(item._id.toString(),dbHelper.writableDatabase)
//            dailyTaskAdapter.deleteItemOfList(pos)
//            setDecorateForCalender(selectedDate.year,selectedDate.month,selectedDate)
//        })
//        builder.setNegativeButton("취소") { dialog, wihch ->
//            dialog.dismiss()
//        }
//        val alertDialog =builder.create()
//        alertDialog.show()
//    }

    //년마다 반복 task 의 날짜만 (month.day) 찾기
    @SuppressLint("Range")
    fun searchTaskOfRepeatYearInDB(){
        dayOfRepeatYear.clear()
        database=dbHelper.readableDatabase

        val yearCursor = TaskDatabaseHelper.searchDBOfYearRepeat(database)
        if (yearCursor!=null){
            while (yearCursor.moveToNext()) {
                dayOfRepeatYear.add("${yearCursor.getInt(yearCursor.getColumnIndex("month"))}.${yearCursor.getInt(yearCursor.getColumnIndex("day"))}");
            }
        }
//        try {
////        var c: Cursor = database.rawQuery("SELECT * FROM y${selectedDate.year.toString()}",null);
//            var c2: Cursor =
//                database.rawQuery("SELECT month,day,time,text,notice FROM myTaskTbl WHERE repeatY == 1", null);
//            while (c2.moveToNext()) {
////                생각해보니 날짜만 있으면 될 것 같기도 하고...
////                Log.d("도원","month : ${c2.getColumnIndex("month")} | " +
////                        "day : ${c2.getColumnIndex("day")} | " +
////                        "time : ${c2.getColumnIndex("time")} | " +
////                        "text : ${c2.getColumnIndex("text")} | " +
////                        "notice : ${c2.getColumnIndex("notice")} | ")
////                yearRepeatTaskList.add(
////                    YearRepeatTaskItem(
////                        c2.getInt(c2.getColumnIndex("month")),
////                        c2.getInt(c2.getColumnIndex("day")),
////                        c2.getLong(c2.getColumnIndex("time")),
////                        c2.getString(c2.getColumnIndex("text")),
////                        c2.getInt(c2.getColumnIndex("notice"))
////                    )
////                )
////
////              아무래도 HashMap Contain으로 보는게 편해서
//
//            }
//        }catch (e : SQLiteException){
//            //TBL 이 없는거면 읽어올 데이터도 없다는 것이니 그냥 패쓰해도 문제 없을듯
//        }
    }

    //월 마다 반복 TASK 찾기
    @SuppressLint("Range")
    fun searchTaskOfRepeatMonthInDB(){
        dayOfRepeatMonth.clear()
        database=dbHelper.readableDatabase

        val monthCursor = TaskDatabaseHelper.searchDBOfMonthRepeat(database)
        if (monthCursor!=null){
            while (monthCursor.moveToNext()) {
                dayOfRepeatMonth.add(monthCursor.getInt(monthCursor.getColumnIndex("day")).toString())
            }
        }
//        try {
//            val c2: Cursor =
//                database.rawQuery("SELECT day,time,text,notice FROM myTaskTbl WHERE repeatM == 1", null);
//            while (c2.moveToNext()) {
////                Log.d("도원","" +
////                        "day : ${c2.getColumnIndex("day")} | " +
////                        "time : ${c2.getColumnIndex("time")} | " +
////                        "text : ${c2.getColumnIndex("text")} | " +
////                        "notice : ${c2.getColumnIndex("notice")} | ")
////                monthRepeatTaskList.add(
////                    MonthRepeatTaskItem(
////                        c2.getInt(c2.getColumnIndex("day")),
////                        c2.getLong(c2.getColumnIndex("time")),
////                        c2.getString(c2.getColumnIndex("text")),
////                        c2.getInt(c2.getColumnIndex("notice"))
////                    )
////                )
//                dayOfRepeatMonth.add(c2.getInt(c2.getColumnIndex("day")).toString())
//            }
//        }catch (e : SQLiteException){
//            //TBL 이 없는거면 읽어올 데이터도 없다는 것이니 그냥 패쓰해도 문제 없을듯
//
//        }
    }

    //주마다 반복
    @SuppressLint("Range")
    fun searchTaskOfRepeatWeekInDB(){
        dayOfRepeatWeek.clear()
        database=dbHelper.readableDatabase

        val weekCursor = TaskDatabaseHelper.searchDBOfWeekRepeat(database)
        if (weekCursor!=null){
            while (weekCursor.moveToNext()){
                dayOfRepeatWeek.add(weekCursor.getString(weekCursor.getColumnIndex("week")))
            }
        }
//        try {
//            val c2: Cursor =
//                database.rawQuery("SELECT week,time,text,notice FROM myTaskTbl WHERE repeatW == 1", null);
//            while (c2.moveToNext()) {
////                Log.d("도원","" +
////                        "day : ${c2.getColumnIndex("week")} | " +
////                        "time : ${c2.getColumnIndex("time")} | " +
////                        "text : ${c2.getColumnIndex("text")} | " +
////                        "notice : ${c2.getColumnIndex("notice")} | ")
////                weekRepeatTaskList.add(
////                    WeekRepeatTaskItem(
////                        c2.getString(c2.getColumnIndex("week")),
////                        c2.getLong(c2.getColumnIndex("time")),
////                        c2.getString(c2.getColumnIndex("text")),
////                        c2.getInt(c2.getColumnIndex("notice"))
////                    )
////                )
//                dayOfRepeatWeek.add(c2.getString(c2.getColumnIndex("week")))
//            }
//        }catch (e : SQLiteException){
//            //TBL 이 없는거면 읽어올 데이터도 없다는 것이니 그냥 패쓰해도 문제 없을듯
//
//        }

    }

    //반복 안하는 Task 찾기
    @SuppressLint("Range")
    fun searchTaskOfRepeatNoInDB(){
        dayOfRepeatNo.clear()
        database=dbHelper.readableDatabase
        val noRepeatTask =TaskDatabaseHelper.searchDBOfNoRepeat(database)
        if (noRepeatTask!=null){
            while (noRepeatTask.moveToNext()){
                dayOfRepeatNo.add("${noRepeatTask.getInt(noRepeatTask.getColumnIndex("year"))}" +
                        ".${noRepeatTask.getInt(noRepeatTask.getColumnIndex("month"))}." +
                        "${noRepeatTask.getInt(noRepeatTask.getColumnIndex("day"))}")
            }
        }
//        try {
//            val c2: Cursor =
//                database.rawQuery("SELECT year,month,day,time,text,notice FROM myTaskTbl WHERE repeatN == 1", null);
//            while (c2.moveToNext()) {
////                Log.d("도원","" +
////                        "day : ${c2.getColumnIndex("week")} | " +
////                        "time : ${c2.getColumnIndex("time")} | " +
////                        "text : ${c2.getColumnIndex("text")} | " +
////                        "notice : ${c2.getColumnIndex("notice")} | ")
////                noRepeatTaskList.add(
////                    NoRepeatTaskItem(
////                        c2.getInt(c2.getColumnIndex("year")),
////                        c2.getInt(c2.getColumnIndex("month")),
////                        c2.getInt(c2.getColumnIndex("day")),
////                        c2.getLong(c2.getColumnIndex("time")),
////                        c2.getString(c2.getColumnIndex("text")),
////                        c2.getInt(c2.getColumnIndex("notice"))
////                    )
////                )
//                dayOfRepeatNo.add("${c2.getInt(c2.getColumnIndex("year"))}" +
//                        ".${c2.getInt(c2.getColumnIndex("month"))}." +
//                        "${c2.getInt(c2.getColumnIndex("day"))}")
//            }
//        }catch (e : SQLiteException){
//            //TBL 이 없는거면 읽어올 데이터도 없다는 것이니 그냥 패쓰해도 문제 없을듯
//
//        }

    }

    //해당 월에 맞는 task 찾기
    @SuppressLint("Range")
    fun searchTaskInDB(month : Int, year :Int){
        monthlyTaskList.clear()
        database=dbHelper.readableDatabase
        try {
//        var c: Cursor = database.rawQuery("SELECT * FROM y${selectedDate.year.toString()}",null);
            var c2: Cursor =
                database.rawQuery("SELECT * FROM y${year} WHERE month = ${month}", null);
            while (c2.moveToNext()) {
                monthlyTaskList.add(
                    TaskItem(
                        c2.getInt(c2.getColumnIndex("_id")),
                        c2.getInt(c2.getColumnIndex("year")),
                        c2.getInt(c2.getColumnIndex("month")),
                        c2.getInt(c2.getColumnIndex("day")),
                        c2.getString(c2.getColumnIndex("week")),
                        c2.getLong(c2.getColumnIndex("time")),
                        c2.getString(c2.getColumnIndex("title")),
                        c2.getString(c2.getColumnIndex("text")),
                        c2.getInt(c2.getColumnIndex("notice")),
                        c2.getInt(c2.getColumnIndex("repeatY")),
                        c2.getInt(c2.getColumnIndex("repeatM")),
                        c2.getInt(c2.getColumnIndex("repeatW")),
                        c2.getInt(c2.getColumnIndex("repeatN")),
                        c2.getInt(c2.getColumnIndex("priority")),
                        0,
                        ""
                    )
                )
            }
        }catch (e : SQLiteException){
            //TBL 이 없는거면 읽어올 데이터도 없다는 것이니 그냥 패쓰해도 문제 없을듯

        }
    }

    @SuppressLint("Range")
    private fun searchTaskInRepeatWeek(month :Int,day:Int,calendarDay: CalendarDay){
        dailyTaskList.clear()
        try {
            var c2: Cursor =
                database.rawQuery("SELECT * FROM myTaskTbl WHERE week != '0&0&0&0&0&0&0' ", null);
            while (c2.moveToNext()) {
                val tempStr: List<String> = c2.getString(c2.getColumnIndex("week")).split("&")
                for (a in 0..tempStr.size - 1) {
                    if (tempStr[a].equals("1")) {
                        var pos = a;
                        if (a == 0) pos = 7
                        if (pos == calendarDay.date.dayOfWeek.value) {
                            val tempTask = TaskItem(
                                c2.getInt(c2.getColumnIndex("_id")),
                                c2.getInt(c2.getColumnIndex("year")),
                                c2.getInt(c2.getColumnIndex("month")),
                                c2.getInt(c2.getColumnIndex("day")),
                                c2.getString(c2.getColumnIndex("week")),
                                c2.getLong(c2.getColumnIndex("time")),
                                c2.getString(c2.getColumnIndex("title")),
                                c2.getString(c2.getColumnIndex("text")),
                                c2.getInt(c2.getColumnIndex("notice")),
                                c2.getInt(c2.getColumnIndex("repeatY")),
                                c2.getInt(c2.getColumnIndex("repeatM")),
                                c2.getInt(c2.getColumnIndex("repeatW")),
                                c2.getInt(c2.getColumnIndex("repeatN")),
                                c2.getInt(c2.getColumnIndex("priority")),
                                0,
                                ""
                            )
                            dailyTaskList.add(tempTask)
                        }
                    }
                }
            }
            c2.close()
        }catch (e : SQLiteException){
            //TBL 이 없는거면 읽어올 데이터도 없다는 것이니 그냥 패쓰해도 문제 없을듯
            Log.d("도원","ee : ${e.localizedMessage}");
        }

//        Log.d("도원","month : ${c2.getString(c2.getColumnIndex("week"))} | ")
//        Log.d("도원","month : ${calendarDay.date.dayOfWeek.value} | ${tempStr.size} ")




    }

    //선택된 날짜에 맞는 task 찾기
    @SuppressLint("Range")
    private fun searchTaskInDay(month :Int,day:Int,calendarDay: CalendarDay){
        database=dbHelper.readableDatabase
        try {
            var c2: Cursor = database.rawQuery("SELECT * FROM myTaskTbl WHERE day = ${day} AND week == '0&0&0&0&0&0&0' ",null);
            while (c2.moveToNext()){
                if (c2.getInt(c2.getColumnIndex("repeatN"))==1 &&
                        month==c2.getInt(c2.getColumnIndex("month")) &&
                    calendarDay.year == c2.getInt(c2.getColumnIndex("year"))){
                    val tempTask =TaskItem(
                        c2.getInt(c2.getColumnIndex("_id")),
                        c2.getInt(c2.getColumnIndex("year")),
                        c2.getInt(c2.getColumnIndex("month")),
                        c2.getInt(c2.getColumnIndex("day")),
                        c2.getString(c2.getColumnIndex("week")),
                        c2.getLong(c2.getColumnIndex("time")),
                        c2.getString(c2.getColumnIndex("title")),
                        c2.getString(c2.getColumnIndex("text")),
                        c2.getInt(c2.getColumnIndex("notice")),
                        c2.getInt(c2.getColumnIndex("repeatY")),
                        c2.getInt(c2.getColumnIndex("repeatM")),
                        c2.getInt(c2.getColumnIndex("repeatW")),
                        c2.getInt(c2.getColumnIndex("repeatN")),
                        c2.getInt(c2.getColumnIndex("priority")),
                        0,
                        ""
                    )
                    dailyTaskList.add(tempTask)
                }else if (c2.getInt(c2.getColumnIndex("repeatY"))==1 &&
                    month==c2.getInt(c2.getColumnIndex("month"))){
                    val tempTask =TaskItem(
                        c2.getInt(c2.getColumnIndex("_id")),
                        c2.getInt(c2.getColumnIndex("year")),
                        c2.getInt(c2.getColumnIndex("month")),
                        c2.getInt(c2.getColumnIndex("day")),
                        c2.getString(c2.getColumnIndex("week")),
                        c2.getLong(c2.getColumnIndex("time")),
                        c2.getString(c2.getColumnIndex("title")),
                        c2.getString(c2.getColumnIndex("text")),
                        c2.getInt(c2.getColumnIndex("notice")),
                        c2.getInt(c2.getColumnIndex("repeatY")),
                        c2.getInt(c2.getColumnIndex("repeatM")),
                        c2.getInt(c2.getColumnIndex("repeatW")),
                        c2.getInt(c2.getColumnIndex("repeatN")),
                        c2.getInt(c2.getColumnIndex("priority")),
                        0,
                        ""
                    )
                    dailyTaskList.add(tempTask)
                }else if (c2.getInt(c2.getColumnIndex("repeatM"))==1&&
                    day==c2.getInt(c2.getColumnIndex("day"))){
                    val tempTask =TaskItem(
                        c2.getInt(c2.getColumnIndex("_id")),
                        c2.getInt(c2.getColumnIndex("year")),
                        c2.getInt(c2.getColumnIndex("month")),
                        c2.getInt(c2.getColumnIndex("day")),
                        c2.getString(c2.getColumnIndex("week")),
                        c2.getLong(c2.getColumnIndex("time")),
                        c2.getString(c2.getColumnIndex("title")),
                        c2.getString(c2.getColumnIndex("text")),
                        c2.getInt(c2.getColumnIndex("notice")),
                        c2.getInt(c2.getColumnIndex("repeatY")),
                        c2.getInt(c2.getColumnIndex("repeatM")),
                        c2.getInt(c2.getColumnIndex("repeatW")),
                        c2.getInt(c2.getColumnIndex("repeatN")),
                        c2.getInt(c2.getColumnIndex("priority")),
                        0,
                        ""
                    )
                    dailyTaskList.add(tempTask)
                }

            }
//            Log.d("도원","dailyTaskList2 : ${dailyTaskList} | ")
            dailyTaskAdapter.setTaskItem(dailyTaskList,calendarDay)
            c2.close()
        }catch (e : SQLiteException){
            //TBL 이 없는거면 읽어올 데이터도 없다는 것이니 그냥 패쓰해도 문제 없을듯
            Log.d("도원","ee : ${e.localizedMessage}");
        }
//        rcTaskList.adapter
    }

    //tbl 삭제용
    fun dropTable(view :View,tblName :String){
//        dbHelper= TaskDatabaseHelper(view.context,"task.db",null,1);
        database=dbHelper.writableDatabase;
        var c2: Cursor =database.rawQuery("DROP TABLE IF EXISTS ${tblName.toString()}",null)

        Log.d("도원","c2  ${c2} || ${database.version}")
//        database.delete(tblName,null,null)
    }

    private fun checkHolidayForCreateDB(year : Int, context: Context){
        CoroutineScope(Dispatchers.Main).launch {
            holidayRetrofit.getHoliday(context,year.toString())
        }
    }

    private fun settingSelectedDate(selectDay :CalendarDay){
        if (holidayOfDay[selectDay.day.toString()] !=null){
            binding.tvSelectedDate.text="${selectDay.year}.${selectDay.month}.${selectDay.day} [${holidayOfDay[selectDay.day.toString()]}]"
            binding.tvSelectedDate.setTextColor(Color.parseColor("#FF0000"))
        }else{
            binding.tvSelectedDate.text="${selectDay.year}.${selectDay.month}.${selectDay.day}"
            binding.tvSelectedDate.setTextColor(Color.parseColor("#000000"))
        }
    }


    //일정 만들기 method
    fun notifydataChange(){
        database=dbHelper.writableDatabase
        searchTaskInRepeatWeek(selectedDate.month,selectedDate.day,selectedDate)
        searchTaskInDay(selectedDate.month,selectedDate.day,selectedDate)
        setDecorateForCalender(selectedDate.year,selectedDate.month,binding.calendarView.currentDate)
    }

    //데코레이션 method
    private fun setDecorateForCalender(year:Int,month :Int, date : CalendarDay){

        CoroutineScope(Dispatchers.IO).launch {
//        searchTaskInDB(month,year)
            searchTaskOfRepeatYearInDB()
            searchTaskOfRepeatMonthInDB()
            searchTaskOfRepeatWeekInDB()
            searchTaskOfRepeatNoInDB()
            getHolidayList(year,month)
            addDeco(month,date)
        }

    }
    suspend fun addDeco(month :Int, date : CalendarDay) = withContext(Dispatchers.Main){
        binding.calendarView.removeDecorators()
        context?.let {
            binding.calendarView.addDecorators(
                RangeDayDecorator(date),
                SundayDecorator(),
                SaturdayDecorator(),
                HolidayDecorator(holidayOfDay),
                OutOfRangeDecorator(date),
                SelectDecorator(it as Activity),
                TaskDotDecorator(it as Activity,dayOfRepeatYear,month,"Year"),
                TaskDotDecorator(it as Activity,dayOfRepeatMonth,month,"Month"),
                TaskDotDecorator(it as Activity,dayOfRepeatWeek,month,"Week"),
                TaskDotDecorator(it as Activity,dayOfRepeatNo,month,"No"),
                TodayDecorator(it,CalendarDay.today())
            )
        }
    }

    @SuppressLint("Range")
    fun checkTBLNAME(){
        //테이블 명 파악하는 메소드
        database=dbHelper.writableDatabase
        var c2 :Cursor = database.rawQuery("SELECT * FROM sqlite_master WHERE type='table'",null)
        while (c2.moveToNext()){
            for (a in c2.columnNames){
                Log.d("도원","month : ${a} || ${c2.getString(c2.getColumnIndex(a))} ")
            }
            Log.d("도원","month : =============================")
        }
    }

    fun getSelectDateInfo() = selectedDate

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable("calendarDate",selectedDate);
        Log.d("도원","selectedDate8  : ${selectedDate.month}.${selectedDate.day}");
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            CalendarFragment().apply {

            }
    }
}