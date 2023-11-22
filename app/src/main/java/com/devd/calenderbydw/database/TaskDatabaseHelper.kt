package com.devd.calenderbydw.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

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

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        try {
            if (oldVersion<3){
                updateColumn(db!!)
            }

        }catch ( e : SQLiteException){
            Log.d("ehdnjs","sql exception ${e.localizedMessage}")
            db!!.execSQL("DROP TABLE IF EXISTS myTaskTbl");
            onCreate(db);
        }
//        val sql : String = "DROP TABLE if exists "+tblName
//        db?.let { it.execSQL(sql) }
    }

    private fun updateColumn(a_db : SQLiteDatabase){
        a_db.execSQL("ALTER TABLE myTaskTbl ADD COLUMN isHoliday integer DEFAULT 0;")
    }


}