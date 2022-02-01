package com.dwstyle.calenderbydw.fragments

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dwstyle.calenderbydw.CreateTaskActivity
import com.dwstyle.calenderbydw.R
import com.dwstyle.calenderbydw.adapters.DateOfListAdapter
import com.dwstyle.calenderbydw.adapters.TaskListOfSelectDateAdapter
import com.dwstyle.calenderbydw.database.TaskDatabaseHelper
import com.dwstyle.calenderbydw.item.DateOfListItem
import com.dwstyle.calenderbydw.item.TaskItem
import com.dwstyle.calenderbydw.utils.CustomAlertDialog
import com.dwstyle.calenderbydw.utils.WidgetUtils
import com.google.android.gms.wearable.Wearable
import com.prolificinteractive.materialcalendarview.CalendarDay
import org.joda.time.DateTime
import org.threeten.bp.ZoneOffset

class TaskListFragment : Fragment() {

    private lateinit var tvDay:TextView
    private lateinit var preDate:Button
    private lateinit var nextDate:Button
    private lateinit var rcSelectTaskList :RecyclerView
    private lateinit var searchDate:ImageView

    private lateinit var tvMonthOfList : TextView
    private lateinit var RCScheduledDate : RecyclerView
    private lateinit var dateOfListAdapter: DateOfListAdapter
    private var dateItems = ArrayList<DateOfListItem>()
    private val taskCntEach=ArrayList<TaskItem>()

    private lateinit var taskListOfSelectDateAdapter :TaskListOfSelectDateAdapter

    private var selectedCalendarDay : CalendarDay?=null

    private lateinit var resultLauncher : ActivityResultLauncher<Intent>

    private var changePos =-1

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
        dbHelper= TaskDatabaseHelper(view.context,"task.db",null,3)

        selectedCalendarDay?.let {
            tvDay.text="${it.year}.${it.month}.${it.day}"
            tvMonthOfList.text="${it.year}.${it.month}"
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
        dateOfListAdapter= DateOfListAdapter(view.context)
        RCScheduledDate.adapter=dateOfListAdapter
        dateOfListAdapter.setOnItemClickListener(object :DateOfListAdapter.OnItemClickListener{
            override fun onItemClickListener(v: View, year: Int, month: Int, day: Int,position :Int) {
                selectedCalendarDay=CalendarDay.from(year,month,day)
                tvDay.text="${selectedCalendarDay?.year}.${selectedCalendarDay?.month}.${selectedCalendarDay?.day}"
                tvMonthOfList.text="${selectedCalendarDay?.year}.${selectedCalendarDay?.month}"
                searchTaskOfSelectedDay(selectedCalendarDay!!)
                dateOfListAdapter.selectPosition(position)
            }
        })

        taskListOfSelectDateAdapter.setOnDeleteItemClickListener(object :TaskListOfSelectDateAdapter.OnDeleteClickListener{
            override fun OnDeleteClick(v: View, item: TaskItem, pos: Int) {
                database=dbHelper.writableDatabase
                CustomAlertDialog(context!!).taskDeleteDialog( DialogInterface.OnClickListener { dialog, which ->
                    TaskDatabaseHelper.deleteTask(item._id.toString(),dbHelper.writableDatabase,context!!,Wearable.getDataClient(context!!))
                    searchTaskOfSelectedDay(selectedCalendarDay!!)
                    context?.let {
                        WidgetUtils.updateWidgetData(it)
                        WidgetUtils.changeDBToBytes(it)
                    }
                    dialog.dismiss()
                })
            }
        }, object : TaskListOfSelectDateAdapter.OnChangeClickListener{

            override fun OnChangeClick(v: View, item: TaskItem, pos: Int) {
                val intent = Intent(context, CreateTaskActivity::class.java)
                intent.putExtra("type","change")
                intent.putExtra("taskForChange",item)
                resultLauncher.launch(intent)
                changePos=pos

            }

        })

        resultLauncher=registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
            ActivityResultCallback {
                //Task 등록 데이터 받는 곳
                if (it.resultCode == AppCompatActivity.RESULT_OK){
                    val intent = it.data
//                    TaskDatabaseHelper.changeTask(intent.getParcelableExtra<TaskItem>("changeItem")!!,dbHelper.writableDatabase)
                    if (intent?.getParcelableExtra<TaskItem>("changeItem")!=null){
                        TaskDatabaseHelper.changeTask(intent.getParcelableExtra<TaskItem>("changeItem")!!,dbHelper.writableDatabase,requireContext(),
                            Wearable.getDataClient(context))
                        searchTaskOfSelectedDay(selectedCalendarDay!!)
//                        if (changePos!=-1){
//                            dateOfListAdapter.notifyItemChanged(changePos)
//                        }
                        context?.let {
                            WidgetUtils.updateWidgetData(it)
                            WidgetUtils.changeDBToBytes(it)
                        }
                    }
                }
            })

        searchDate.setOnClickListener{
            val datePickerDialog = DatePickerDialog(view.context, DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                setCalendarDay(CalendarDay.from(year,month+1,dayOfMonth))

            },selectedCalendarDay?.year!!, selectedCalendarDay?.month!!-1,selectedCalendarDay?.day!!)
            datePickerDialog.show()

        }
        return view
    }

    fun initView(view :View){
        tvDay=view.findViewById(R.id.tvDay)
        preDate=view.findViewById(R.id.preDate)
        nextDate=view.findViewById(R.id.nextDate)
        rcSelectTaskList=view.findViewById(R.id.rcSelectTaskList)
        rcSelectTaskList.layoutManager=LinearLayoutManager(view.context,LinearLayoutManager.VERTICAL,false)
        searchDate=view.findViewById(R.id.searchDate)


        RCScheduledDate=view.findViewById(R.id.RCScheduledDate)
        RCScheduledDate.layoutManager=LinearLayoutManager(view.context,LinearLayoutManager.HORIZONTAL,false)
        tvMonthOfList=view.findViewById(R.id.tvMonthOfList)
        RCScheduledDate.addItemDecoration(HorizontalSpaceDecorate(30))

    }

    fun getSelectDateInfo() = selectedCalendarDay

    fun setCalendarDay(calendarDay: CalendarDay){
        dateItems.clear()
        selectedCalendarDay=calendarDay
        tvDay.text="${selectedCalendarDay?.year}.${selectedCalendarDay?.month}.${selectedCalendarDay?.day}"
        tvMonthOfList.text="${selectedCalendarDay?.year}.${selectedCalendarDay?.month}"
        searchTaskOfSelectedDay(selectedCalendarDay!!)
        setTopDateList()
    }

    fun setTopDateList(){
        val calendarDay=selectedCalendarDay!!
        var currentMonthDate = DateTime(calendarDay.date.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli())
        currentMonthDate=currentMonthDate.withDayOfMonth(1)
        var oldMonth :Int=0;
        while (true){
            taskCntEach.clear()
            searchTaskInRepeatWeek(currentMonthDate.dayOfWeek,taskCntEach)
            searchTaskInDay(currentMonthDate.year,currentMonthDate.monthOfYear,currentMonthDate.dayOfMonth,taskCntEach)
            val taskCnt= taskCntEach.size
            val temp =DateOfListItem(
                currentMonthDate.dayOfWeek.toString(),
                currentMonthDate.dayOfMonth.toString(),
                currentMonthDate.year.toString(),
                currentMonthDate.monthOfYear.toString(),
                taskCnt
            )
            dateItems.add(temp)
            oldMonth=currentMonthDate.monthOfYear
            currentMonthDate=currentMonthDate.plusDays(1)
            if (oldMonth!=currentMonthDate.monthOfYear){
                break
            }
        }
        dateOfListAdapter.setDateItems(dateItems)
        RCScheduledDate.scrollToPosition(calendarDay.day-1)
        dateOfListAdapter.selectPosition((calendarDay.day-1))
    }
    fun searchTaskOfSelectedDay(calendarDay: CalendarDay){
        dailyTaskList.clear()
        searchTaskInRepeatWeek(calendarDay.date.dayOfWeek.value,dailyTaskList)
        searchTaskInDay(calendarDay.year,calendarDay.month,calendarDay.day,dailyTaskList)
        taskListOfSelectDateAdapter.setTaskItem(dailyTaskList)
    }

    fun notifydataChange(){
        selectedCalendarDay?.let {
            searchTaskOfSelectedDay(it)
        }
    }

    //매주 반복
    @SuppressLint("Range")
    private fun searchTaskInRepeatWeek(week : Int, taskList : ArrayList<TaskItem>){
        database=dbHelper.readableDatabase

        try {
            val c2: Cursor =
                database.rawQuery("SELECT * FROM myTaskTbl WHERE week != '0&0&0&0&0&0&0' ", null);
            while (c2.moveToNext()) {
                val tempStr: List<String> = c2.getString(c2.getColumnIndex("week")).split("&")
                for (a in tempStr.indices) {
                    if (tempStr[a].equals("1")) {
                        var pos = a;
                        if (a == 0) pos = 7
                        if (pos == week) {
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
                            taskList.add(tempTask)
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
    @SuppressLint("Range")
    private fun searchTaskInDay(year:Int, month:Int, day:Int, taskList : ArrayList<TaskItem>){
        database=dbHelper.readableDatabase
        try {
            val c2: Cursor = database.rawQuery("SELECT * FROM myTaskTbl WHERE day = ${day} AND week == '0&0&0&0&0&0&0' ",null);
            while (c2.moveToNext()){
                if (c2.getInt(c2.getColumnIndex("repeatN"))==1 &&
                    month==c2.getInt(c2.getColumnIndex("month")) &&
                    year == c2.getInt(c2.getColumnIndex("year"))){
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
                    taskList.add(tempTask)
                }else if (c2.getInt(c2.getColumnIndex("repeatY"))==1 &&
                    month==c2.getInt(c2.getColumnIndex("month"))){
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
                    taskList.add(tempTask)
                }else if (c2.getInt(c2.getColumnIndex("repeatM"))==1&&
                    day==c2.getInt(c2.getColumnIndex("day"))){
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
                    taskList.add(tempTask)
                }

            }
//            Log.d("도원","dailyTaskList2 : ${dailyTaskList} | ")

            c2.close()
        }catch (e : SQLiteException){
            //TBL 이 없는거면 읽어올 데이터도 없다는 것이니 그냥 패쓰해도 문제 없을듯
            Log.d("도원","ee : ${e.localizedMessage}");
        }
//        rcTaskList.adapter
    }

    inner class HorizontalSpaceDecorate(private val horizontalSpaceHeight: Int) : RecyclerView.ItemDecoration(){
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            super.getItemOffsets(outRect, view, parent, state)
            outRect.right = horizontalSpaceHeight

        }
    }


    companion object {
        @JvmStatic
        fun newInstance() =
            TaskListFragment().apply {

            }
    }
}