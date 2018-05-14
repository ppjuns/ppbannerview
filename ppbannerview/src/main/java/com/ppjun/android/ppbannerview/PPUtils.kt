package com.ppjun.android.ppbannerview

import android.content.res.Resources
import android.util.TypedValue

class PPUtils {

    companion object {

        fun dp2px(dp: Int) = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(),
                Resources.getSystem().displayMetrics).toInt()

    }
}