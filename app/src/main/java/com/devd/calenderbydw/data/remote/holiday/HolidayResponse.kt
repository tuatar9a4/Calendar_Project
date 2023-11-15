package com.devd.calenderbydw.data.remote.holiday

import com.devd.calenderbydw.data.local.db.HolidayDbData
import com.tickaroo.tikxml.annotation.Element
import com.tickaroo.tikxml.annotation.PropertyElement
import com.tickaroo.tikxml.annotation.Xml

@Xml(name = "response")
data class HolidayResponse(
    @Element(name = "body")
    val body: Body
)

@Xml(name = "body")
data class Body(
    @Element(name = "items")
    val items: Items
)

@Xml(name = "items")
data class Items(
    @Element(name = "item")
    val item: List<HolidayItem>
)

@Xml
data class HolidayItem(
    @PropertyElement(name = "dateKind") val dateKind: String,
    @PropertyElement(name = "dateName") val holidayName: String,
    @PropertyElement(name = "isHoliday") val holidayStr: String,
    @PropertyElement(name = "locdate") val holidayYearToday: String,
) {
    val isHolidayBoolean: Boolean
        get() = holidayStr == "Y"

    fun toHolidayDbItem()= HolidayDbData(
        holidayName = holidayName,
        holidayYear = holidayYearToday.substring(0,4),
        holidayYearToday = holidayYearToday.toInt(),
        isHolidayRest = isHolidayBoolean
    )
}