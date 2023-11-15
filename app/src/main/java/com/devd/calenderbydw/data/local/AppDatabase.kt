package com.devd.calenderbydw.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.devd.calenderbydw.data.local.dao.HolidayDao
import com.devd.calenderbydw.data.local.db.HolidayDbData

@Database(
    entities = [HolidayDbData::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun holidayDao() : HolidayDao

    companion object{
        fun buildDatabase(context: Context) : AppDatabase{
            return Room.databaseBuilder(context, AppDatabase::class.java, "calendar_db")
                // pre-populate the database
                .fallbackToDestructiveMigration()
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