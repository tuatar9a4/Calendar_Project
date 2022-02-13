package com.devd.calenderbydw.item

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class TaskItem (
    var _id :Int,
    var year :Int,
    var month : Int,
    var day :Int,
    var week :String,
    var time:Long,
    var title :String,
    var text :String,
    var notice :Int,
    var repeatY :Int,
    var repeatM :Int,
    var repeatW :Int,
    var repeatN :Int,
    var priority :Int,
    var isHoliday :Int,
    var expectDay :String
        ) :Parcelable