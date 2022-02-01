package com.dwstyle.calenderbydw

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.wear.tiles.*
import androidx.wear.tiles.DimensionBuilders.*
import androidx.wear.tiles.LayoutElementBuilders.VERTICAL_ALIGN_CENTER
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
    private val holidaySet =HashSet<String>()
    private var weekInt =0
    lateinit var sharedPreferences:SharedPreferences
    private final val settingMillis : String ="SETTINGMILLS"
    private var clickType= "none"
    override fun onTileRequest(requestParams: RequestBuilders.TileRequest): ListenableFuture<TileBuilders.Tile> {

        sharedPreferences =applicationContext.getSharedPreferences("sharedData", Context.MODE_PRIVATE)
        if (sharedPreferences.getLong(settingMillis,0)==0L){
            sharedPreferences.edit().putLong(settingMillis,DateTime().withDayOfMonth(1).withTimeAtStartOfDay().millis).apply()
        }
        // setFreshnessIntervalMillis : 타일의 새로 고침 간격 (밀리초) / 날짜의 변화가 있어야해서 1시간에 한번 씩 새로고침함
        // setTimeline : 화면을 구성을 set 함 보통 LayoutElementBuilders.LayoutElement 를 반환하는 method를 만든 후 거기에서 타일을 구성함
        Log.d("도원","requestPareams : "+requestParams.state?.lastClickableId)
        if (requestParams.state?.lastClickableId!=null){
            if (requestParams.state!!.lastClickableId.equals(applicationContext.getString(R.string.widget_minus))){
                clickType=applicationContext.getString(R.string.widget_minus);
            }else if (requestParams.state!!.lastClickableId.equals(applicationContext.getString(R.string.widget_plus))){
                clickType=applicationContext.getString(R.string.widget_plus)
            }else if (requestParams.state!!.lastClickableId.equals(applicationContext.getString(R.string.widget_today))){
                clickType=applicationContext.getString(R.string.widget_today)
            }else if( requestParams.state!!.lastClickableId==applicationContext.getString(R.string.widget_go_to_main)){
                val intent =Intent(applicationContext,MainActivity::class.java)
                intent.putExtra("widgetMonth","fromWidget")
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                clickType=applicationContext.getString(R.string.widget_none)
            }else{
                clickType=applicationContext.getString(R.string.widget_none)
            }
        }
        Log.d("도원","ㅇㅇㅇ");
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
    //업데이트 함수로 호출이 안되고 타일을 없애고 새로 만드니까 호출이 됨
    override fun onResourcesRequest(requestParams: RequestBuilders.ResourcesRequest): ListenableFuture<ResourceBuilders.Resources> {
        Log.d("도원","onResourcesRequest")

        return Futures.immediateFuture(ResourceBuilders.Resources.Builder().setVersion(
            RESOURCES_VERSION)
            .addIdToImageMapping(getString(R.string.dot_year),ResourceBuilders.ImageResource.Builder()
                .setAndroidResourceByResId(ResourceBuilders.AndroidImageResourceByResId.Builder()
                    .setResourceId(R.drawable.dot_year)
                    .build()).build())
            .addIdToImageMapping(getString(R.string.dot_month),ResourceBuilders.ImageResource.Builder()
                .setAndroidResourceByResId(ResourceBuilders.AndroidImageResourceByResId.Builder()
                    .setResourceId(R.drawable.dot_month)
                    .build()).build())
            .addIdToImageMapping(getString(R.string.dot_day),ResourceBuilders.ImageResource.Builder()
                .setAndroidResourceByResId(ResourceBuilders.AndroidImageResourceByResId.Builder()
                    .setResourceId(R.drawable.dot_day)
                    .build()).build())
            .addIdToImageMapping(getString(R.string.dot_week),ResourceBuilders.ImageResource.Builder()
                .setAndroidResourceByResId(ResourceBuilders.AndroidImageResourceByResId.Builder()
                    .setResourceId(R.drawable.dot_week)
                    .build()).build())
            .addIdToImageMapping(getString(R.string.next_icon),ResourceBuilders.ImageResource.Builder()
                .setAndroidResourceByResId(ResourceBuilders.AndroidImageResourceByResId.Builder()
                    .setResourceId(R.drawable.next_month_icon)
                    .build()).build())
            .addIdToImageMapping(getString(R.string.pre_icon),ResourceBuilders.ImageResource.Builder()
                .setAndroidResourceByResId(ResourceBuilders.AndroidImageResourceByResId.Builder()
                    .setResourceId(R.drawable.pre_month_icon)
                    .build()).build())
            .build())
    }


    // 타일의 구성을 위한 method
    fun myLayout(deviceParameters: DeviceParametersBuilders.DeviceParameters) : LayoutElementBuilders.LayoutElement{
        //달력 구성을 위한 정보를 미리 가져옴
        getCalendar(clickType)
        // Column : 타일의 구성 요소를 열(세로) 로 정렬
        // 첫 addContent : (Spacer) 상단에서 30f 만큼의 공간을 만든다 / 워치가 원형이다 보니 중앙으로 구성을 맞추기 위함
        // 둘 addContent : (Text) 글자를 쓰는 Builder / TextAlignmentProp 를 사용하여 text 를 중앙 정렬한다.
        // 셋 addContent : (Spacer) 공간 만드는 Builder
        val calendarLayout =LayoutElementBuilders.Column.Builder().setWidth(wrap())
        calendarLayout.setHeight(expand())
            .addContent(LayoutElementBuilders.Spacer.Builder().setHeight(dp(20f)).build())
            .addContent(
                LayoutElementBuilders.Row.Builder().setWidth(wrap()).setVerticalAlignment(LayoutElementBuilders.VerticalAlignmentProp.Builder()
                    .setValue(VERTICAL_ALIGN_CENTER).build())
                    .addContent(
                        LayoutElementBuilders.Spacer.Builder().setHeight(dp(7f)).setWidth(dp(20f))
                            .setModifiers(ModifiersBuilders.Modifiers.Builder()
                            .setClickable(
                                ModifiersBuilders.Clickable.Builder()
                                    .setId("Minus")
                                    .setOnClick(ActionBuilders.LoadAction.Builder().build())
                                    .build()).build())
                            .build()
                    )
                    .addContent(
                        LayoutElementBuilders.Text.Builder().setMultilineAlignment(LayoutElementBuilders.TextAlignmentProp.Builder().setValue(
                        LayoutElementBuilders.TEXT_ALIGN_CENTER).build())
                        .setText("<")
                        .setModifiers(ModifiersBuilders.Modifiers.Builder()
                            .setClickable(
                                ModifiersBuilders.Clickable.Builder()
                                .setId("Minus")
                                    .setOnClick(ActionBuilders.LoadAction.Builder().build())
                                    .build()).build())
                        .setFontStyle(LayoutElementBuilders.FontStyles.caption1(deviceParameters)
                            .setSize(sp(13f))
                            .build()

                        ).build()
                    )
                    .addContent(LayoutElementBuilders.Spacer.Builder().setWidth(dp(13f))
                        .setModifiers(ModifiersBuilders.Modifiers.Builder()
                            .setClickable(
                                ModifiersBuilders.Clickable.Builder()
                                    .setId("Minus")
                                    .setOnClick(ActionBuilders.LoadAction.Builder().build())
                                    .build()).build()).build()
                    )
                    .addContent(
                        //년.월 yyyy.MM
                        LayoutElementBuilders.Text.Builder().setMultilineAlignment(LayoutElementBuilders.TextAlignmentProp.Builder().setValue(
                            LayoutElementBuilders.TEXT_ALIGN_CENTER).build())
                            .setText(calendarDate)
                            .setModifiers(ModifiersBuilders.Modifiers.Builder()
                                .setClickable(
                                    ModifiersBuilders.Clickable.Builder()
                                        .setId("Today")
                                        .setOnClick(ActionBuilders.LoadAction.Builder().build())
                                        .build()).build())
                            .setFontStyle(LayoutElementBuilders.FontStyles.caption1(deviceParameters)
                                .setSize(sp(calendarTopTextSize))
                                .build()).build()
                    )
                    .addContent(LayoutElementBuilders.Spacer.Builder().setWidth(dp(13f))
                        .setModifiers(ModifiersBuilders.Modifiers.Builder()
                        .setClickable(
                            ModifiersBuilders.Clickable.Builder()
                                .setId("Plus")
                                .setOnClick(ActionBuilders.LoadAction.Builder().build())
                                .build()).build()).build()
                    )
                    .addContent(
                        //년.월 yyyy.MM
                        LayoutElementBuilders.Text.Builder().setMultilineAlignment(LayoutElementBuilders.TextAlignmentProp.Builder().setValue(
                            LayoutElementBuilders.TEXT_ALIGN_CENTER).build())
                            .setText(">")
                            .setModifiers(ModifiersBuilders.Modifiers.Builder()
                                .setClickable(
                                    ModifiersBuilders.Clickable.Builder()
                                        .setId("Plus")
                                        .setOnClick(ActionBuilders.LoadAction.Builder().build())
                                        .build()).build())
                            .setFontStyle(LayoutElementBuilders.FontStyles.caption1(deviceParameters)
                                .setSize(sp(13f))
                                .build()).build()
                    )
                    .addContent(
                        LayoutElementBuilders.Spacer.Builder().setHeight(dp(7f)).setWidth(dp(20f))
                            .setModifiers(ModifiersBuilders.Modifiers.Builder()
                                .setClickable(
                                    ModifiersBuilders.Clickable.Builder()
                                        .setId("Plus")
                                        .setOnClick(ActionBuilders.LoadAction.Builder().build())
                                        .build()).build())
                            .build()
                    )
                    .build())
                    .addContent(
                        LayoutElementBuilders.Spacer.Builder().setHeight(dp(3f)).build()
                    )
//<a href="https://www.streamlinehq.com">Free Navigation Right 3 PNG icon by Streamline</a> next icon
//        <a href="https://www.streamlinehq.com">Free Navigation Left 3 PNG icon by Streamline</a> pre icon
//        <a href="https://www.streamlinehq.com">Free Move Left 3 PNG icon by Streamline</a> move_next
//        <a href="https://www.streamlinehq.com">Free Move Right 2 PNG icon by Streamline</a> move_preview
//        <a href="https://www.streamlinehq.com">Free Calendar Refresh PNG icon by Streamline</a> recycler
//        <a href="https://www.streamlinehq.com">Free Calendar Date PNG icon by Streamline</a> todayCllaendr
        //상단 요일 만드는 곳
        val weekText = arrayOf("Sun","Mon","The","Wen","Thu","Fri","Sat");
        val rowBuilderBox =  LayoutElementBuilders.Row.Builder().setWidth(wrap());
        val weekFontStyle =LayoutElementBuilders.FontStyles.caption1(deviceParameters)
            .setSize(sp(calendarWeekTextSize))

        for (a in weekText){
            if (a.equals("Sun")) weekFontStyle.setColor(ColorBuilders.argb(ContextCompat.getColor(baseContext,R.color.sunColor)))
            else if (a.equals("Sat"))weekFontStyle.setColor(ColorBuilders.argb(ContextCompat.getColor(baseContext,R.color.satColor)))
            else weekFontStyle.setColor(ColorBuilders.argb(ContextCompat.getColor(baseContext,R.color.currnetMonthDayColor)))
            // 가로 행 으로 요일을 하나씩 추가한다.
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
            // 토요일이 아닌경우 (마지막이 아닌경우) 옆에 공간을 띄운다.
            if (!a.equals("Sat"))rowBuilderBox.addContent(LayoutElementBuilders.Spacer.Builder().setWidth(dp(calendarSpace)).build())
        }
        //열에 아까 만든 요일 행 추가
        calendarLayout.addContent(rowBuilderBox.build())
        //다음열에 spacer로 공간을 띄운다.
        calendarLayout.addContent(LayoutElementBuilders.Spacer.Builder().setHeight(dp(5f)).build())

        //날짜를 만드는곳
        var count =0;
        var isItem =false;
        //가로 (row) 행 만들기
        lateinit var calendarDayRow : LayoutElementBuilders.Row.Builder
        //점을 담을 Row Builder
        lateinit var dotContainer :LayoutElementBuilders.Row.Builder
        //점을 만들기위한 Image Builder
        lateinit var scheduleItem : LayoutElementBuilders.Image.Builder
        lateinit var modifi :ModifiersBuilders

        for (a in allDayOfMonth){
            val strs :List<String> =a.split(".")
            val strMonthDay ="${strs[0]}.${strs[1]}"
            if (count==0){
                //한줄을 다 채우면 가로(Row) 행 Builder를 새로 만든다.
                calendarDayRow=LayoutElementBuilders.Row.Builder().setWidth(wrap());
            }
            dotContainer=LayoutElementBuilders.Row.Builder().setWidth(wrap())
//            isItem = count==3
            //날짜의 색상  먼저 전달인지 확인하고 그 후 오늘인지 다음으로 일요일 토요일 평일 순으로 확인
//            Log.d("도원","holidaySet  : ${holidaySet}")
//            Log.d("도원","dotDaySetY  : ${dotDaySetY}")
//            Log.d("도원","dotDaySetM  : ${dotDaySetM}")
//            Log.d("도원","dotDaySetN  : ${dotDaySetN}")
//            Log.d("도원","weekRepeat  : ${weekRepeat}")
//            Log.d("도원","strMonthDay  : ${strMonthDay}")
//            Log.d("도원","strs[2]  : ${strs[2]}")
//
            val dayTextColor =if(!strMonthDay.split(".")[0].equals(firstDayOfMonth.split(".")[0]))R.color.preMonthDayColor
                                else if (strMonthDay.equals(currentDate)) R.color.toDayColor
                                else if (holidaySet.contains(strMonthDay)) R.color.sunColor
                                else if (strs[2] == getString(R.string.weeksunEn)|| strs[2]==getString(R.string.weeksunKr) )R.color.sunColor
                                else if (strs[2].equals("Sat") || strs[2].equals("토"))R.color.satColor
                                else R.color.currnetMonthDayColor
            isDot=false
            weekInt=0
            //점찍는곳
            if (dotDaySetY.contains(strMonthDay)){
//            if (true){
                isDot=true
                scheduleItem=LayoutElementBuilders.Image.Builder().setHeight(dp(2f)).setWidth(dp(2f)).setResourceId("dot_year")
                dotContainer.addContent(scheduleItem.build())
                    .addContent(LayoutElementBuilders.Spacer.Builder().setWidth(dp(1f)).build())
            }
//            for (str in dotDaySetY){
//                if (str.equals(strMonthDay)) {
//                    isDot=true
//                    scheduleItem=LayoutElementBuilders.Image.Builder().setHeight(dp(2f)).setWidth(dp(2f)).setResourceId("dot_image")
//                }
//            }
            if (dotDaySetM.contains(strMonthDay.split(".")[1])){
                isDot=true
                scheduleItem=LayoutElementBuilders.Image.Builder().setHeight(dp(2f)).setWidth(dp(2f)).setResourceId("dot_month")
                dotContainer.addContent(scheduleItem.build())
                    .addContent(LayoutElementBuilders.Spacer.Builder().setWidth(dp(1f)).build())
            }
//            for (str in dotDaySetM){
//                if (str.equals(strMonthDay.split(".")[1])) {
//                    isDot=true
//                    scheduleItem=LayoutElementBuilders.Image.Builder().setHeight(dp(2f)).setWidth(dp(2f)).setResourceId("dot_image")
//                }
//            }
            if (dotDaySetN.contains(strMonthDay)){
                isDot=true
                scheduleItem=LayoutElementBuilders.Image.Builder().setHeight(dp(2f)).setWidth(dp(2f)).setResourceId("dot_day")
                dotContainer.addContent(scheduleItem.build())
                    .addContent(LayoutElementBuilders.Spacer.Builder().setWidth(dp(1f)).build())
            }
//            for (str in dotDaySetN){
//                Log.d("도원","${str}  || ${strMonthDay}")
//                if (str.equals(strMonthDay)) {
//                    isDot=true
//                    scheduleItem=LayoutElementBuilders.Image.Builder().setHeight(dp(2f)).setWidth(dp(2f)).setResourceId("dot_image")
//                }
//            }
            if (strs[2]=="Sun"  || strs[2]=="일")weekInt=7
            if (strs[2]=="Mon"  || strs[2]=="월")weekInt=1
            if (strs[2]=="Tue"  || strs[2]=="화")weekInt=2
            if (strs[2]=="Wed"  || strs[2]=="수")weekInt=3
            if (strs[2]=="Thu"  || strs[2]=="목")weekInt=4
            if (strs[2]=="Fri"  || strs[2]=="금")weekInt=5
            if (strs[2]=="Sat"  || strs[2]=="토")weekInt=6
            if (weekRepeat.contains(weekInt.toString())){
                isDot=true
                Log.d("도원","weekRepeat Dot : ${weekInt.toString()}")
                scheduleItem=LayoutElementBuilders.Image.Builder().setHeight(dp(2f)).setWidth(dp(2f)).setResourceId("dot_week")
                dotContainer.addContent(scheduleItem.build())
                    .addContent(LayoutElementBuilders.Spacer.Builder().setWidth(dp(1f)).build())
            }

//            if (!isDot){
//                scheduleItem=LayoutElementBuilders.Image.Builder().setHeight(dp(0f)).setWidth(dp(0f)).setResourceId("dot_image")
//            }

            //날짜 하나씩 담는다
            //오늘날짜
            if (strMonthDay.equals(currentDate)){
                calendarDayRow.addContent(
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
                                    dotContainer.build()
//                                    scheduleItem.build()
                                ).build()
                        ).build())
            }else{
//                오늘을 제외한 날짜
                calendarDayRow.addContent(
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
                                    dotContainer.build()
//                                    scheduleItem.build()
                                ).build()
                        ).build())
            }
            //한칸을 다 채우면 Clomu(열) 세로 에 집어 넣는다.
            count++;
            if (count==7){
                calendarLayout.addContent(calendarDayRow.setModifiers(ModifiersBuilders.Modifiers.Builder()
                    .setClickable(
                        ModifiersBuilders.Clickable.Builder()
                            .setId("goToMain")
                            .setOnClick(ActionBuilders.LoadAction.Builder().build())
                            .build()).build()).build())
                count=0
            }
        }
        return calendarLayout.build()
    }

    private var calendarDate : String =""
    private var firstDayOfMonth: String =""
    private var allDayOfMonth : List<String> =ArrayList<String>()
    private var currentDate :String =""
    private var currentDate1 :String =""

    private fun getCalendar(type :String) {

        var currentDateTime = sharedPreferences.getLong(settingMillis, 0)

        if (currentDateTime==0L){
            currentDateTime= DateTime().withDayOfMonth(1).withTimeAtStartOfDay().millis
            sharedPreferences.edit().putLong(settingMillis,currentDateTime).apply()
        }else if (type.equals("Plus")){
            currentDateTime=DateTime(currentDateTime).plusMonths(1).millis
            sharedPreferences.edit().putLong(settingMillis,currentDateTime).apply()
        }else if (type.equals("Minus")){
            currentDateTime=DateTime(currentDateTime).minusMonths(1).millis
            sharedPreferences.edit().putLong(settingMillis,currentDateTime).apply()
        }else if (type.equals("Today")){
            currentDateTime=DateTime().withDayOfMonth(1).withTimeAtStartOfDay().millis
            sharedPreferences.edit().putLong(settingMillis,currentDateTime).apply()
        }
        calendarDate=DateTime(currentDateTime).toString("yyyy.MM")
        firstDayOfMonth = DateTime(currentDateTime).toString("MM.dd")
        allDayOfMonth = getMonthList(DateTime(currentDateTime))
        currentDate = DateTime(System.currentTimeMillis()).toString("MM.dd")
        currentDate1 = System.currentTimeMillis().toString()

        getDatDayFromDatabase(calendarDate.split(".").get(0),calendarDate.split(".")[1])

//        Log.d("도원","이번달 calendarDate : $calendarDate // test : $firstDayOfMonth //test2 : $allDayOfMonth  // ")
//        Log.d("도원","이번달 currentDate : $currentDate ")
//        Log.d("도원","다음달 calendarDate : ${DateTime(plus).toString("yyyy.MM")} // test : ${DateTime(plus).toString("MM.dd")} //test2 : ${getMonthList(DateTime(plus))}  // ")
    }

    //날짜를 얻어 봅시다
    private fun getDatDayFromDatabase(year :String,month:String){
        dbHelper= TaskDatabaseHelper(applicationContext,"wearTask.db",null,3);
        database=dbHelper.readableDatabase

        searchTaskOfRepeatYearInDB()
        searchTaskOfRepeatMonthInDB()
        searchTaskOfRepeatWeekInDB()
        searchTaskOfRepeatNoInDB(year)
        searchHolidayInDB(year.toInt(),month.toInt())
    }

    private fun searchTaskOfRepeatYearInDB(){
        dotDaySetY.clear()
        try {
            var c2: Cursor = database.rawQuery("SELECT month,day FROM myTaskTbl WHERE repeatY == 1", null);
            while (c2.moveToNext()) {
                val str1 =if (c2.getInt(0).toString().length==1)"0${c2.getInt(0)}" else c2.getInt(0).toString()
                val str2 =if (c2.getInt(1).toString().length==1)"0${c2.getInt(1)}" else c2.getInt(1).toString()
                dotDaySetY.add("${str1}.${str2}")
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
                val str1 =if (c2.getInt(0).toString().length==1)"0${c2.getInt(0)}" else c2.getInt(0).toString()
                dotDaySetM.add("${str1}")
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
                database.rawQuery("SELECT year,month,day FROM myTaskTbl WHERE repeatN == 1 AND year == ${currentYear}", null);
            while (c2.moveToNext()) {
                val str1 =if (c2.getInt(1).toString().length==1)"0${c2.getInt(1)}" else c2.getInt(1).toString()
                val str2 =if (c2.getInt(2).toString().length==1)"0${c2.getInt(2)}" else c2.getInt(2).toString()
                dotDaySetN.add("${str1}.${str2}")
            }
        }catch (e : SQLiteException){
            //TBL 이 없는거면 읽어올 데이터도 없다는 것이니 그냥 패쓰해도 문제 없을듯
        }
    }

    //공휴일 가져오기
    private fun searchHolidayInDB(year : Int,month: Int ){
        holidaySet.clear()
        try{
            if (TaskDatabaseHelper.isExistsTable(database,"holiday${year}Tbl")){
                val  c2 : Cursor? = TaskDatabaseHelper.searchHoliday(database,year,month,"holiday${year}Tbl")
                c2?.let {
                    while (it.moveToNext()){
                        val holidayMonth =if(it.getInt(1).toString().length==1)"0${it.getInt(1)}" else it.getInt(1)
                        val holidayDay =if(it.getInt(2).toString().length==1)"0${it.getInt(2)}" else it.getInt(2)
                        holidaySet.add("${holidayMonth}.${holidayDay}")
                    }
                }
            }
        }catch (e : SQLiteException){
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