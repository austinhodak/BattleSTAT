package com.respondingio.battlegroundsbuddy.stats

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.PointF
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.davemorrissey.labs.subscaleview.ImageSource
import com.respondingio.battlegroundsbuddy.R
import com.respondingio.battlegroundsbuddy.models.LogPlayerKill
import com.respondingio.battlegroundsbuddy.models.Pin
import com.respondingio.battlegroundsbuddy.views.PinView
import kotlinx.android.synthetic.main.maptile_activity.match_detail_toolbar


class MapTileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.maptile_activity)
        setSupportActionBar(match_detail_toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        if (intent == null) {
            finish()
        }

        val mapName = intent.getStringExtra("mapName")
        val imageView = findViewById<PinView>(R.id.pinview)
        imageView.setImage(ImageSource.asset(mapName))
        imageView.maxScale = 320f

        val killFeedLogPlayerKill = intent.getSerializableExtra("killLog") as LogPlayerKill

        var offest = 1.0
        if (mapName == "sanhok/Savage_Main_Low_Res.jpg") {
            offest = 3.41333333
        }

        val victimPoint = PointF((killFeedLogPlayerKill.victim.location.x / 100 / offest).toFloat(), (killFeedLogPlayerKill.victim.location.y / 100 / offest).toFloat())
        if (killFeedLogPlayerKill.killer.location.x > 0.0 && killFeedLogPlayerKill.killer.location.y > 0.0) {
            val killerPoint = PointF((killFeedLogPlayerKill.killer.location.x / 100 / offest).toFloat(), (killFeedLogPlayerKill.killer.location.y / 100 / offest).toFloat())
            imageView.setPin(Pin(killerPoint, "KILLER", getBitmapDrawable(R.drawable.shooting)))

            imageView.setScaleAndCenter(5f, PointF((victimPoint.x + killerPoint.x) / 2, (victimPoint.y + killerPoint.y) / 2))
        } else {
            imageView.setScaleAndCenter(5f, PointF(victimPoint.x, victimPoint.y))
        }

        imageView.setPin(Pin(victimPoint, "VICTIM", getBitmapDrawable(R.drawable.headstone_color)))
    }

    private fun getBitmapDrawable(int: Int): Bitmap {
        return Bitmap.createScaledBitmap(BitmapFactory.decodeResource(resources, int), 100, 100, true)
    }
}