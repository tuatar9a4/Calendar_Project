package com.dwstyle.calenderbydw.utils

import android.content.Context
import android.graphics.PointF
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView

class CenterRecyclerManager : LinearLayoutManager {

    constructor(context: Context) : super(context) {

    }

    constructor(context: Context,orientation :Int,reverseLayout :Boolean) : super(context,orientation,reverseLayout) {

    }

    override fun smoothScrollToPosition(
        recyclerView: RecyclerView?,
        state: RecyclerView.State?,
        position: Int
    ) {
        recyclerView?.let {
            val smoothScroller =CenterSmoothScroller(it.context)
            startSmoothScroll(smoothScroller)
        }
    }

    private class CenterSmoothScroller(context: Context) : LinearSmoothScroller(context) {

        override fun calculateDtToFit(
            viewStart: Int,
            viewEnd: Int,
            boxStart: Int,
            boxEnd: Int,
            snapPreference: Int
        ): Int {
            return  (boxStart + (boxEnd - boxStart) / 2) - (viewStart + (viewEnd - viewStart) / 2);
        }

        override fun computeScrollVectorForPosition(targetPosition: Int): PointF? {
            return null
        }
    }
}