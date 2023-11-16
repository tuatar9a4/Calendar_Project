package com.devd.calenderbydw.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * RepeatType 0:반복 없음,1: 매일, 2:매주, 3:매달, 4:매년
 */
@Entity(tableName = "task_table")
data class TaskDBEntity (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo("id")
    val id : Int=0,
    @ColumnInfo("year")
    val year :Int =0,
    @ColumnInfo("month")
    val month :Int =0,
    @ColumnInfo("day")
    val day :Int =0,
    @ColumnInfo("title")
    val title :String="",
    @ColumnInfo("contents")
    val contents :String?=null,
    @ColumnInfo("repeatType")
    val repeatType :Int =NO_REPEAT,
    @ColumnInfo("stickerOneID")
    val stickerOneID :String? =null,
    @ColumnInfo("stickerTwoID")
    val stickerTwoID :String? =null,
    @ColumnInfo("stickerThreeID")
    val stickerThreeID :String? =null,
){
    companion object{
        const val NO_REPEAT=0
        const val DAILY_REPEAT=1
        const val WEEK_REPEAT=2
        const val MONTH_REPEAT=3
        const val YEAR_REPEAT=4
    }
}