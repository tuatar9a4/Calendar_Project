package com.devd.calenderbydw.utils

import androidx.annotation.IntDef
import androidx.annotation.NonNull
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener

class SnapPagerScrollListener(
    private val snapHelper: PagerSnapHelper,
    private val type:Int,
    private val notifyOnInit:Boolean,
    private val listener:OnChangeListener,
    private var snapPosition:Int = RecyclerView.NO_POSITION,
) : OnScrollListener() {

    companion object{
        const val ON_SCROLL =0
        const  val ON_SETTLED = 1
    }

    @IntDef(ON_SCROLL,ON_SETTLED)
    annotation class Type

    interface OnChangeListener {
        fun onSnapped(position :Int,isRightScroll:Boolean)
    }

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        if((type == ON_SCROLL) || !hasItemPosition()){
            notifyListenerIfNeeded(getSnapPosition(recyclerView))
        }
    }

    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        super.onScrollStateChanged(recyclerView, newState)
        if (type == ON_SETTLED && newState == RecyclerView.SCROLL_STATE_IDLE) {
            notifyListenerIfNeeded(getSnapPosition(recyclerView))
        }
    }

    private fun getSnapPosition(recyclerView : RecyclerView) :Int {
        val layoutManager = recyclerView.layoutManager ?: return RecyclerView.NO_POSITION
        val snapView = snapHelper.findSnapView(layoutManager) ?: return RecyclerView.NO_POSITION;
        return layoutManager.getPosition(snapView)
    }

    private fun notifyListenerIfNeeded(newSnapPosition :Int) {
        if (snapPosition != newSnapPosition) {
            if (notifyOnInit && !hasItemPosition()) {
                listener.onSnapped(newSnapPosition,snapPosition<newSnapPosition)
            } else if (hasItemPosition()) {
                listener.onSnapped(newSnapPosition,snapPosition<newSnapPosition)
            }

            snapPosition = newSnapPosition
        }
    }

    private fun hasItemPosition() :Boolean {
        return snapPosition != RecyclerView.NO_POSITION
    }
}