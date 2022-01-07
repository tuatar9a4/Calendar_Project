package com.dwstyle.calenderbydw.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.dwstyle.calenderbydw.item.HolidayItem
import com.dwstyle.calenderbydw.item.TaskItem

class TaskDatabaseHelper(context : Context?, dbName:String?,factory:SQLiteDatabase.CursorFactory?,version: Int) : SQLiteOpenHelper(context,dbName,factory,version){
// change_icon -> <a href="https://www.streamlinehq.com">Free Pencil 1 PNG icon by Streamline</a>
//    cancle -> <a href="https://www.streamlinehq.com">Free Remove Circle PNG icon by Streamline</a>
    private lateinit var tblName :String

    override fun onCreate(db: SQLiteDatabase?) {
       var sql : String="CREATE TABLE if not exists myTaskTbl ("+
               "_id integer primary key autoincrement,"+
               "year integer,"+
               "month integer,"+
               "day integer,"+
               "week text,"+
               "time integer,"+
               "title text,"+
               "text text,"+
               "notice integer,"+
               "repeatY integer,"+
               "repeatM integer,"+
               "repeatW integer,"+
               "repeatN integer,"+
               "priority integer,"+
               "isHoliday integer,"+
               "expectDay text"+
        ");"
        db?.let { it.execSQL(sql) }
    }

    fun createHolidayTbl(db: SQLiteDatabase,tblName : String){
        val sql : String="CREATE TABLE if not exists ${tblName} ("+
                "_id integer primary key autoincrement,"+
                "year integer,"+
                "month integer,"+
                "day integer,"+
                "title text,"+
                "isRest integer"+
                ");"
        db?.let { it.execSQL(sql) }

    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        try {
            if (oldVersion<4){
                updateColumn(db!!)
            }

        }catch ( e : SQLiteException){
            db!!.execSQL("DROP TABLE IF EXISTS myTaskTbl");
            onCreate(db);
        }
//        val sql : String = "DROP TABLE if exists "+tblName
//        db?.let { it.execSQL(sql) }
    }

    private fun updateColumn(a_db : SQLiteDatabase){
        a_db.execSQL("ALTER TABLE myTaskTbl ADD COLUMN isHoliday integer DEFAULT 0 ;")
    }

    companion object{

        fun deleteTask(_id : String,database : SQLiteDatabase){
            if (database!=null){
                var c2: Cursor =database.rawQuery("DELETE FROM myTaskTbl  WHERE _id == ${_id.toInt()} ",null)
                Log.d("도원","c2 ${c2.count}")
//            var c2:Cursor =database.rawQuery("SELECT * FROM myTaskTbl  WHERE _id = ${_id} ",null)
            }
        }


        fun changeTask(taskItem: TaskItem,database: SQLiteDatabase){
            if (database!=null){
                var c2: Cursor =database.rawQuery(
                    "UPDATE myTaskTbl SET " +
                            "year=${taskItem.year}," +
                            "month=${taskItem.month}," +
                            "day=${taskItem.day}," +
                            "time=${taskItem.time}," +
                            "title='${taskItem.title}'," +
                            "text='${taskItem.text}'," +
                            "week='${taskItem.week}'," +
                            "repeatY=${taskItem.repeatY}," +
                            "repeatM=${taskItem.repeatM}," +
                            "repeatW=${taskItem.repeatW}," +
                            "repeatN=${taskItem.repeatN}," +
                            "notice=${taskItem.notice}," +
                            "priority=${taskItem.priority}," +
                            "expectDay='${taskItem.expectDay}'" +
                            " WHERE _id == ${taskItem._id} ",null)
                while (c2.moveToNext()){
                }
            }

        }

        fun createTask(taskItem: TaskItem,database : SQLiteDatabase){

            val contentValue = ContentValues();
            contentValue.put("year",taskItem.year)
            contentValue.put("month",taskItem.month)
            contentValue.put("day",taskItem.day);
            contentValue.put("week",taskItem.week);
            contentValue.put("time",taskItem.time);
            contentValue.put("title",taskItem.title);
            contentValue.put("text",taskItem.text);
            contentValue.put("repeatY",taskItem.repeatY);
            contentValue.put("repeatM",taskItem.repeatM);
            contentValue.put("repeatW",taskItem.repeatW);
            contentValue.put("repeatN",taskItem.repeatN);
            contentValue.put("notice",taskItem.notice);
            contentValue.put("priority",taskItem.priority)
            contentValue.put("expectDay",taskItem.expectDay)

            database.insert("myTaskTbl",null,contentValue);
        }

        //년마다 반복 task 의 날짜만 (month.day) 찾기
        fun searchDBOfYearRepeat(database : SQLiteDatabase) : Cursor?{
            try {
                var c2: Cursor =
                    database.rawQuery("SELECT month,day,time,title,notice FROM myTaskTbl WHERE repeatY == 1", null);
                return c2
            }catch (e : SQLiteException){
                //TBL 이 없는거면 읽어올 데이터도 없다는 것이니 그냥 패쓰해도 문제 없을듯
            }
            return null
        }

        //달마다 반복하는 Task  찾기
        fun searchDBOfMonthRepeat(database : SQLiteDatabase) : Cursor? {
            try {
                val c2: Cursor =
                    database.rawQuery(
                        "SELECT day,time,title,notice FROM myTaskTbl WHERE repeatM == 1",
                        null
                    );
                return c2
            } catch (e: SQLiteException) {
                //TBL 이 없는거면 읽어올 데이터도 없다는 것이니 그냥 패쓰해도 문제 없을듯

            }
            return null
        }

        //반복 안하는 Task 찾기
        fun searchDBOfNoRepeat(database : SQLiteDatabase) : Cursor?{
            try {
                val c2: Cursor =
                    database.rawQuery("SELECT year,month,day,time,title,notice FROM myTaskTbl WHERE repeatN == 1", null);
                return c2
            }catch (e : SQLiteException){
                //TBL 이 없는거면 읽어올 데이터도 없다는 것이니 그냥 패쓰해도 문제 없을듯

            }
            return null
        }

        //주마다 반복하는 Task 찾기
        fun searchDBOfWeekRepeat(database: SQLiteDatabase) : Cursor?{
            try {
                val c2: Cursor =
                    database.rawQuery("SELECT week,time,title,notice FROM myTaskTbl WHERE repeatW == 1", null);
                return c2
            }catch (e : SQLiteException){
                //TBL 이 없는거면 읽어올 데이터도 없다는 것이니 그냥 패쓰해도 문제 없을듯

            }
            return null
        }

        fun isExistsTable(database: SQLiteDatabase,tableName :String) : Boolean{
            val cursor = database.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name ='${tableName}'" , null);
            cursor.moveToFirst();

            return cursor.count >0
        }

        fun createHoliday(holidayItem: HolidayItem,database : SQLiteDatabase,tblName :String){

            val contentValue = ContentValues();
            contentValue.put("year",holidayItem.year)
            contentValue.put("month",holidayItem.month)
            contentValue.put("day",holidayItem.day);
            contentValue.put("title",holidayItem.title);
            contentValue.put("isRest",holidayItem.isRest)

            database.insert(tblName,null,contentValue);
        }

        fun searchHoliday(database : SQLiteDatabase,year : Int,month :Int,tblName :String) : Cursor?{
            try {
                var c2: Cursor =
                    database.rawQuery("SELECT year,month,day,title FROM $tblName WHERE isRest == 1 AND year==${year} AND month==${month}", null);
                return c2
            }catch (e : SQLiteException){
                //TBL 이 없는거면 읽어올 데이터도 없다는 것이니 그냥 패쓰해도 문제 없을듯
            }
            return null
        }

    }


}