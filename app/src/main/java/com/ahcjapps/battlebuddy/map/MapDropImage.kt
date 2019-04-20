package com.ahcjapps.battlebuddy.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import com.ahcjapps.battlebuddy.R
import androidx.appcompat.widget.AppCompatImageView


class MapDropImage : AppCompatImageView {
    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    private val paint = Paint()

    var vX = -1f
    var vY = -1f

    var bitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(resources, R.drawable.marker_yellow), 100, 100, false)

    fun updatePoint(x: Float, y: Float) {
        var xx = 663f
        var yy = 292f
        vX = x / (1000f / width) - 50
        vY = y / (1000f / height) - 100
        invalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        paint.reset()
        paint.isAntiAlias = true

        Log.d("IMAGE", "$height - $width -- $vX - $vY")

        if (vX != -1f && vY != -1f)
        canvas?.drawBitmap(bitmap, vX, vY, paint)
    }
}