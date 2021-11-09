package com.dwstyle.calenderbydw.fragments

import android.app.Activity
import android.content.AbstractThreadedSyncAdapter
import android.content.ContentValues
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dwstyle.calenderbydw.R
import com.dwstyle.calenderbydw.adapters.DailyTaskAdapter
import com.dwstyle.calenderbydw.calendardacorator.*
import com.dwstyle.calenderbydw.database.TaskDatabaseHelper
import com.dwstyle.calenderbydw.item.TaskItem
import com.jakewharton.threetenabp.AndroidThreeTen
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener
import com.prolificinteractive.materialcalendarview.OnMonthChangedListener
import com.prolificinteractive.materialcalendarview.format.TitleFormatter
import java.sql.Date
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class CalendarFragment : Fragment() {

    private lateinit var calendarView:MaterialCalendarView
    //선택된 날짜 표시
    private lateinit var tvSelectedDate : TextView
    private lateinit var selectedDate : CalendarDay

    //할일 목록
    private lateinit var rcTaskList : RecyclerView
    private val monthlyTaskList : ArrayList<TaskItem> =ArrayList<TaskItem>()
    private val dailyTaskList :ArrayList<TaskItem> = ArrayList()
    private lateinit var dailyTaskAdapter: DailyTaskAdapter

    //년도 반복 ,달마다 반복, 주마다 반복, 반복 없음 task
    private val dayOfRepeatYear=HashMap<Int,String>()


    private lateinit var dbHelper : TaskDatabaseHelper
    private lateinit var database : SQLiteDatabase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dayOfRepeatYear[4] = "#FF0000&#00FF00&#0000FF"
        dayOfRepeatYear[8] = "#FFFF00&#00FFFF&#FF00FF"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =inflater.inflate(R.layout.fragment_calendar, container, false);
        AndroidThreeTen.init(view.context);
        initView(view)
        dbHelper= TaskDatabaseHelper(view.context,"task.db",null,1);
//        dropTable(view,"y2021");
//        dropTable(view,"y2022");
        initCalendarSetting(view)

        return view
    }

    fun initCalendarSetting(view : View){
        //달력 배경 색 및 선택시 색
        calendarView.background=view.context.getDrawable(R.drawable.calendar_background)
        calendarView.selectionColor=Color.parseColor("#cc00cc")

        //달력 날짜 선택시 이벤트
        calendarView.setOnDateChangedListener(OnDateSelectedListener { widget, date, selected ->
            selectedDate=date
            tvSelectedDate.text="${selectedDate.year}.${selectedDate.month}.${selectedDate.day}"
            searchTaskInDay(date.month,date.day)

        })

        calendarView.setOnMonthChangedListener(OnMonthChangedListener { widget, date ->
            //달이 변경 될때마다 데코레이터 새로 초기화 해줘야함 안그러면 중복됨
            setDecorateForCalender(date.year,date.month,calendarView.currentDate)

        })
        //현재 날짜 선택
        selectedDate=CalendarDay.today()
        calendarView.selectedDate= selectedDate
        tvSelectedDate.text="${selectedDate.year}.${selectedDate.month}.${selectedDate.day}"
        //타이틀 포맷 변경 yyyy년 MM월
        calendarView.setTitleFormatter(TitleFormatter {
            val calendarFormat =SimpleDateFormat("yyyy 년 MM 월")
            var date :Date =Date.valueOf(it.date.toString())
            val yearMonthFormat =calendarFormat.format(date)
            return@TitleFormatter yearMonthFormat
        })
        //일요일 토요일 범위 벗어난 평일 데코레이트
        setDecorateForCalender(selectedDate.year,selectedDate.month,calendarView.currentDate)
        //월의 최값의 이전 날들, 최대값의 이후 날들 표시
        calendarView.showOtherDates=MaterialCalendarView.SHOW_OTHER_MONTHS

        //타이틀 클릭 리스너
        calendarView.setOnTitleClickListener(View.OnClickListener {
            selectedDate=CalendarDay.today()
            //타이틀 변경
            tvSelectedDate.text="${selectedDate.year}.${selectedDate.month}.${selectedDate.day}"
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

    }

    //해당 월에 맞는 task 찾기
    fun searchTaskInDB(month : Int,year :Int){
        monthlyTaskList.clear()
        dbHelper.createMonthTBL("y${year}");
        database=dbHelper.readableDatabase
        try {
//        var c: Cursor = database.rawQuery("SELECT * FROM y${selectedDate.year.toString()}",null);
            var c2: Cursor =
                database.rawQuery("SELECT * FROM y${year} WHERE month = ${month}", null);
            while (c2.moveToNext()) {
                monthlyTaskList.add(
                    TaskItem(
                        c2.getInt(c2.getColumnIndex("year")),
                        c2.getInt(c2.getColumnIndex("month")),
                        c2.getInt(c2.getColumnIndex("day")),
                        c2.getString(c2.getColumnIndex("week")),
                        c2.getLong(c2.getColumnIndex("time")),
                        c2.getString(c2.getColumnIndex("text")),
                        c2.getInt(c2.getColumnIndex("notice")),
                        c2.getInt(c2.getColumnIndex("repeatY")),
                        c2.getInt(c2.getColumnIndex("repeatM")),
                        c2.getInt(c2.getColumnIndex("repeatW")),
                        c2.getInt(c2.getColumnIndex("repeatN"))
                    )
                )
            }
        }catch (e : SQLiteException){
            //TBL 이 없는거면 읽어올 데이터도 없다는 것이니 그냥 패쓰해도 문제 없을듯

        }
    }

    //선택된 일에 맞는 task 찾기
    private fun searchTaskInDay(month :Int,day:Int){
        dailyTaskList.clear()
        dbHelper.createMonthTBL("y"+selectedDate.year.toString());
        database=dbHelper.readableDatabase
        try {
            var c2: Cursor = database.rawQuery("SELECT * FROM y${selectedDate.year.toString()} WHERE day = ${day} AND month = $month",null);
            while (c2.moveToNext()){
//            Log.d("도원","month : ${c2.getString(c2.getColumnIndex("month"))} |  day ${c2.getString(c2.getColumnIndex("day"))}   | text :  ${c2.getString(c2.getColumnIndex("text"))} ")
                val tempTask =TaskItem(
                    c2.getInt(c2.getColumnIndex("year")),
                    c2.getInt(c2.getColumnIndex("month")),
                    c2.getInt(c2.getColumnIndex("day")),
                    c2.getString(c2.getColumnIndex("week")),
                    c2.getLong(c2.getColumnIndex("time")),
                    c2.getString(c2.getColumnIndex("text")),
                    c2.getInt(c2.getColumnIndex("notice")),
                    c2.getInt(c2.getColumnIndex("repeatY")),
                    c2.getInt(c2.getColumnIndex("repeatM")),
                    c2.getInt(c2.getColumnIndex("repeatW")),
                    c2.getInt(c2.getColumnIndex("repeatN"))
                )
                dailyTaskList.add(tempTask)
            }
            dailyTaskAdapter.setTaskItem(dailyTaskList)
        }catch (e : SQLiteException){
            //TBL 이 없는거면 읽어올 데이터도 없다는 것이니 그냥 패쓰해도 문제 없을듯
        }
//        rcTaskList.adapter
    }

    //tbl 삭제용
    fun dropTable(view :View,tblName :String){
        dbHelper= TaskDatabaseHelper(view.context,"task.db",null,1);
        database=dbHelper.writableDatabase;
        var c2: Cursor =database.rawQuery("DROP TABLE IF EXISTS $tblName",null)
    }

    //일정 만들기 method
    fun createTask(taskItem: TaskItem){
        dbHelper.createMonthTBL("myTaskTbl");
        database=dbHelper.writableDatabase
        dbHelper.onCreate(database)
        var contentValue = ContentValues();
        contentValue.put("year",taskItem.year)
        contentValue.put("month",taskItem.month)
        contentValue.put("day",taskItem.day);
        contentValue.put("week",taskItem.week);
        contentValue.put("time",taskItem.time);
        contentValue.put("text",taskItem.text);
        contentValue.put("repeatY",taskItem.repeatY);
        contentValue.put("repeatM",taskItem.repeatM);
        contentValue.put("repeatW",taskItem.repeatW);
        contentValue.put("repeatN",taskItem.repeatN);
        contentValue.put("notice",taskItem.notice);

        database.insert("myTaskTbl",null,contentValue);
//        var c2: Cursor = database.rawQuery("SELECT * FROM y${selectedDate.year.toString()}",null);
//        while (c2.moveToNext()){
//            Log.d("도원","month : ${c2.getString(c2.getColumnIndex("month"))} |  day ${c2.getString(c2.getColumnIndex("day"))}   | text :  ${c2.getString(c2.getColumnIndex("text"))} ")
//        }

        setDecorateForCalender(selectedDate.year,selectedDate.month,calendarView.currentDate)
    }

    //데코레이션 method
    private fun setDecorateForCalender(year:Int,month :Int, date : CalendarDay){
        calendarView.removeDecorators()
        searchTaskInDB(month,year)
        calendarView.addDecorators(
            SundayDecorator(),
            SaturdayDecorator(),
            TaskDotDecorator(dayOfRepeatYear),
            SelectDecorator(context as Activity),
            OutOfRangeDecorator(date)
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


    companion object {
        @JvmStatic
        fun newInstance() =
            CalendarFragment().apply {

            }
    }
}