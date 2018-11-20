package com.respondingio.battlegroundsbuddy.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.widget.ImageView
import com.respondingio.battlegroundsbuddy.R
import androidx.appcompat.widget.AppCompatImageView
import kotlin.math.roundToInt


class MapDropImage : AppCompatImageView {
    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    private val paint = Paint()

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        paint.reset()
        paint.isAntiAlias = true

        val vX = height / 2f
        val vY = width / 2f

        Log.d("IMAGE", "$height - $width")

        canvas?.drawBitmap(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(resources, R.drawable.marker_yellow), 100, 100, false), x / 2, y / 2, paint)
    }
}