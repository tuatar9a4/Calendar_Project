package com.dwstyle.calenderbydw.item

data class TaskItem (
    var year :Int,
    var month : Int,
    var day :Int,
    var week :String,
    var time:Long,
    var text :String,
    var notice :Int,
    var repeatY :Int,
    var repeatM :Int,
    var repeatW :Int,
    var repeatN :Int,
        )