package com.respondingio.battlegroundsbuddy.info;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import com.respondingio.battlegroundsbuddy.R;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        createNotification("Circle Starts in 30 seconds!", context);
    }

    private void createNotification(String text, Context context) {

//        if (!mSharedPreferences.getBoolean("timerNotifyEnabled", true)) {
//            return;
//        }

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, "blue_zone");

        long[] v = {1000, 1000, 1000, 1000};

        mBuilder
                .setSmallIcon(R.drawable.icons8_rifle)
                .setContentTitle(text)
                .setContentText("Get moving! The Blue Zone starts moving soon!")
                .setVibrate(v)
                .setLights(Color.RED, 1000, 1000)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(100, mBuilder.build());

        //Vibrator v = (Vibrator) this.getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 500 milliseconds
        //v.vibrate(1000);
    }
}
