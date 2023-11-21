package com.devd.calenderbydw.data.local.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

/**
 * RepeatType 0:반복 없음,1: 매일, 2:매주, 3:매달, 4:매년
 */
@Parcelize
@Entity(
    indices = [Index(value = ["repeatType"])],
    tableName = "task_table"
)
data class TaskDBEntity (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo("id")
    val id : Int=0,
    @ColumnInfo("year")
    var year :String ="0",
    @ColumnInfo("month")
    var month :String ="0",
    @ColumnInfo("day")
    var day :String ="0",
    @ColumnInfo("weekCount")
    var weekCount :Int =1,
    @ColumnInfo("title")
    var title :String="",
    @ColumnInfo("contents")
    var contents :String?=null,
    @ColumnInfo("repeatType")
    var repeatType :Int =NO_REPEAT,
    @ColumnInfo("stickerOneID")
    val stickerOneID :String? =null,
    @ColumnInfo("stickerTwoID")
    val stickerTwoID :String? =null,
    @ColumnInfo("stickerThreeID")
    val stickerThreeID :String? =null,
    @ColumnInfo("createDate")
    var createDate :Long =0,
) : Parcelable {
    companion object{
        const val NO_REPEAT=0
        const val DAILY_REPEAT=1
        const val WEEK_REPEAT=2
        const val MONTH_REPEAT=3
        const val YEAR_REPEAT=4
    }
}