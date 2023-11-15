package com.devd.calenderbydw.utils

import android.content.Context
import android.content.res.Resources
import android.util.DisplayMetrics
import android.util.Size
import android.util.TypedValue
import android.view.WindowManager
import androidx.recyclerview.widget.RecyclerView


fun Context.getDeviceSize() :Size {
    return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
        val metrics =
            this.getSystemService(WindowManager::class.java).currentWindowMetrics
        Size(metrics.bounds.width(), metrics.bounds.height())
    } else {
        val display = this.getSystemService(WindowManager::class.java).defaultDisplay
        val metrics = if (display != null) {
            DisplayMetrics().also { display.getRealMetrics(it) }
        } else {
            Resources.getSystem().displayMetrics
        }
        Size(metrics.widthPixels, metrics.heightPixels)
    }

}

//Dp 값 (value) 을 float로 변형
fun Context.getDpValue(value: Float) = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP,
    value,
    this.resources?.displayMetrics
)

//Sp 값 을 변경
fun Context.getSpValue(value: Float) = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_SP,
    value,
    this.resources.displayMetrics
)

fun RecyclerView.addSingleItemDecoRation(vararg itemDecorations : RecyclerView.ItemDecoration){
    val itemDecoCount = itemDecorationCount
    if(itemDecoCount>0){
        for (a in 0 until itemDecoCount){
            for(itemDeco in itemDecorations){
                if(getItemDecorationAt(a)==itemDeco){
                    removeItemDecorationAt(a)
                }
            }
        }
    }
    for(a in itemDecorations){
        addItemDecoration(a)
    }
}