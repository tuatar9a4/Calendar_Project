package com.dwstyle.calenderbydw.utils

import android.app.Activity
import android.graphics.Rect
import android.util.Log
import android.view.View
import androidx.core.view.size
import androidx.recyclerview.widget.RecyclerView

class DateTimePickerDecoration(private val activity: Activity,private val height : Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        if (parent.getChildAdapterPosition(view)==0){
            outRect.top=LengthUtils.getDeviceHeight(activity)/2-height
        }else if(parent.getChildAdapterPosition(view)==parent.adapter!!.itemCount-1){
            outRect.top=20
            outRect.bottom=LengthUtils.getDeviceHeight(activity)/2-height
        }else {
            outRect.top=20
        }
    }
}