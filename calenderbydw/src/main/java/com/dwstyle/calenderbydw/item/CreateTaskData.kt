package com.dwstyle.calenderbydw.item

import android.os.Parcel
import android.os.Parcelable

data class CreateTaskData(
    var _id:Int,
    var year:Int,
    var month: Int,
    var day:Int,
    var week: String?,
    var time:Long,
    var title:String?,
    var text:String?,
    var notice:Int,
    var repeatY:Int,
    var repeatM:Int,
    var repeatW:Int,
    var repeatN:Int,
    var priority:Int,
    var isHoliday:Int,
    var expectDay:String?
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString(),
        parcel.readLong(),
        parcel.readString(),
        parcel.readString(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString()
    ) {
    }


    companion object CREATOR : Parcelable.Creator<CreateTaskData> {
        override fun createFromParcel(parcel: Parcel): CreateTaskData {
            return CreateTaskData(parcel)
        }

        override fun newArray(size: Int): Array<CreateTaskData?> {
            return arrayOfNulls(size)
        }
    }

    override fun describeContents(): Int {
       return 0;
    }

    override fun writeToParcel(p0: Parcel?, p1: Int) {
        p0?.writeInt(_id)
        p0?.writeInt(year)
        p0?.writeInt(month)
        p0?.writeInt(day)
        p0?.writeString(week)
        p0?.writeLong(time)
        p0?.writeString(title)
        p0?.writeString(text)
        p0?.writeInt(notice)
        p0?.writeInt(repeatY)
        p0?.writeInt(repeatM)
        p0?.writeInt(repeatW)
        p0?.writeInt(repeatN)
        p0?.writeInt(priority)
        p0?.writeInt(isHoliday)
        p0?.writeString(expectDay)
    }
}
