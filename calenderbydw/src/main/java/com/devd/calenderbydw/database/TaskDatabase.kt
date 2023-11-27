package com.devd.calenderbydw.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.AutoMigrationSpec
import androidx.sqlite.db.SupportSQLiteDatabase
import com.devd.calenderbydw.data.dao.TaskDao
import com.devd.calenderbydw.data.local.entity.TaskDBEntity


@Database(
    entities = [TaskDBEntity::class],
    version = 1,
    exportSchema = false
)
abstract class TaskDatabase : RoomDatabase() {
    abstract fun taskDao() : TaskDao
    companion object{

        fun buildDatabase(context: Context) : TaskDatabase{
            return Room.databaseBuilder(context, TaskDatabase::class.java, "wear_calendar_task")
                // pre-populate the database
                .addCallback(
                    object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // from asset0
                        }
                    }
                )
                .build()
        }
    }

}