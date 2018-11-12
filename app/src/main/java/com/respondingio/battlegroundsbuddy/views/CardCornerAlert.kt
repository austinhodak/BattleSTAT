package com.respondingio.battlegroundsbuddy.views

import android.content.Context
import android.content.res.ColorStateList
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import com.respondingio.battlegroundsbuddy.R
import kotlinx.android.synthetic.main.card_corner_alert.view.*


class CardCornerAlert constructor(context: Context, attr: AttributeSet? = null) : RelativeLayout(context, attr) {

    init {
        LayoutInflater.from(context).inflate(R.layout.card_corner_alert, this, true)
    }

    fun setIconDrawable(resId: Int) {
        alert_icon?.setImageResource(resId)
    }

    fun setAlertColor(colorID: Int) {
        alert_background?.backgroundTintList = ColorStateList.valueOf(resources.getColor(colorID))
    }

    fun hide(): CardCornerAlert {
        visibility = View.GONE
        return this
    }

    fun show(): CardCornerAlert {
        visibility = View.VISIBLE
        return this
    }

    fun setOutdated(): CardCornerAlert {
        setAlertColor(R.color.timelineOrange)
        setIconDrawable(R.drawable.ic_access_time_black_24dp)
        show()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            tooltipText = "Stats are Outdated"
        }
        return this
    }
}