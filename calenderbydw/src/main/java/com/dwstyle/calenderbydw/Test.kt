package com.dwstyle.calenderbydw

import android.util.Log
import androidx.core.content.ContextCompat
import androidx.wear.tiles.*
import androidx.wear.tiles.DimensionBuilders.*
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture

import org.joda.time.DateTime
import org.joda.time.DateTimeConstants

private const val RESOURCES_VERSION = "1"

class Test : TileService(){

    private var calendarTopTextSize=8f
    private var calendarSpace =0f
    private var calendarWeekTextSize=10f
    private var daySpace=9f

    override fun onTileRequest(requestParams: RequestBuilders.TileRequest): ListenableFuture<TileBuilders.Tile> {

        val tile = TileBuilders.Tile.Builder()
            .setResourcesVersion(RESOURCES_VERSION)
            .setFreshnessIntervalMillis(1000*60*60)
            .setTimeline(
                TimelineBuilders.Timeline.Builder()
                // We add a single timeline entry when our layout is fixed, and
                // we don't know in advance when its contents might change.
                .addTimelineEntry(
                    TimelineBuilders.TimelineEntry.Builder().setLayout(
                        LayoutElementBuilders.Layout.Builder().setRoot(
                            myLayout(requestParams.deviceParameters!!)
                        ).build()
                    ).build()
                    // .setLayout(...).build()
                ).build()
            ).build()
        return Futures.immediateFuture(tile)
    }

    override fun onResourcesRequest(requestParams: RequestBuilders.ResourcesRequest): ListenableFuture<ResourceBuilders.Resources> {
        return Futures.immediateFuture(ResourceBuilders.Resources.Builder().setVersion(
            RESOURCES_VERSION).addIdToImageMapping("dot_image",ResourceBuilders.ImageResource.Builder()
                .setAndroidResourceByResId(ResourceBuilders.AndroidImageResourceByResId.Builder()
                    .setResourceId(R.drawable.testdot)
                    .build()).build()).build())
    }


    public fun myLayout(deviceParameters: DeviceParametersBuilders.DeviceParameters) : LayoutElementBuilders.LayoutElement{
        getCalendar()
        var aa=LayoutElementBuilders.Row.Builder()
        aa .setWidth(wrap())
        aa .setHeight(expand())
        aa.setVerticalAlignment(LayoutElementBuilders.VERTICAL_ALIGN_TOP)
        for (a in 0..10){
            aa.addContent(
                LayoutElementBuilders.Text.Builder()
                    .setText(a.toString())
                    .build()
            )
        }

        val calendarLayout =LayoutElementBuilders.Column.Builder().setWidth(wrap())
        calendarLayout.setHeight(expand())
            .addContent(LayoutElementBuilders.Spacer.Builder().setHeight(dp(30f)).build())
            .addContent(
                //년.월 yyyy.MM
                LayoutElementBuilders.Text.Builder().setMultilineAlignment(LayoutElementBuilders.TextAlignmentProp.Builder().setValue(
                    LayoutElementBuilders.TEXT_ALIGN_CENTER).build())
                        .setText(calendarDate)
                        .setFontStyle(LayoutElementBuilders.FontStyles.caption1(deviceParameters)
                            .setSize(sp(calendarTopTextSize))
                            .build()

                    ).build()
            ).addContent(
                LayoutElementBuilders.Spacer.Builder().setHeight(dp(5f)).build()
            )
        var weekText = arrayOf("Sun","Mon","The","Wen","Thu","Fri","Sat");
        var rowBuilderBox =  LayoutElementBuilders.Row.Builder().setWidth(wrap());
        var weekFontStyle =LayoutElementBuilders.FontStyles.caption1(deviceParameters)
            .setSize(sp(calendarWeekTextSize))

        for (a in weekText){
            if (a.equals("Sun")) weekFontStyle.setColor(ColorBuilders.argb(ContextCompat.getColor(baseContext,R.color.sunColor)))
            else if (a.equals("Sat"))weekFontStyle.setColor(ColorBuilders.argb(ContextCompat.getColor(baseContext,R.color.satColor)))
            else weekFontStyle.setColor(ColorBuilders.argb(ContextCompat.getColor(baseContext,R.color.currnetMonthDayColor)))

            rowBuilderBox.addContent(
                LayoutElementBuilders.Box.Builder().setWidth(dp(20f)).addContent(
                    LayoutElementBuilders.Text.Builder()
                        .setMultilineAlignment(LayoutElementBuilders.TextAlignmentProp.Builder().setValue(
                            LayoutElementBuilders.TEXT_ALIGN_CENTER).build())
                        .setText(a)
                        .setFontStyle(
                            weekFontStyle.build()
                        ).build()
                ).build()
            ).build()
            if (!a.equals("Sat"))rowBuilderBox.addContent(LayoutElementBuilders.Spacer.Builder().setWidth(dp(calendarSpace)).build())
        }
        calendarLayout.addContent(rowBuilderBox.build())
        calendarLayout.addContent(LayoutElementBuilders.Spacer.Builder().setHeight(dp(5f)).build())

        var count =0;
        var isItem =false;
        lateinit var ttt : LayoutElementBuilders.Row.Builder
        lateinit var scheduleItem : LayoutElementBuilders.Image.Builder
        lateinit var modifi :ModifiersBuilders

        for (a in allDayOfMonth){
            if (count==0){
                ttt=LayoutElementBuilders.Row.Builder().setWidth(wrap());
            }
//            isItem = count==3
            var dayTextColor =if(!a.split(".")[0].equals(firstDayOfMonth.split(".")[0]))R.color.preMonthDayColor
                                else if (a.equals(currentDate)) R.color.toDayColor
                                else R.color.currnetMonthDayColor
            if (isItem){
                scheduleItem=LayoutElementBuilders.Image.Builder().setHeight(dp(2f)).setWidth(dp(2f)).setResourceId("dot_image")
            }else{
                scheduleItem=LayoutElementBuilders.Image.Builder().setHeight(dp(0f)).setWidth(dp(0f)).setResourceId("dot_image")
            }
            if (a.equals(currentDate)){
                ttt.addContent(
                    LayoutElementBuilders.Box.Builder().setWidth(dp(20f)).setHeight(dp(20f))
                        .setVerticalAlignment(LayoutElementBuilders.VERTICAL_ALIGN_TOP).addContent(
                            LayoutElementBuilders.Column.Builder()
                                .addContent(
                                    LayoutElementBuilders.Text.Builder()
                                        .setText(a.split(".")[1])
                                        .setFontStyle(
                                            LayoutElementBuilders.FontStyles.caption1(deviceParameters)
                                                .setColor(ColorBuilders.argb(ContextCompat.getColor(baseContext,dayTextColor)))
                                                .setSize(sp(calendarWeekTextSize))
                                                .build()
                                        ).build()
                                ).addContent(
                                    scheduleItem.build()
                                ).build()
                        ).build())
            }else{
                ttt.addContent(
                    LayoutElementBuilders.Box.Builder().setWidth(dp(20f)).setHeight(dp(20f))
                        .setVerticalAlignment(LayoutElementBuilders.VERTICAL_ALIGN_TOP).addContent(
                            LayoutElementBuilders.Column.Builder()
                                .addContent(
                                    LayoutElementBuilders.Text.Builder()
                                        .setText(a.split(".")[1])
                                        .setFontStyle(
                                            LayoutElementBuilders.FontStyles.caption1(deviceParameters)
                                                .setColor(ColorBuilders.argb(ContextCompat.getColor(baseContext,dayTextColor)))
                                                .setSize(sp(calendarWeekTextSize))
                                                .build()
                                        ).build()
                                ).addContent(
                                    scheduleItem.build()
                                ).build()
                        ).build())
            }
            count++;
            if (count==7){
                calendarLayout.addContent(ttt.build())
                count=0
            }
        }
        return calendarLayout.build()
    }

    private lateinit var calendarDate : String
    private lateinit var firstDayOfMonth: String
    private lateinit var allDayOfMonth : List<String>
    private lateinit var currentDate :String
    private lateinit var currentDate1 :String
    fun getCalendar(){
        var start:Long =DateTime().withDayOfMonth(1).withTimeAtStartOfDay().millis

        var plus =DateTime(start).plusMonths(1).millis
        calendarDate=DateTime(start).toString("yyyy.MM")
        firstDayOfMonth = DateTime(start).toString("MM.dd")
        allDayOfMonth = getMonthList(DateTime(start))
        currentDate = DateTime(System.currentTimeMillis()).toString("MM.dd")
        currentDate1 = System.currentTimeMillis().toString()

//        Log.d("도원","이번달 calendarDate : $calendarDate // test : $firstDayOfMonth //test2 : $allDayOfMonth  // ")
//        Log.d("도원","이번달 currentDate : $currentDate ")
//        Log.d("도원","다음달 calendarDate : ${DateTime(plus).toString("yyyy.MM")} // test : ${DateTime(plus).toString("MM.dd")} //test2 : ${getMonthList(DateTime(plus))}  // ")

    }

    fun getMonthList(dateTime: DateTime): List<String> {
        val list = mutableListOf<String>()

        val date = dateTime.withDayOfMonth(1)
        val prev = getPrevOffSet(date)

        val startValue = date.minusDays(prev)

        val totalDay = DateTimeConstants.DAYS_PER_WEEK * 6

        for (i in 0 until totalDay) {
            list.add(DateTime(startValue.plusDays(i)).toString("MM.dd"))
        }

        return list
    }
    /**
     * 해당 calendar 의 이전 달의 일 갯수를 반환한다.
     */
    private fun getPrevOffSet(dateTime: DateTime): Int {
        var prevMonthTailOffset = dateTime.dayOfWeek

        if (prevMonthTailOffset >= 7) prevMonthTailOffset %= 7

        return prevMonthTailOffset
    }



}