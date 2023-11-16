package com.devd.calenderbydw.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.devd.calenderbydw.data.local.dao.HolidayDao
import com.devd.calenderbydw.data.local.dao.TaskDao
import com.devd.calenderbydw.data.local.entity.HolidayDbEntity
import com.devd.calenderbydw.data.local.entity.TaskDBEntity

@Database(
    entities = [HolidayDbEntity::class,TaskDBEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun holidayDao() : HolidayDao
    abstract fun taskDao() : TaskDao

    companion object{
        fun buildDatabase(context: Context) : AppDatabase{
            return Room.databaseBuilder(context, AppDatabase::class.java, "calendar_db")
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