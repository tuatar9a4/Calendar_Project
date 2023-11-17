package com.devd.calenderbydw.data.local.dialog

import androidx.annotation.GravityInt

data class BottomSheetItem (
    val type :Int=-1,
    val text :String?=null,
    var isCheck :Boolean=false,
    @GravityInt val gravity :Int?=null,
)