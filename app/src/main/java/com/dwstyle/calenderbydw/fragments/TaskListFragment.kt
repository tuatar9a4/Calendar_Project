package com.dwstyle.calenderbydw.fragments

import android.content.DialogInterface
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dwstyle.calenderbydw.R
import com.dwstyle.calenderbydw.adapters.TaskListOfSelectDateAdapter
import com.dwstyle.calenderbydw.database.TaskDatabaseHelper
import com.dwstyle.calenderbydw.item.TaskItem
import com.dwstyle.calenderbydw.utils.CustomAlertDialog
import com.prolificinteractive.materialcalendarview.CalendarDay
import kotlinx.android.synthetic.main.fragment_task_list.*

class TaskListFragment : Fragment() {

    private lateinit var tvDay:TextView
    private lateinit var preDate:Button
    private lateinit var nextDate:Button
    private lateinit var rcSelectTaskList :RecyclerView

    private lateinit var taskListOfSelectDateAdapter :TaskListOfSelectDateAdapter

    private var selectedCalendarDay : CalendarDay?=null


    private lateinit var dbHelper : TaskDatabaseHelper
    private lateinit var database : SQLiteDatabase

    private val dailyTaskList =ArrayList<TaskItem>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view= inflater.inflate(R.layout.fragment_task_list,container,false)
        initView(view)
        dbHelper= TaskDatabaseHelper(view.context,"task.db",null,2)

        selectedCalendarDay?.let {
            tvDay.text="${it.year}.${it.month}.${it.day}"
        }

        preDate.setOnClickListener {
            selectedCalendarDay= CalendarDay.from(selectedCalendarDay?.date!!.minusDays(1))
            Log.d("도원","${selectedCalendarDay?.year}.${selectedCalendarDay?.month}.${selectedCalendarDay?.day}")
            tvDay.text="${selectedCalendarDay?.year}.${selectedCalendarDay?.month}.${selectedCalendarDay?.day}"
            searchTaskOfSelectedDay(selectedCalendarDay!!)
        }

        nextDate.setOnClickListener {
            selectedCalendarDay= CalendarDay.from(selectedCalendarDay?.date!!.plusDays(1))
            Log.d("도원","${selectedCalendarDay?.year}.${selectedCalendarDay?.month}.${selectedCalendarDay?.day}")
            tvDay.text="${selectedCalendarDay?.year}.${selectedCalendarDay?.month}.${selectedCalendarDay?.day}"
            searchTaskOfSelectedDay(selectedCalendarDay!!)
        }

        taskListOfSelectDateAdapter=TaskListOfSelectDateAdapter(view.context)
        rcSelectTaskList.adapter=taskListOfSelectDateAdapter
        taskListOfSelectDateAdapter.setOnDeleteItemClickListener(object :TaskListOfSelectDateAdapter.OnDeleteClickListener{
            override fun OnDeleteClick(v: View, item: TaskItem, pos: Int) {
                database=dbHelper.writableDatabase
                CustomAlertDialog(context!!).taskDeleteDialog( DialogInterface.OnClickListener { dialog, which ->
                    TaskDatabaseHelper.deleteTask(item._id.toString(),dbHelper.writableDatabase)
                    searchTaskOfSelectedDay(selectedCalendarDay!!)
                    dialog.dismiss()
                })
            }
        })
        return view
    }

    fun initView(view :View){
        tvDay=view.findViewById(R.id.tvDay)
        preDate=view.findViewById(R.id.preDate)
        nextDate=view.findViewById(R.id.nextDate)
        rcSelectTaskList=view.findViewById(R.id.rcSelectTaskList)
        rcSelectTaskList.layoutManager=LinearLayoutManager(view.context,LinearLayoutManager.VERTICAL,false)
    }

    fun setCalendarDay(calendarDay: CalendarDay){
        selectedCalendarDay=calendarDay
        tvDay.text="${selectedCalendarDay?.year}.${selectedCalendarDay?.month}.${selectedCalendarDay?.day}"
        searchTaskOfSelectedDay(selectedCalendarDay!!)
    }

    fun searchTaskOfSelectedDay(calendarDay: CalendarDay){
        searchTaskInRepeatWeek(calendarDay)
        searchTaskInDay(calendarDay)
    }

    fun notifydataChange(){
        selectedCalendarDay?.let {
            searchTaskOfSelectedDay(it)
        }
    }

    private fun searchTaskInRepeatWeek(calendarDay: CalendarDay){
        database=dbHelper.readableDatabase
        dailyTaskList.clear()
        try {
            val c2: Cursor =
                database.rawQuery("SELECT * FROM myTaskTbl WHERE week != '0&0&0&0&0&0&0' ", null);
            while (c2.moveToNext()) {
                val tempStr: List<String> = c2.getString(c2.getColumnIndex("week")).split("&")
                for (a in tempStr.indices) {
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
    }

    //선택된 날짜에 맞는 task 찾기
    private fun searchTaskInDay(calendarDay: CalendarDay){
        database=dbHelper.readableDatabase
        try {
            val c2: Cursor = database.rawQuery("SELECT * FROM myTaskTbl WHERE day = ${calendarDay.day} AND week == '0&0&0&0&0&0&0' ",null);
            while (c2.moveToNext()){
                if (c2.getInt(c2.getColumnIndex("repeatN"))==1 &&
                    calendarDay.month==c2.getInt(c2.getColumnIndex("month")) &&
                    calendarDay.year == c2.getInt(c2.getColumnIndex("year"))){
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
                }else if (c2.getInt(c2.getColumnIndex("repeatY"))==1 &&
                    calendarDay.month==c2.getInt(c2.getColumnIndex("month"))){
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
                }else if (c2.getInt(c2.getColumnIndex("repeatM"))==1&&
                    calendarDay.day==c2.getInt(c2.getColumnIndex("day"))){
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
            taskListOfSelectDateAdapter.setTaskItem(dailyTaskList)
            Log.d("도원","아이템 사이즈 : ${dailyTaskList.size}")
//            Log.d("도원","dailyTaskList2 : ${dailyTaskList} | ")

            c2.close()
        }catch (e : SQLiteException){
            //TBL 이 없는거면 읽어올 데이터도 없다는 것이니 그냥 패쓰해도 문제 없을듯
            Log.d("도원","ee : ${e.localizedMessage}");
        }
//        rcTaskList.adapter
    }



    companion object {
        @JvmStatic
        fun newInstance() =
            TaskListFragment().apply {

            }
    }
}