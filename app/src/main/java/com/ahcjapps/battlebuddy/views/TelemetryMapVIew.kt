package com.ahcjapps.battlebuddy.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import com.ahcjapps.battlebuddy.R
import com.ahcjapps.battlebuddy.models.Pin
import com.ahcjapps.battlebuddy.models.SafeZoneCircle
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import kotlin.math.roundToInt

class TelemetryMapVIew constructor(context: Context, attr: AttributeSet? = null) : SubsamplingScaleImageView(context, attr) {

    init {
        this.setPanLimit(SubsamplingScaleImageView.PAN_LIMIT_INSIDE)
        this.setMinimumScaleType(SCALE_TYPE_CENTER_CROP)
    }

    val pinListM = ArrayList<PinSet>()
    var safeZoneCircleList = ArrayList<SafeZoneCircle>()
    private val paint = Paint()

    fun addKill(killer: Pin? = null, victim: Pin? = null, removeOtherKills: Boolean = false, drawLine: Boolean) {
        if (removeOtherKills) pinListM.clear()
        pinListM.add(PinSet(killer, victim, drawLine))
        invalidate()
    }

    fun addKill(pinSet: PinSet, removeOtherKills: Boolean = false) {
        if (removeOtherKills) pinListM.clear()
        pinListM.add(pinSet)
        invalidate()
    }

    fun placeSafeZones(list: ArrayList<SafeZoneCircle>) {
        safeZoneCircleList = list
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if (!isReady) {
            return
        }
        paint.reset()
        paint.isAntiAlias = true

        val scaleX: Float = if (this.scale > 3.40) {
            3.4F
        } else {
            this.scale
        }

        val height = (30 * scaleX).roundToInt()
        val width = (36 * scaleX).roundToInt()

        for (pinSet in pinListM) {

            //Line on bottom
            if (pinSet.drawLine) {
                pinSet.drawTheLine(canvas, this, paint)
            }

            Log.d("ZOOM", "${this.scale} - ${this.scaleX} - ${this.scaleY} - ${this.cameraDistance}")

            if (pinSet.pin2 != null) {
                val vPin = sourceToViewCoord(pinSet.pin2!!.points)
                val vX = vPin!!.x - height / 2
                val vY = vPin.y - width

                canvas?.drawBitmap(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(resources, R.drawable.point_skull), height, width, false), vX, vY, paint)
            }

            //Killer on top
            if (pinSet.pin1 != null) {
                val vPin = sourceToViewCoord(pinSet.pin1!!.points)
                val vX = vPin!!.x - height / 2
                val vY = vPin.y - width

                canvas?.drawBitmap(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(resources, R.drawable.pin_gun), height, width, false), vX, vY, paint)
            }
        }

        val circlePaint = Paint()
        circlePaint.style = Paint.Style.STROKE
        circlePaint.color = Color.WHITE
        circlePaint.strokeWidth = 4f

        for (circle in safeZoneCircleList) {
            val cirlcePin = sourceToViewCoord(circle.position.x.toFloat(), circle.position.y.toFloat())!!

            canvas?.drawCircle(cirlcePin.x, cirlcePin.y,circle.radius.toFloat() * this.scale, circlePaint)

            Log.d("CIRCLE", circle.position.toString())
        }
    }
}

data class PinSet (
        var pin1: Pin? = null,
        var pin2: Pin? = null,
        var drawLine: Boolean = false
) {
    fun canDrawLine() : Boolean {
        if (pin1 != null && pin2 != null) return true
        return false
    }

    fun drawTheLine(canvas: Canvas?, context: SubsamplingScaleImageView, paint: Paint) {
        if (!canDrawLine() && canvas != null) return

        paint.color = Color.parseColor("#D50000")
        paint.style = Paint.Style.STROKE
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeCap = Paint.Cap.ROUND

        val fl: Float = if (context.scale > 3.40) {
            10f
        } else {
            //context.scale * 2.94117647f
            10f
        }

        paint.strokeWidth = fl

        val vPin1 = context.sourceToViewCoord(pin1?.points)!!
        val vPin2 = context.sourceToViewCoord(pin2?.points)!!

        canvas!!.drawLine(vPin1.x, vPin1.y - 10, vPin2.x, vPin2.y - 10, paint)
    }
}