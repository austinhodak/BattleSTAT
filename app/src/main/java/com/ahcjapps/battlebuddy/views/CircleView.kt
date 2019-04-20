package com.ahcjapps.battlebuddy.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.ahcjapps.battlebuddy.R

class CircleView constructor(context: Context, attr: AttributeSet? = null) : View(context, attr) {

    var paint: Paint = Paint()
    var color: Int? = null

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        paint.color = color ?: resources.getColor(R.color.md_white_1000)
        canvas?.drawCircle(height / 2f, width / 2f, width.toFloat(), paint)
    }

    fun setColor (color: Int) {
        this.color = color
        invalidate()
    }
}