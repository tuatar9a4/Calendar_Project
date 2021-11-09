package com.dwstyle.calenderbydw.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class TaskDatabaseHelper(context : Context?, dbName:String?,factory:SQLiteDatabase.CursorFactory?,version: Int) : SQLiteOpenHelper(context,dbName,factory,version){

    private lateinit var tblName :String

    override fun onCreate(db: SQLiteDatabase?) {
        Log.d("도원","tblName onCreate : ${tblName}");
       var sql : String="CREATE TABLE if not exists "+tblName+" ("+
               "_id integer primary key autoincrement,"+
               "year integer,"+
               "month integer,"+
               "day integer,"+
               "week text,"+
               "time integer,"+
               "text text,"+
               "repeatY integer,"+
               "repeatM integer,"+
               "repeatW integer,"+
               "repeatN integer,"+
               "notice integer,"+

        ");"
        db?.let { it.execSQL(sql) }
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        Log.d("도원","tblName  onUpgrade: ${tblName}");
        val sql : String = "DROP TABLE if exists "+tblName
        db?.let { it.execSQL(sql) }
    }

    fun createMonthTBL(tblName:String){
        this.tblName=tblName;
    }



}