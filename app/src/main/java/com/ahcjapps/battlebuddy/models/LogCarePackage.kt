package com.ahcjapps.battlebuddy.models

import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

data class LogCarePackage (
        val itemPackageId: String,
        val location: LogLocation,
        val items: List<LogItem>,
        var elapsedTime: Long
)

data class LogCarePackageLand (
        val itemPackage: LogCarePackage,
        val _D: String,
        val _T: String
) {
    fun doElapsedTime(createdAt: String) : LogCarePackageLand {
        //Do date.
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
        sdf.timeZone = TimeZone.getTimeZone("GMT")

        val sdf2 = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        sdf2.timeZone = TimeZone.getTimeZone("GMT")

        val matchStartDate = sdf.parse(createdAt)
        val killTime = sdf2.parse(_D)

        val difference = killTime.time - matchStartDate.time
        itemPackage.elapsedTime = difference / 1000
        Log.d("ELAPSEDCARE", "${itemPackage.elapsedTime}")

        return this
    }

    fun getTimeString(createdAt: String): String {
        //Do date.
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
        sdf.timeZone = TimeZone.getTimeZone("GMT")

        val sdf2 = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        sdf2.timeZone = TimeZone.getTimeZone("GMT")

        val matchStartDate = sdf.parse(createdAt)
        val killTime = sdf2.parse(_D)

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

        return String.format("%02d:%02d", elapsedMinutes, elapsedSeconds)
    }
}

data class LogCarePackageSpawn (
        val itemPackage: LogCarePackage,
        val _D: String,
        val _T: String
) {
    fun getTimeString(createdAt: String): String {
        //Do date.
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
        sdf.timeZone = TimeZone.getTimeZone("GMT")

        val sdf2 = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        sdf2.timeZone = TimeZone.getTimeZone("GMT")

        val matchStartDate = sdf.parse(createdAt)
        val killTime = sdf2.parse(_D)

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

        return String.format("%02d:%02d", elapsedMinutes, elapsedSeconds)
    }
}