package com.dwstyle.calenderbydw.utils

import android.content.Context
import android.graphics.PointF
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import androidx.wear.widget.WearableLinearLayoutManager

class CenterRecyclerManager: WearableLinearLayoutManager{
    private var progressToCenter: Float = 0f
    private var viewHeight = 0
    constructor(context: Context,height : Int) : super(context) {
        viewHeight=height
    }


    override fun smoothScrollToPosition(
        recyclerView: RecyclerView?,
        state: RecyclerView.State?,
        position: Int
    ) {
        recyclerView?.let {
            val smoothScroller =CenterSmoothScroller(it.context,viewHeight)
            smoothScroller.targetPosition=position
            startSmoothScroll(smoothScroller)
        }
    }

    private class CenterSmoothScroller(context: Context,viewHeight:Int) : LinearSmoothScroller(context) {
        override fun calculateDtToFit(
            viewStart: Int,
            viewEnd: Int,
            boxStart: Int,
            boxEnd: Int,
            snapPreference: Int
        ): Int {

            return  (boxStart + (boxEnd - boxStart) / 2) - (viewStart+10+ (viewEnd - viewStart) / 2);
        }

        override fun computeScrollVectorForPosition(targetPosition: Int): PointF? {
            return null
        }
    }

}