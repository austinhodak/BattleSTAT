package com.austinh.battlebuddy.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.austinh.battlebuddy.R
import kotlinx.android.synthetic.main.view_stat_item.view.*

class StatItem constructor(
        context: Context,
        attr: AttributeSet? = null,
        statName: String? = "NULL",
        statValue: String? = "NULL"
) : LinearLayout(context, attr) {

    init {
        LayoutInflater.from(context).inflate(R.layout.view_stat_item, this, true)

        this.statText.text = statName ?: "NULL"
        this.statValue.text = statValue
    }

}