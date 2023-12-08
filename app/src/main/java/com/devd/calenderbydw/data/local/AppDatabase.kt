package com.devd.calenderbydw.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.devd.calenderbydw.data.local.dao.CalendarDao
import com.devd.calenderbydw.data.local.dao.DiaryDao
import com.devd.calenderbydw.data.local.dao.HolidayDao
import com.devd.calenderbydw.data.local.dao.TaskDao
import com.devd.calenderbydw.data.local.entity.CalendarDayConverters
import com.devd.calenderbydw.data.local.entity.CalendarMonthEntity
import com.devd.calenderbydw.data.local.entity.DiaryEntity
import com.devd.calenderbydw.data.local.entity.HolidayDbEntity
import com.devd.calenderbydw.data.local.entity.TaskDBEntity

@Database(
    entities = [HolidayDbEntity::class,TaskDBEntity::class, CalendarMonthEntity::class,DiaryEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(CalendarDayConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun holidayDao() : HolidayDao
    abstract fun taskDao() : TaskDao
    abstract fun calendarDao() : CalendarDao
    abstract fun diaryDao() : DiaryDao

    companion object{
        fun buildDatabase(context: Context) : AppDatabase{
            return Room.databaseBuilder(context, AppDatabase::class.java, "calendar_db")
                // pre-populate the database
                .allowMainThreadQueries()
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