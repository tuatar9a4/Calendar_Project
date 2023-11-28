package com.devd.calenderbydw.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("diary_table")
data class DiaryEntity(
    @ColumnInfo("id")
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo("year")
    val year: Int = 2021,
    @ColumnInfo("month")
    val month: Int = 3,
    @ColumnInfo("day")
    val day: Int = 22,
    @ColumnInfo("weatherType")
    val weatherType: Int = 0,
    @ColumnInfo("customWeather")
    val customWeather: String? = null,
    @ColumnInfo("feelingType")
    val feelingType: Int = 0,
    @ColumnInfo("customFeel")
    val customFeel: String? = null,
    @ColumnInfo("diaryContents")
    val diaryContents: String = "",
    @ColumnInfo("createDate")
    val createDate : Long =0L
)