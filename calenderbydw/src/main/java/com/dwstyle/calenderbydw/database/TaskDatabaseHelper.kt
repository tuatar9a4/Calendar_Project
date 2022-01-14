package com.dwstyle.calenderbydw.database

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class TaskDatabaseHelper(context : Context?, dbName:String?,factory:SQLiteDatabase.CursorFactory?,version: Int) : SQLiteOpenHelper(context,dbName,factory,version){

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
    }

    private fun updateColumn(a_db : SQLiteDatabase){
        a_db.execSQL("ALTER TABLE myTaskTbl ADD COLUMN title TEXT DEFAULT '' ")
    }

    fun createMonthTBL(tblName:String){
        this.tblName=tblName;
    }

    companion object {


        fun isExistsTable(database: SQLiteDatabase,tableName :String) : Boolean{
            val cursor = database.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name ='${tableName}'" , null);
            cursor.moveToFirst();

            return cursor.count >0
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