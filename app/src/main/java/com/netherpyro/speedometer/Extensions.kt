package com.netherpyro.speedometer

import android.content.Context
import android.util.TypedValue

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