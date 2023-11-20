package com.devd.calenderbydw.utils

import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.annotation.IntRange
import org.joda.time.DateTime
import org.joda.time.DateTimeConstants

object ConstVariable {

    const val HOLIDAY_API_URL = "http://apis.data.go.kr/"

    const val WEEK_SUN_DAY=1
    const val WEEK_MON_DAY=2
    const val WEEK_TUE_DAY=3
    const val WEEK_WEN_DAY=4
    const val WEEK_THU_DAY=5
    const val WEEK_FRI_DAY=6
    const val WEEK_SAT_DAY=7

    const val ERROR_UK1001 = "UK1001"


    const val WEEKS_PER_MONTH = 6

    /**
     * 같은 달인지 체크
     */
    fun isSameMonth(first: DateTime, second: DateTime): Boolean =
        first.year == second.year && first.monthOfYear == second.monthOfYear

    /**
     * 해당 요일의 색깔을 반환한다.
     * 일요일 -> 빨간색
     * 토요일 -> 파란색
     * 나머지 -> 검정색
     */
    @ColorInt
    fun getDateColor(@IntRange(from=1, to=7) dayOfWeek: Int): Int {
        return when (dayOfWeek) {
            /* 토요일은 파란색 */
            DateTimeConstants.SATURDAY -> Color.parseColor("#2962FF")
            /* 일요일 빨간색 */
            DateTimeConstants.SUNDAY -> Color.parseColor("#D32F2F")
            /* 그 외 검정색 */
            else -> Color.parseColor("#000000")
        }
    }
}