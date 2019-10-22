package com.ahcjapps.battlebuddy.stats.match_watch

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.ahcjapps.battlebuddy.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MatchWatcher : JobService() {


    override fun onStopJob(params: JobParameters?): Boolean {

        return false
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        //1. Listen to players /allMatches/ for new match matching last 6 digits of provided match (accountID, platform, season)
        //2. RUN get player matches
        //3. Listen for 10 seconds after RUN is complete, if found notify of match being found. (Preload?)


        setupListener(params)

        return true
    }

    private fun setupListener(params: JobParameters?) {
        if (params == null) jobFinished(params, false)
        val playerID = params!!.extras.getString("playerID")
        val platformID = params.extras.getString("platformID")
        val seasonID = params.extras.getString("seasonID")
        val lastSix = params.extras.getString("lastSix")

        FirebaseDatabase.getInstance().reference.child("user_stats/$playerID/allMatches/$platformID/${seasonID.toLowerCase()}/matches").orderByChild("createdAt").limitToLast(5).addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                if (!p0.exists()) return

                for (child in p0.children) {
                    if (child.key!!.substring(child.key!!.length - 6) == lastSix) {
                        //Match Found
                        createNotificationChannel()
                        var builder = NotificationCompat.Builder(applicationContext, "MATCH_WATCH")
                                .setSmallIcon(R.drawable.grandmaster_noti)
                                .setContentTitle("")
                    }
                }
            }
        })
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Match Watcher"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("MATCH_WATCH", name, importance)
            // Register the channel with the system
            val notificationManager: NotificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}