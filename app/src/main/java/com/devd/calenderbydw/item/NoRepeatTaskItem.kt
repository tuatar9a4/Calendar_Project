package com.devd.calenderbydw.item

data class NoRepeatTaskItem(
    var year :Int,
    var month : Int,
    var day :Int,
    var time:Long,
    var text :String,
    var notice :Int,
)
