package com.netherpyro.speedometer

import android.content.Context
import android.util.TypedValue
import android.view.View
import android.view.ViewTreeObserver

/**
 * @author mmikhailov on 23.05.2020.
 */
fun Context.dpToPx(dp: Float) =
        if (dp > 0f) {
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)
        } else 0f

fun Context.spToPx(sp: Float) =
        if (sp > 0f) {
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, resources.displayMetrics)
        } else 0f

fun <T : View> T.alsoOnLaid(block: (T) -> Unit) {
    viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            viewTreeObserver.removeOnGlobalLayoutListener(this)
            block.invoke(this@alsoOnLaid)
        }
    })
}