package com.dwstyle.calenderbydw

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.wear.tiles.*
import androidx.wear.tiles.DimensionBuilders.*
import com.dwstyle.calenderbydw.database.TaskDatabaseHelper
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture

import org.joda.time.DateTime
import org.joda.time.DateTimeConstants

private const val RESOURCES_VERSION = "1"

class CalendarTile : TileService(){

    private var calendarTopTextSize=8f
    private var calendarSpace =0f
    private var calendarWeekTextSize=10f
    private var daySpace=9f

    private lateinit var dbHelper : TaskDatabaseHelper
    private lateinit var database : SQLiteDatabase

    private var isDot = false;
    private val dotDaySetY =HashSet<String>()
    private val dotDaySetM =HashSet<String>()
    private val dotDaySetN =HashSet<String>()
    private val dotDaySetW =ArrayList<String>()
    private var weekInt =0

    override fun onTileRequest(requestParams: RequestBuilders.TileRequest): ListenableFuture<TileBuilders.Tile> {

        // setFreshnessIntervalMillis : 타일의 새로 고침 간격 (밀리초) / 날짜의 변화가 있어야해서 1시간에 한번 씩 새로고침함
        // setTimeline : 화면을 구성을 set 함 보통 LayoutElementBuilders.LayoutElement 를 반환하는 method를 만든 후 거기에서 타일을 구성함

        val tile = TileBuilders.Tile.Builder()
            .setResourcesVersion(RESOURCES_VERSION)
            .setFreshnessIntervalMillis(1000*60*60)
            .setTimeline(
                TimelineBuilders.Timeline.Builder()
                .addTimelineEntry(
                    TimelineBuilders.TimelineEntry.Builder().setLayout(
                        LayoutElementBuilders.Layout.Builder().setRoot(
                            myLayout(requestParams.deviceParameters!!)
                        ).build()
                    ).build()
                ).build()
            ).build()
        return Futures.immediateFuture(tile)
    }

    //타일에서 drawable에 있는 Resource를 사용하려면 onResourcesRequest에서 해당 정도를 요청해서 얻어와야함
    //이경우에는 dot_image 를 요청하는 상황  addIdToImageMapping 의 id 가 가져온 이미지의 id 값이 됨
    override fun onResourcesRequest(requestParams: RequestBuilders.ResourcesRequest): ListenableFuture<ResourceBuilders.Resources> {
        return Futures.immediateFuture(ResourceBuilders.Resources.Builder().setVersion(
            RESOURCES_VERSION).addIdToImageMapping("dot_image",ResourceBuilders.ImageResource.Builder()
                .setAndroidResourceByResId(ResourceBuilders.AndroidImageResourceByResId.Builder()
                    .setResourceId(R.drawable.testdot)
                    .build()).build()).build())
    }


    // 타일의 구성을 위한 method
    public fun myLayout(deviceParameters: DeviceParametersBuilders.DeviceParameters) : LayoutElementBuilders.LayoutElement{
        //달력 구성을 위한 정보를 미리 가져옴
        getCalendar()

        // Column : 타일의 구성 요소를 열(세로) 로 정렬
        // 첫 addContent : (Spacer) 상단에서 30f 만큼의 공간을 만든다 / 워치가 원형이다 보니 중앙으로 구성을 맞추기 위함
        // 둘 addContent : (Text) 글자를 쓰는 Builder / TextAlignmentProp 를 사용하여 text 를 중앙 정렬한다.
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
            val strs :List<String> =a.split(".")
            val strMonthDay ="${strs[0]}.${strs[1]}"
            if (count==0){
                ttt=LayoutElementBuilders.Row.Builder().setWidth(wrap());
            }
//            isItem = count==3
            var dayTextColor =if(!strMonthDay.split(".")[0].equals(firstDayOfMonth.split(".")[0]))R.color.preMonthDayColor
                                else if (strMonthDay.equals(currentDate)) R.color.toDayColor
                                else R.color.currnetMonthDayColor
            isDot=false
            weekInt=0
            //점찍는곳
            for (str in dotDaySetY){
                if (str.equals(strMonthDay)) {
                    isDot=true
                    scheduleItem=LayoutElementBuilders.Image.Builder().setHeight(dp(2f)).setWidth(dp(2f)).setResourceId("dot_image")
                }
            }
            for (str in dotDaySetM){
                if (str.equals(strMonthDay.split(".")[1])) {
                    isDot=true
                    scheduleItem=LayoutElementBuilders.Image.Builder().setHeight(dp(2f)).setWidth(dp(2f)).setResourceId("dot_image")
                }
            }
            for (str in dotDaySetN){
                Log.d("도원","${str}  || ${strMonthDay}")
                if (str.equals(strMonthDay)) {
                    isDot=true
                    scheduleItem=LayoutElementBuilders.Image.Builder().setHeight(dp(2f)).setWidth(dp(2f)).setResourceId("dot_image")
                }
            }
            if (strs[2]=="Sun")weekInt=7
            if (strs[2]=="Mon")weekInt=1
            if (strs[2]=="Tue")weekInt=2
            if (strs[2]=="Wed")weekInt=3
            if (strs[2]=="Thu")weekInt=4
            if (strs[2]=="Fri")weekInt=5
            if (strs[2]=="Sat")weekInt=6
            if (weekRepeat.contains(weekInt.toString())){
                isDot=true
                scheduleItem=LayoutElementBuilders.Image.Builder().setHeight(dp(2f)).setWidth(dp(2f)).setResourceId("dot_image")
            }

            if (!isDot){
                scheduleItem=LayoutElementBuilders.Image.Builder().setHeight(dp(0f)).setWidth(dp(0f)).setResourceId("dot_image")
            }

            if (strMonthDay.equals(currentDate)){
                ttt.addContent(
                    LayoutElementBuilders.Box.Builder().setWidth(dp(20f)).setHeight(dp(20f))
                        .setVerticalAlignment(LayoutElementBuilders.VERTICAL_ALIGN_TOP).addContent(
                            LayoutElementBuilders.Column.Builder()
                                .addContent(
                                    LayoutElementBuilders.Text.Builder()
                                        .setText(strMonthDay.split(".")[1])
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
                                        .setText(strMonthDay.split(".")[1])
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

        getDatDayFromDatabase(calendarDate.split(".").get(0))

//        Log.d("도원","이번달 calendarDate : $calendarDate // test : $firstDayOfMonth //test2 : $allDayOfMonth  // ")
//        Log.d("도원","이번달 currentDate : $currentDate ")
//        Log.d("도원","다음달 calendarDate : ${DateTime(plus).toString("yyyy.MM")} // test : ${DateTime(plus).toString("MM.dd")} //test2 : ${getMonthList(DateTime(plus))}  // ")
    }

    //날짜를 얻어 봅시다
    private fun getDatDayFromDatabase(year :String){
        dbHelper= TaskDatabaseHelper(applicationContext,"wearTask.db",null,1);
        database=dbHelper.readableDatabase

        searchTaskOfRepeatYearInDB()
        searchTaskOfRepeatMonthInDB()
        searchTaskOfRepeatWeekInDB()
        searchTaskOfRepeatNoInDB(year)
    }

    private fun searchTaskOfRepeatYearInDB(){
        dotDaySetY.clear()
        try {
            var c2: Cursor = database.rawQuery("SELECT month,day FROM myTaskTbl WHERE repeatY == 1", null);
            while (c2.moveToNext()) {
                dotDaySetY.add("${c2.getInt(0)}.${c2.getInt(1)}")
            }
        }catch (e : SQLiteException){
            //TBL 이 없는거면 읽어올 데이터도 없다는 것이니 그냥 패쓰해도 문제 없을듯
        }
    }

    //월 마다 반복 TASK 찾기
    fun searchTaskOfRepeatMonthInDB(){
        dotDaySetM.clear()
        try {
            val c2: Cursor = database.rawQuery("SELECT day FROM myTaskTbl WHERE repeatM == 1", null);
            while (c2.moveToNext()) {
                dotDaySetM.add("${c2.getInt(0)}")
            }
        }catch (e : SQLiteException){
            //TBL 이 없는거면 읽어올 데이터도 없다는 것이니 그냥 패쓰해도 문제 없을듯
        }
    }

    private var weekRepeat = HashSet<String>()
    //주마다 반복
    private fun searchTaskOfRepeatWeekInDB(){
        dotDaySetW.clear()
        weekRepeat.clear()
        try {
            val c2: Cursor =
                database.rawQuery("SELECT week FROM myTaskTbl WHERE repeatW == 1", null);
            while (c2.moveToNext()) {
                dotDaySetW.add(c2.getString(0))
            }
            for (str in dotDaySetW){
                val weekStrs=str.split("&")
                checkWeekRepeat(weekStrs)
            }

        }catch (e : SQLiteException){
            //TBL 이 없는거면 읽어올 데이터도 없다는 것이니 그냥 패쓰해도 문제 없을듯
        }
    }

    fun checkWeekRepeat(weekStr : List<String>){
        for ( a in 0..weekStr.size){
            when(a){
                0-> if (weekStr[a].equals("1")) weekRepeat.add("7") //SUN
                1-> if (weekStr[a].equals("1")) weekRepeat.add("1") //MON
                2-> if (weekStr[a].equals("1")) weekRepeat.add("2") //TUE
                3-> if (weekStr[a].equals("1")) weekRepeat.add("3") //WEN
                4-> if (weekStr[a].equals("1")) weekRepeat.add("4") //THU
                5-> if (weekStr[a].equals("1")) weekRepeat.add("5") //FRI
                6-> if (weekStr[a].equals("1")) weekRepeat.add("6") //SAT
            }
        }
    }

    //반복 안하는 Task 찾기
    private fun searchTaskOfRepeatNoInDB(currentYear :String){
        dotDaySetN.clear()
        try {
            val c2: Cursor =
                database.rawQuery("SELECT year,month,day,time,text,notice FROM myTaskTbl WHERE repeatN == 1 AND year == ${currentYear}", null);
            while (c2.moveToNext()) {
                val str1 =if (c2.getInt(1).toString().length==1)"0${c2.getInt(1)}" else c2.getInt(1).toString()
                val str2 =if (c2.getInt(2).toString().length==1)"0${c2.getInt(2)}" else c2.getInt(2).toString()
                dotDaySetN.add("${str1}.${str2}")
                Log.d("도원","cursor : ${str1}.${str2}");
            }
        }catch (e : SQLiteException){
            //TBL 이 없는거면 읽어올 데이터도 없다는 것이니 그냥 패쓰해도 문제 없을듯
        }
    }


    fun getMonthList(dateTime: DateTime): List<String> {
        val list = mutableListOf<String>()

        val date = dateTime.withDayOfMonth(1)
        val prev = getPrevOffSet(date)

        val startValue = date.minusDays(prev)

        val totalDay = DateTimeConstants.DAYS_PER_WEEK * 6

        for (i in 0 until totalDay) {
            list.add(DateTime(startValue.plusDays(i)).toString("MM.dd.E"))
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