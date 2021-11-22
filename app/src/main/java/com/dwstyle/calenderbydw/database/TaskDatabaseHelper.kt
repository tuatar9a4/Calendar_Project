package com.dwstyle.calenderbydw.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
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
               "expectDay text"+
        ");"
        db?.let { it.execSQL(sql) }
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        try {
            if (oldVersion<3){
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
        a_db.execSQL("ALTER TABLE myTaskTbl ADD COLUMN title TEXT DEFAULT '' ")
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
                            "title=\"${taskItem.title}\"," +
                            "text=\"${taskItem.text}\"," +
                            "week='${taskItem.week}'," +
                            "repeatY=${taskItem.repeatY}," +
                            "repeatM=${taskItem.repeatM}," +
                            "repeatW=${taskItem.repeatW}," +
                            "repeatN=${taskItem.repeatN}," +
                            "notice=${taskItem.notice}," +
                            "priority=${taskItem.priority}," +
                            "expectDay='${taskItem.expectDay}'" +
                            " WHERE _id == ${taskItem._id} ",null)
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

    }


}