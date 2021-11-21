package com.dwstyle.calenderbydw.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class TaskDatabaseHelper(context : Context?, dbName:String?,factory:SQLiteDatabase.CursorFactory?,version: Int) : SQLiteOpenHelper(context,dbName,factory,version){

    private lateinit var tblName :String

    override fun onCreate(db: SQLiteDatabase?) {
       var sql : String="CREATE TABLE if not exists "+tblName+" ("+
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

    fun createMonthTBL(tblName:String){
        this.tblName=tblName;
    }

    private fun updateColumn(a_db : SQLiteDatabase){
        a_db.execSQL("ALTER TABLE myTaskTbl ADD COLUMN title TEXT DEFAULT '' ")
    }



}