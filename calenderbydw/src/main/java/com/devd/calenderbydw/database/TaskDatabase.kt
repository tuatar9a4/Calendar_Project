package com.devd.calenderbydw.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase


@Database(
    entities = [],
    version = 1,
    exportSchema = false
)
abstract class TaskDatabase : RoomDatabase() {

    companion object{
        fun buildDatabase(context: Context) : TaskDatabase{
            return Room.databaseBuilder(context, TaskDatabase::class.java, "calendar_db")
                // pre-populate the database
                .addCallback(
                    object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // from asset0
                        }
                    }
                )
                .fallbackToDestructiveMigration()
                .build()
        }
    }

}