package com.brokenstrawapps.battlebuddy.stats.matchdetails

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.PointF
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.view.Window
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.brokenstrawapps.battlebuddy.R
import com.brokenstrawapps.battlebuddy.R.id
import com.brokenstrawapps.battlebuddy.Telemetry
import com.brokenstrawapps.battlebuddy.map.Map
import com.brokenstrawapps.battlebuddy.models.LogPlayerKill
import com.brokenstrawapps.battlebuddy.models.Pin
import com.brokenstrawapps.battlebuddy.models.SafeZoneCircle
import com.brokenstrawapps.battlebuddy.views.PinSet
import com.davemorrissey.labs.subscaleview.ImageSource
import kotlinx.android.synthetic.main.maptile_activity.*
import net.idik.lib.slimadapter.SlimAdapter
import net.idik.lib.slimadapter.SlimInjector
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class KillFeedListMap : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent == null) {
            finish()
        }
        this.requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.maptile_activity)

        val safeZoneCircleList = intent.getSerializableExtra("safeZoneCircleList") as ArrayList<SafeZoneCircle>

        val mapName = intent.getStringExtra("mapName")
        val imageView = pinview
        imageView.setImage(ImageSource.uri(Uri.fromFile(File(filesDir, mapName))))
        imageView.maxScale = 36f

        val killFeedLogPlayerKill = intent.getSerializableExtra("killLog") as LogPlayerKill

        var map: Map? = Map.ERANGEL_LOW
        for (item in Map.values()) {
            if (item.fileName == mapName) map = item
        }

        val offset = when (map) {
            Map.MIRAMAR_LOW -> 4.048
            Map.SANHOK_LOW -> 3.41333333
            Map.VIKENDI_LOW -> 3.072
            Map.SANHOK_HIGH -> 0.5
            Map.KARAKIN_HIGH -> 0.25
            Map.KARAKIN_LOW -> 1.666
            else -> 1.0
        }

        val pinSet = PinSet(drawLine = true)

        if (killFeedLogPlayerKill.killer?.location?.isValidLocation() == true) {
            val killerPoint = PointF((killFeedLogPlayerKill.killer.location.x / 100 / offset).toFloat(), (killFeedLogPlayerKill.killer.location.y / 100 / offset).toFloat())
            pinSet.pin1 = Pin(killerPoint, "KILLER", getBitmapDrawable(R.drawable.pin_gun))
        }

        if (killFeedLogPlayerKill.victim.location.isValidLocation()) {
            val victimPoint = PointF((killFeedLogPlayerKill.victim.location.x / 100 / offset).toFloat(), (killFeedLogPlayerKill.victim.location.y / 100 / offset).toFloat())
            imageView.setScaleAndCenter(5f, PointF(victimPoint.x, victimPoint.y))
            pinSet.pin2 = Pin(victimPoint, "VICTIM", getBitmapDrawable(R.drawable.point_skull))
        }

        imageView.addKill(pinSet, true)

        imageView.placeSafeZones(processCircles(safeZoneCircleList, offset))
    }

    private fun loadList(list: ArrayList<LogPlayerKill>, createdAt: String) {
        mapRV.layoutManager = LinearLayoutManager(this)

        SlimAdapter.create().attachTo(mapRV).register(R.layout.stats_kill_feed_item, SlimInjector<LogPlayerKill> { data, injector ->
            injector.text(id.kill_feed_killer, "")
            injector.text(id.kill_feed_victim, "")

            if (data.killer?.name?.isEmpty() == true) {
                data.killer.name = Telemetry().damageTypeCategory[data.damageTypeCategory].toString()
                injector.typeface(id.kill_feed_killer, injector.findViewById<TextView>(id.kill_feed_killer).typeface, Typeface.ITALIC)
            } else {
                injector.typeface(id.kill_feed_killer, injector.findViewById<TextView>(id.kill_feed_killer).typeface, Typeface.NORMAL)
            }

            injector.text(id.kill_feed_killer, data.killer?.name?.trim())
            injector.text(id.kill_feed_victim, data.victim.name.trim())

            if (Telemetry().damageCauserName[data.damageCauserName].toString() == "Player") {
                injector.text(id.kill_feed_cause, Telemetry().damageTypeCategory[data.damageTypeCategory].toString())
            } else {
                injector.text(id.kill_feed_cause, Telemetry().damageCauserName[data.damageCauserName].toString())
            }

            injector.text(id.textView9, (list.size - list.indexOf(data)).toString())

//            when {
//                data.killer.accountId == activity.currentPlayerID -> injector.background(id.textView9, drawable.chip_green_outline)
//                data.victim.accountId == activity.currentPlayerID -> injector.background(id.textView9, drawable.chip_red_outline)
//                else -> injector.background(id.textView9, drawable.chip_grey_outline)
//            }

            //Do date.
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
            sdf.timeZone = TimeZone.getTimeZone("GMT")

            val sdf2 = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            sdf2.timeZone = TimeZone.getTimeZone("GMT")

            val matchStartDate = sdf.parse(createdAt)
            val killTime = sdf2.parse(data._D)

            var difference = killTime.time - matchStartDate.time

            val secondsInMilli: Long = 1000
            val minutesInMilli = secondsInMilli * 60
            val hoursInMilli = minutesInMilli * 60
            val daysInMilli = hoursInMilli * 24

            difference %= daysInMilli

            difference %= hoursInMilli

            val elapsedMinutes = difference / minutesInMilli
            difference %= minutesInMilli

            val elapsedSeconds = difference / secondsInMilli

            injector.text(id.kill_feed_time, String.format("%02d:%02d", elapsedMinutes, elapsedSeconds))

            injector.text(id.kill_feed_distance, "${String.format("%.0f", Math.rint(data.distance/100))}m")

            injector.clicked(id.kill_feed_top) {

            }
        }).updateData(list)
    }

    private fun getBitmapDrawable(int: Int): Bitmap {
        return Bitmap.createScaledBitmap(BitmapFactory.decodeResource(resources, int), 100, 121, false)
    }

    private fun processCircles(list: ArrayList<SafeZoneCircle>, offset: Double) : ArrayList<SafeZoneCircle> {
        for (item in list) {
            item.position.x = item.position.x / 100 / offset
            item.position.y = item.position.y / 100 / offset
            item.radius = item.radius / 100 / offset
        }

        return list
    }
}