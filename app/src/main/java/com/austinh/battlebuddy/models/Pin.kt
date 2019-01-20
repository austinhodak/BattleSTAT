package com.austinh.battlebuddy.models

import android.graphics.Bitmap
import android.graphics.PointF
import java.io.Serializable

data class Pin (
        val points: PointF,
        val name: String,
        val bitmap: Bitmap
) : Serializable