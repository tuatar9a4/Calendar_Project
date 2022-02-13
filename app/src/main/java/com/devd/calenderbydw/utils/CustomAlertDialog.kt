package com.devd.calenderbydw.utils

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface

class CustomAlertDialog(private val context: Context) {

    fun taskDeleteDialog(listener :DialogInterface.OnClickListener){
        val builder  = AlertDialog.Builder(context)

        builder.setMessage("일정을 삭제 하시겠습니까?")
        builder.setPositiveButton("확인", listener)
        builder.setNegativeButton("취소") { dialog, wihch ->
            dialog.dismiss()
        }
        val alertDialog =builder.create()
        alertDialog.show()
    }
}