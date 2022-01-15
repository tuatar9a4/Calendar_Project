package com.dwstyle.calenderbydw.fragments

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
import androidx.core.view.marginBottom
import androidx.core.view.marginLeft
import androidx.core.view.marginRight
import androidx.core.view.marginTop
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dwstyle.calenderbydw.CreateTaskActivity
import com.dwstyle.calenderbydw.R
import com.dwstyle.calenderbydw.adapters.DailyTaskAdapter
import com.dwstyle.calenderbydw.calendardacorator.*
import com.dwstyle.calenderbydw.database.TaskDatabaseHelper
import com.dwstyle.calenderbydw.item.*
import com.dwstyle.calenderbydw.retrofit.HolidayRetrofit
import com.dwstyle.calenderbydw.utils.CustomAlertDialog
import com.dwstyle.calenderbydw.utils.ShowTaskDialog
import com.dwstyle.calenderbydw.utils.WidgetUtils
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

    private lateinit var calendarView:MaterialCalendarView
    //선택된 날짜 표시
    private lateinit var tvSelectedDate : TextView
    private lateinit var selectedDate : CalendarDay

    //할일 목록
    private lateinit var rcTaskList : RecyclerView
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
    ): View? {
        // Inflate the layout for this fragment
        val view =inflater.inflate(R.layout.fragment_calendar, container, false)
        AndroidThreeTen.init(view.context)
        initView(view)
        dbHelper= TaskDatabaseHelper(view.context,"task.db",null,3)
//        dbHelper.createMonthTBL("myTaskTbl")
        database=dbHelper.writableDatabase
        dbHelper.onUpgrade(database,database.version,3)
//        dropTable(view,"myTaskTbl");
//        dbHelper.onCreate(database)
//        dropTable(view,"y2022");
        bindData(view)

        var check =false;
        for (a in selectedDate.year-1..selectedDate.year+1){
            if (!TaskDatabaseHelper.isExistsTable(dbHelper.readableDatabase,"holiday${a}Tbl")){
                checkHolidayForCreateDB(a,view.context)
                check=true;
            }
        }
        if (!check){
            initCalendarSetting(view)
        }
        return view
    }

    private fun bindData(v: View){
        holidayRetrofit.getHolidayItems().observe(viewLifecycleOwner,
            { t :ArrayList<HolidayItem> ->
                if (t.size>0){
                    if (!TaskDatabaseHelper.isExistsTable(dbHelper.readableDatabase,"holiday${t[0].year}Tbl")){
                        dbHelper.createHolidayTbl(dbHelper.writableDatabase,"holiday${t[0].year}Tbl")
                        for ((i,item) in t.withIndex()){
                            TaskDatabaseHelper.createHoliday(item,dbHelper.writableDatabase,"holiday${t[0].year}Tbl")
                        }
                    }
                }
                if (!justDb){
                    initCalendarSetting(v)
                    justDb=true
                }
            })

    }

    private fun initCalendarSetting(view : View){
        //달력 배경 색 및 선택시 색
        calendarView.background=view.context.getDrawable(R.drawable.calendar_background)
//        calendarView.selectionColor=Color.parseColor("#cc00cc")
        Log.d("도원"," dount : ${calendarView.childCount}")

        calendarView.setTileHeightDp(40)
        calendarView.setTileWidthDp(50)
        //달력 날짜 선택시 이벤트
        calendarView.setOnDateChangedListener(OnDateSelectedListener { widget, date, selected ->
            selectedDate=date
            settingSelectedDate(selectedDate)
            searchTaskInRepeatWeek(date.month,date.day,date)
            searchTaskInDay(date.month,date.day,date)
        })

        calendarView.setOnMonthChangedListener(OnMonthChangedListener { widget, date ->
            //년도 바뀔때 마다 앞 뒤 년도 공휴일 가져오기
            currentY=date.year
            Log.d("도원","${currentY} || ${oldY}")
            if (currentY!=oldY){
                justDb=true
                for (a in currentY-1..currentY+1){
                    if (!TaskDatabaseHelper.isExistsTable(dbHelper.readableDatabase,"holiday${a}Tbl")){
                        checkHolidayForCreateDB(a,view.context)
                    }
                }

            }
            oldY=currentY
            //달이 변경 될때마다 데코레이터 새로 초기화 해줘야함 안그러면 중복됨
            setDecorateForCalender(date.year,date.month,calendarView.currentDate)

        })
        //현재 날짜 선택

        calendarView.selectedDate= selectedDate

        //타이틀 포맷 변경 yyyy년 MM월
        calendarView.setTitleFormatter(TitleFormatter {
            val calendarFormat =SimpleDateFormat("yyyy 년 MM 월")
            var date :Date =Date.valueOf(it.date.toString())
            val yearMonthFormat =calendarFormat.format(date)
            return@TitleFormatter yearMonthFormat
        })
        //일요일 토요일 범위 벗어난 평일 데코레이트
        setDecorateForCalender(selectedDate.year,selectedDate.month,calendarView.currentDate)
        searchTaskInRepeatWeek(selectedDate.month,selectedDate.day,selectedDate)
        searchTaskInDay(selectedDate.month,selectedDate.day,selectedDate)
        settingSelectedDate(selectedDate)
        //월의 최값의 이전 날들, 최대값의 이후 날들 표시
        calendarView.showOtherDates=MaterialCalendarView.SHOW_OTHER_MONTHS

        //타이틀 클릭 리스너
        calendarView.setOnTitleClickListener(View.OnClickListener {
            selectedDate=CalendarDay.today()
            //타이틀 변경
            settingSelectedDate(selectedDate)
            //날짜 이동
            calendarView.selectedDate = selectedDate
            //달력 이동
            calendarView.currentDate = selectedDate
        })
    }

    //view 초기화
    fun initView(view : View){
        calendarView=view.findViewById(R.id.calendarView);
        tvSelectedDate=view.findViewById(R.id.tvSelectedDate);
        rcTaskList=view.findViewById(R.id.rcTaskList);
        rcTaskList.layoutManager=LinearLayoutManager(view.context,LinearLayoutManager.VERTICAL,false)
        dailyTaskAdapter=DailyTaskAdapter(view.context)
        rcTaskList.adapter=dailyTaskAdapter

        dailyTaskAdapter.setOnDeleteItemClickListener(object : DailyTaskAdapter.OnItemClickListener{
            //삭제 선택시
            override fun onItemClick(v: View, item: TaskItem, pos: Int) {
                CustomAlertDialog(context!!).taskDeleteDialog( DialogInterface.OnClickListener { dialog, which ->
                    TaskDatabaseHelper.deleteTask(item._id.toString(),dbHelper.writableDatabase)
                    dailyTaskAdapter.deleteItemOfList(pos)
                    setDecorateForCalender(selectedDate.year,selectedDate.month,selectedDate)
                    context?.let {
                        WidgetUtils.updateWidgetData(it)
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
                        TaskDatabaseHelper.deleteTask(item._id.toString(),dbHelper.writableDatabase)
                        dailyTaskAdapter.deleteItemOfList(pos)
                        setDecorateForCalender(selectedDate.year,selectedDate.month,selectedDate)
                        context?.let {
                            WidgetUtils.updateWidgetData(it)
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
                        TaskDatabaseHelper.changeTask(intent.getParcelableExtra<TaskItem>("changeItem")!!,dbHelper.writableDatabase)
                        searchTaskInRepeatWeek(selectedDate.month,selectedDate.day,selectedDate)
                        searchTaskInDay(selectedDate.month,selectedDate.day,selectedDate)
                        setDecorateForCalender(selectedDate.year,selectedDate.month,calendarView.currentDate)
                        context?.let {
                            WidgetUtils.updateWidgetData(it)
                        }
                    }
                }
            })

    }

    fun refreshTaskList(calendarDay: CalendarDay?){
        if (calendarDay!=null){
            selectedDate=calendarDay
//            tvSelectedDate.text="${selectedDate.year}.${selectedDate.month}.${selectedDate.day}"
            calendarView.selectedDate = selectedDate
        }
        setDecorateForCalender(selectedDate.year,selectedDate.month,calendarView.currentDate)
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
    fun searchTaskInDB(month : Int,year :Int){
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
                        ""
                    )
                )
            }
        }catch (e : SQLiteException){
            //TBL 이 없는거면 읽어올 데이터도 없다는 것이니 그냥 패쓰해도 문제 없을듯

        }
    }


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
            tvSelectedDate.text="${selectDay.year}.${selectDay.month}.${selectDay.day} [${holidayOfDay[selectDay.day.toString()]}]"
            tvSelectedDate.setTextColor(Color.parseColor("#FF0000"))
        }else{
            tvSelectedDate.text="${selectDay.year}.${selectDay.month}.${selectDay.day}"
            tvSelectedDate.setTextColor(Color.parseColor("#000000"))
        }
    }


    //일정 만들기 method
    fun notifydataChange(){
        database=dbHelper.writableDatabase
//        var contentValue = ContentValues();
//        contentValue.put("year",taskItem.year)
//        contentValue.put("month",taskItem.month)
//        contentValue.put("day",taskItem.day);
//        contentValue.put("week",taskItem.week);
//        contentValue.put("time",taskItem.time);
//        contentValue.put("title",taskItem.title);
//        contentValue.put("text",taskItem.text);
//        contentValue.put("repeatY",taskItem.repeatY);
//        contentValue.put("repeatM",taskItem.repeatM);
//        contentValue.put("repeatW",taskItem.repeatW);
//        contentValue.put("repeatN",taskItem.repeatN);
//        contentValue.put("notice",taskItem.notice);
//        contentValue.put("priority",taskItem.priority)
//        contentValue.put("expectDay",taskItem.exceptDay)
//
//        database.insert("myTaskTbl",null,contentValue);
//        var c2: Cursor = database.rawQuery("SELECT * FROM y${selectedDate.year.toString()}",null);
//        while (c2.moveToNext()){
//            Log.d("도원","month : ${c2.getString(c2.getColumnIndex("month"))} |  day ${c2.getString(c2.getColumnIndex("day"))}   | text :  ${c2.getString(c2.getColumnIndex("text"))} ")
//        }
        searchTaskInRepeatWeek(selectedDate.month,selectedDate.day,selectedDate)
        searchTaskInDay(selectedDate.month,selectedDate.day,selectedDate)
        setDecorateForCalender(selectedDate.year,selectedDate.month,calendarView.currentDate)
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
        calendarView.removeDecorators()
        calendarView.addDecorators(
            RangeDayDecorator(date),
            SundayDecorator(),
            SaturdayDecorator(),
            HolidayDecorator(context as Activity,holidayOfDay),
            OutOfRangeDecorator(date),
            SelectDecorator(context as Activity),
            TaskDotDecorator(context as Activity,dayOfRepeatYear,month,"Year"),
            TaskDotDecorator(context as Activity,dayOfRepeatMonth,month,"Month"),
            TaskDotDecorator(context as Activity,dayOfRepeatWeek,month,"Week"),
            TaskDotDecorator(context as Activity,dayOfRepeatNo,month,"No"),
            TodayDecorator(context,CalendarDay.today())
        )
    }

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