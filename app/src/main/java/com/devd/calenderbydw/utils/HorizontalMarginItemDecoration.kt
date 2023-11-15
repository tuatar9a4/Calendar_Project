package com.devd.calenderbydw.utils

import android.content.Context
import android.graphics.Rect
import android.util.TypedValue
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class HorizontalMarginItemDecoration(firstEndGap: Float, sideGap: Float, context: Context) :
    RecyclerView.ItemDecoration() {

    private val firstLastGap :Int = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        firstEndGap,
        context.resources.displayMetrics
    ).toInt()
    private val sideGap : Int = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        sideGap,
        context.resources.displayMetrics
    ).toInt()

    override fun getItemOffsets(
        outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
    ) {

        val itempostion = parent.getChildAdapterPosition(view)
        if (itempostion == RecyclerView.NO_POSITION) return

        val leftOffset = if(isFirstItem(itempostion)) firstLastGap else sideGap
        val rightOffset = if(isLastItem(itempostion,parent)) firstLastGap else sideGap
        outRect.set(leftOffset, 0, rightOffset, 0)
    }

    private fun isFirstItem(index: Int) = index == 0
    private fun isLastItem(index: Int, recyclerView: RecyclerView) = index == recyclerView.adapter!!.itemCount - 1
}