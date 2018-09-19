package com.respondingio.battlegroundsbuddy.info;

import static android.content.Context.MODE_PRIVATE;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.Chronometer.OnChronometerTickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.respondingio.battlegroundsbuddy.R;
import java.util.ArrayList;
import java.util.List;

public class TimerFragment extends Fragment {

    @BindView(R.id.next_drop)
    TextView airDrop;

    long airDropTimer = 246000;

    @BindView(R.id.circle_eigth)
    TextView eigthCircle;

    @BindView(R.id.circle_fifth)
    TextView fifthCircle;

    @BindView(R.id.first_blue_circle)
    TextView firstBlueCircle;

    CountDownTimer firstCircleAppearCountDown;

    @BindView(R.id.first_circle)
    TextView firstCircleCountdown;

    @BindView(R.id.circle_fourth)
    TextView fourthCircle;

    boolean isTimerRunning = false;

    @BindView(R.id.circle_second)
    TextView secondCircle;

    @BindView(R.id.circle_seven)
    TextView sevenCircle;

    @BindView(R.id.circle_six)
    TextView sixCircle;

    @BindView(R.id.circle_third)
    TextView thirdCircle;

    long timeAtStop = 0;

    @BindView(R.id.timer_minus)
    ImageView timerMinus;

    @BindView(R.id.timer_plus)
    ImageView timerPlus;

    @BindView(R.id.timer_start)
    LinearLayout timerStart;

    @BindView(R.id.timer_stop)
    LinearLayout timerStop;

    @BindView(R.id.timer_total)
    Chronometer totalMatch;

    long totalMatchTimer = 0;

    private long airDropDelay = 180000;

    private SharedPreferences mSharedPreferences;

    private List<PendingIntent> circlePendingIntents = new ArrayList<>();

    Intent intent;

    public TimerFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_timer, container, false);
        ButterKnife.bind(this, view);
        intent = new Intent(getActivity(), AlarmReceiver.class);

        setHasOptionsMenu(true);
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mSharedPreferences = getActivity().getSharedPreferences("com.austinhodak.pubgcenter", MODE_PRIVATE);

        if (mSharedPreferences.contains("timer_last") && mSharedPreferences.contains("timer_sys") && mSharedPreferences.contains("timer_running")) {
            if (mSharedPreferences.getLong("timer_last", 0) == 0 || mSharedPreferences.getLong("timer_sys", 0) == 0) {

            } else {
                Log.d("TIMER", "SHARED");
                if (mSharedPreferences.getBoolean("timer_running", false)) {
                    totalMatchTimer = mSharedPreferences.getLong("timer_last", 0);
                    totalMatch.setBase(mSharedPreferences.getLong("timer_sys", SystemClock.elapsedRealtime()) + totalMatchTimer);
                    totalMatch.start();
                    isTimerRunning = true;
                } else {
                    totalMatchTimer = mSharedPreferences.getLong("timer_last", 0);
                    totalMatch.setBase(SystemClock.elapsedRealtime() + totalMatchTimer);
                    updateTimers();
                }
            }
        }

        timerStart.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                totalMatch.setBase(SystemClock.elapsedRealtime() + totalMatchTimer);
                totalMatch.start();
                isTimerRunning = true;

                startAlarmManager();
            }
        });

        timerStart.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(final View v) {

                return false;
            }
        });

        timerStop.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (!isTimerRunning) {
                    return;
                }
                totalMatchTimer = totalMatch.getBase() - SystemClock.elapsedRealtime();
                totalMatch.stop();
                isTimerRunning = false;

                stopAlarmManager();
            }
        });

        totalMatch.setOnChronometerTickListener(new OnChronometerTickListener() {
            @Override
            public void onChronometerTick(final Chronometer chronometerC) {
                totalMatch = chronometerC;
                totalMatchTimer = totalMatch.getBase() - SystemClock.elapsedRealtime();

                updateTimers();
            }
        });

        timerPlus.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                totalMatch.stop();
                totalMatch.setBase(SystemClock.elapsedRealtime() + totalMatchTimer - 1000);
                stopAlarmManager();

                if (isTimerRunning) {
                    totalMatch.start();
                } else {
                    updateTimers();
                }
            }
        });

        timerPlus.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(final View v) {
                totalMatch.stop();
                totalMatch.setBase(SystemClock.elapsedRealtime() + totalMatchTimer - 30000);
                stopAlarmManager();

                if (isTimerRunning) {
                    totalMatch.start();
                } else {
                    updateTimers();
                }
                return true;
            }
        });

        timerMinus.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                totalMatch.stop();
                totalMatch.setBase(SystemClock.elapsedRealtime() + totalMatchTimer + 1000);
                stopAlarmManager();

                if (isTimerRunning) {
                    totalMatch.start();
                } else {
                    updateTimers();
                }
            }
        });

        timerMinus.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(final View v) {
                totalMatch.stop();
                totalMatch.setBase(SystemClock.elapsedRealtime() + totalMatchTimer + 30000);
                stopAlarmManager();

                if (isTimerRunning) {
                    totalMatch.start();
                } else {
                    updateTimers();
                }
                return true;
            }
        });

        initChannels(getActivity());

        try {
            if (FirebaseRemoteConfig.getInstance().getLong("airdrop_timing_ms") != 0) {
                airDropDelay = FirebaseRemoteConfig.getInstance().getLong("airdrop_timing_ms");
            }
            Log.d("DELAY", airDropDelay + " : DELAY");

        } catch (Exception e) {

        }



        circlePendingIntents.add(PendingIntent.getBroadcast(getActivity(), 1, intent, PendingIntent.FLAG_UPDATE_CURRENT));
        circlePendingIntents.add(PendingIntent.getBroadcast(getActivity(), 2, intent, PendingIntent.FLAG_UPDATE_CURRENT));
        circlePendingIntents.add(PendingIntent.getBroadcast(getActivity(), 3, intent, PendingIntent.FLAG_UPDATE_CURRENT));
        circlePendingIntents.add(PendingIntent.getBroadcast(getActivity(), 4, intent, PendingIntent.FLAG_UPDATE_CURRENT));
        circlePendingIntents.add(PendingIntent.getBroadcast(getActivity(), 5, intent, PendingIntent.FLAG_UPDATE_CURRENT));
        circlePendingIntents.add(PendingIntent.getBroadcast(getActivity(), 6, intent, PendingIntent.FLAG_UPDATE_CURRENT));
        circlePendingIntents.add(PendingIntent.getBroadcast(getActivity(), 7, intent, PendingIntent.FLAG_UPDATE_CURRENT));
        circlePendingIntents.add(PendingIntent.getBroadcast(getActivity(), 8, intent, PendingIntent.FLAG_UPDATE_CURRENT));

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mSharedPreferences.edit().putLong("timer_last", totalMatchTimer).apply();
        mSharedPreferences.edit().putLong("timer_sys", SystemClock.elapsedRealtime()).apply();
        mSharedPreferences.edit().putBoolean("timer_running", isTimerRunning).apply();
    }

    public void initChannels(Context context) {
        if (Build.VERSION.SDK_INT < 26) {
            return;
        }
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel("blue_zone",
                "Blue Zone",
                NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription("Blue Zone Notification");
        notificationManager.createNotificationChannel(channel);
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.timer_fragmentr, menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.timer_reset:
                totalMatchTimer = 0;
                totalMatch.stop();
                totalMatch.setBase(SystemClock.elapsedRealtime());
                firstCircleCountdown.setText("1:50");
                firstBlueCircle.setText("6:50");
                secondCircle.setText("15:10");
                thirdCircle.setText("20:00");
                fourthCircle.setText("23:30");
                fifthCircle.setText("26:30");
                sixCircle.setText("28:40");
                sevenCircle.setText("30:40");
                eigthCircle.setText("32:10");
                airDrop.setText("--");

                firstCircleCountdown.setTextColor(Color.WHITE);
                firstBlueCircle.setTextColor(Color.WHITE);
                secondCircle.setTextColor(Color.WHITE);
                thirdCircle.setTextColor(Color.WHITE);
                fourthCircle.setTextColor(Color.WHITE);
                fifthCircle.setTextColor(Color.WHITE);
                sixCircle.setTextColor(Color.WHITE);
                sevenCircle.setTextColor(Color.WHITE);
                eigthCircle.setTextColor(Color.WHITE);

                airDrop.setTextColor(Color.WHITE);

                airDropTimer = 246000;

                mSharedPreferences.edit().remove("timer_last").remove("timer_sys").remove("timer_running").commit();
                break;
            case R.id.timer_setting:

                boolean notifyEnabled = mSharedPreferences.getBoolean("timerNotifyEnabled", true);
                boolean notifyDropEnabled = mSharedPreferences.getBoolean("airDropNotifyEnabled", true);

                List<Integer> myList = new ArrayList<Integer>();

                if (notifyEnabled) {
                    myList.add(0);
                }

                if (notifyDropEnabled) {
                    myList.add(1);
                }

                Integer[] selected = new Integer[myList.size()];
                myList.toArray(selected);

                final String[] items = {"Enable Notifications", "Enable Airdrop Notifications"};
//                MaterialDialog materialDialog = new MaterialDialog(getActivity())
//                        .title("Timer Settings")
//                        .items(items)
//                        .itemsCallbackMultiChoice(selected, new MaterialDialog.ListCallbackMultiChoice() {
//                            @Override
//                            public boolean onSelection(MaterialDialog dialog, Integer[] which, CharSequence[] text) {
//
//                                if (which.length == 0) {
//                                    Log.d("TIMER", "NOTIFY DISABLED BOTH");
//                                    mSharedPreferences.edit().putBoolean("airDropNotifyEnabled", false).apply();
//                                    mSharedPreferences.edit().putBoolean("timerNotifyEnabled", false).apply();
//
//                                    return true;
//                                } else if (which.length == 2) {
//                                    Log.d("TIMER", "NOTIFY ENABLED BOTH");
//                                    mSharedPreferences.edit().putBoolean("airDropNotifyEnabled", true).apply();
//                                    mSharedPreferences.edit().putBoolean("timerNotifyEnabled", true).apply();
//
//                                    return true;
//                                }
//
//                                for (final Integer aWhich : which) {
//                                    if (aWhich == 0) {
//                                        Log.d("TIMER", "NOTIFY ENABLED");
//                                        mSharedPreferences.edit().putBoolean("timerNotifyEnabled", true).apply();
//                                    } else {
//                                        Log.d("TIMER", "NOTIFY DISABLED");
//                                        mSharedPreferences.edit().putBoolean("timerNotifyEnabled", false).apply();
//                                    }
//
//                                    if (aWhich == 1) {
//                                        Log.d("TIMER", "NOTIFY ENABLED DROP");
//                                        mSharedPreferences.edit().putBoolean("airDropNotifyEnabled", true).apply();
//                                    } else {
//                                        Log.d("TIMER", "NOTIFY DISABLED DROP");
//                                        mSharedPreferences.edit().putBoolean("airDropNotifyEnabled", false).apply();
//                                    }
//                                }
//
//                                return true;
//                            }
//                        })
//                        .positiveText("DONE")
//                        .show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void createAirDropNotification(String text) {

        if (!mSharedPreferences.getBoolean("airDropNotifyEnabled", true)) {
            return;
        }

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getActivity(), "blue_zone");

        long[] v = {1000, 500, 1000, 500};

        mBuilder
                .setSmallIcon(R.drawable.ic_parachute)
                .setContentTitle(text)
                .setContentText("Check the skies! An Air Drop should be coming soon!")
                .setVibrate(v)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getActivity());
        notificationManager.notify(101, mBuilder.build());

    }

    private void createNotification(String text) {

        if (!mSharedPreferences.getBoolean("timerNotifyEnabled", true)) {
            return;
        }

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getActivity(), "blue_zone");

        long[] v = {1000, 1000, 1000, 1000};

        mBuilder
                .setSmallIcon(R.drawable.icons8_rifle)
                .setContentTitle(text)
                .setContentText("Get moving! The Blue Zone starts moving soon!")
                .setVibrate(v)
                .setLights(Color.RED, 1000, 1000)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getActivity());

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(100, mBuilder.build());

        //Vibrator v = (Vibrator) this.getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 500 milliseconds
        //v.vibrate(1000);
    }

    private void updateTimers() {
        //First Circle Appear Timer 111000
        long milli = totalMatch.getBase() - SystemClock.elapsedRealtime() + 111000;
        if (milli > 0) {
            long seconds = (milli / 1000) % 60;
            long minutes = (milli / 1000) / 60;

            if (seconds < 10) {
                firstCircleCountdown.setText((milli / 1000) / 60 + ":0" + (milli / 1000) % 60);
            } else {
                firstCircleCountdown.setText((milli / 1000) / 60 + ":" + (milli / 1000) % 60);
            }

            if (minutes == 0 && seconds == 30) {
                firstCircleCountdown.setTextColor(getResources().getColor(R.color.md_orange_A400));

                //createNotification("1st Blue Zone Starts in 30 Seconds!");
            }

        } else {
            firstCircleCountdown.setTextColor(getResources().getColor(R.color.md_red_500));
        }

        long firstBlue = totalMatch.getBase() - SystemClock.elapsedRealtime() + 411000;
        if (firstBlue > 0) {
            long seconds = (firstBlue / 1000) % 60;
            long minutes = (firstBlue / 1000) / 60;

            if (seconds < 10) {
                firstBlueCircle.setText((firstBlue / 1000) / 60 + ":0" + (firstBlue / 1000) % 60);
            } else {
                firstBlueCircle.setText((firstBlue / 1000) / 60 + ":" + (firstBlue / 1000) % 60);
            }

            if (milli < 0 && minutes >= 1) {
                firstBlueCircle.setTextColor(getResources().getColor(R.color.md_green_A400));
            }

            if (minutes == 0 && seconds == 30) {
                firstBlueCircle.setTextColor(getResources().getColor(R.color.md_orange_500));

                createNotification("1st Circle Starts in 30 Seconds!");
            }

        } else {
            firstBlueCircle.setTextColor(getResources().getColor(R.color.md_red_500));
        }

        long secondCircleMill = totalMatch.getBase() - SystemClock.elapsedRealtime() + 911000;
        if (secondCircleMill > 0) {
            long seconds = (secondCircleMill / 1000) % 60;
            long minutes = (secondCircleMill / 1000) / 60;

            if (seconds < 10) {
                secondCircle.setText((secondCircleMill / 1000) / 60 + ":0" + (secondCircleMill / 1000) % 60);
            } else {
                secondCircle.setText((secondCircleMill / 1000) / 60 + ":" + (secondCircleMill / 1000) % 60);
            }

            if (firstBlue < 0 && minutes >= 1) {
                secondCircle.setTextColor(getResources().getColor(R.color.md_green_A400));
            }

            if (minutes == 0 && seconds == 30) {
                secondCircle.setTextColor(getResources().getColor(R.color.md_orange_500));

                createNotification("2nd Circle Starts in 30 Seconds!");
            }
        } else {
            secondCircle.setTextColor(getResources().getColor(R.color.md_red_500));
        }

        long thirdCirc = totalMatch.getBase() - SystemClock.elapsedRealtime() + 1201000;
        if (thirdCirc > 0) {
            long seconds = (thirdCirc / 1000) % 60;
            long minutes = (thirdCirc / 1000) / 60;

            if (seconds < 10) {
                thirdCircle.setText((thirdCirc / 1000) / 60 + ":0" + (thirdCirc / 1000) % 60);
            } else {
                thirdCircle.setText((thirdCirc / 1000) / 60 + ":" + (thirdCirc / 1000) % 60);
            }

            if (secondCircleMill < 0 && minutes >= 1) {
                thirdCircle.setTextColor(getResources().getColor(R.color.md_green_A400));
            }

            if (minutes == 0 && seconds == 30) {
                thirdCircle.setTextColor(getResources().getColor(R.color.md_orange_500));

                createNotification("3rd Circle Starts in 30 Seconds!");
            }
        } else {
            thirdCircle.setTextColor(getResources().getColor(R.color.md_red_500));
        }

        long fourthCirc = totalMatch.getBase() - SystemClock.elapsedRealtime() + 1411000;
        if (fourthCirc > 0) {
            long seconds = (fourthCirc / 1000) % 60;
            long minutes = (fourthCirc / 1000) / 60;

            if (seconds < 10) {
                fourthCircle.setText((fourthCirc / 1000) / 60 + ":0" + (fourthCirc / 1000) % 60);
            } else {
                fourthCircle.setText((fourthCirc / 1000) / 60 + ":" + (fourthCirc / 1000) % 60);
            }

            if (thirdCirc < 0 && minutes >= 1) {
                fourthCircle.setTextColor(getResources().getColor(R.color.md_green_A400));
            }

            if (minutes == 0 && seconds == 30) {
                fourthCircle.setTextColor(getResources().getColor(R.color.md_orange_500));

                createNotification("4th Circle Starts in 30 Seconds!");
            }
        } else {
            fourthCircle.setTextColor(getResources().getColor(R.color.md_red_500));
        }

        long fifthCirc = totalMatch.getBase() - SystemClock.elapsedRealtime() + 1591000;
        if (fifthCirc > 0) {
            long seconds = (fifthCirc / 1000) % 60;
            long minutes = (fifthCirc / 1000) / 60;

            if (seconds < 10) {
                fifthCircle.setText((fifthCirc / 1000) / 60 + ":0" + (fifthCirc / 1000) % 60);
            } else {
                fifthCircle.setText((fifthCirc / 1000) / 60 + ":" + (fifthCirc / 1000) % 60);
            }

            if (fourthCirc < 0 && minutes >= 1) {
                fifthCircle.setTextColor(getResources().getColor(R.color.md_green_A400));
            }

            if (minutes == 0 && seconds == 30) {
                fifthCircle.setTextColor(getResources().getColor(R.color.md_orange_500));

                createNotification("5th Circle Starts in 30 Seconds!");
            }
        } else {
            fifthCircle.setTextColor(getResources().getColor(R.color.md_red_500));
            sixCircle.setTextColor(getResources().getColor(R.color.md_green_A400));
        }

        long sixCirc = totalMatch.getBase() - SystemClock.elapsedRealtime() + 1721000;
        if (sixCirc > 0) {
            long seconds = (sixCirc / 1000) % 60;
            long minutes = (sixCirc / 1000) / 60;

            if (seconds < 10) {
                sixCircle.setText((sixCirc / 1000) / 60 + ":0" + (sixCirc / 1000) % 60);
            } else {
                sixCircle.setText((sixCirc / 1000) / 60 + ":" + (sixCirc / 1000) % 60);
            }

            if (fifthCirc < 0 && minutes >= 1) {
                sixCircle.setTextColor(getResources().getColor(R.color.md_green_A400));
            }

            if (minutes == 0 && seconds == 30) {
                sixCircle.setTextColor(getResources().getColor(R.color.md_orange_500));

                createNotification("6th Circle Starts in 30 Seconds!");
            }
        } else {
            sixCircle.setTextColor(getResources().getColor(R.color.md_red_500));
        }

        long sevenCirc = totalMatch.getBase() - SystemClock.elapsedRealtime() + 1841000;
        if (sevenCirc > 0) {
            long seconds = (sevenCirc / 1000) % 60;
            long minutes = (sevenCirc / 1000) / 60;

            if (seconds < 10) {
                sevenCircle.setText((sevenCirc / 1000) / 60 + ":0" + (sevenCirc / 1000) % 60);
            } else {
                sevenCircle.setText((sevenCirc / 1000) / 60 + ":" + (sevenCirc / 1000) % 60);
            }

            if (sixCirc < 0 && minutes >= 1) {
                sevenCircle.setTextColor(getResources().getColor(R.color.md_green_A400));
            }

            if (minutes == 0 && seconds == 30) {
                sevenCircle.setTextColor(getResources().getColor(R.color.md_orange_500));

                createNotification("7th Circle Starts in 30 Seconds!");
            }
        } else {
            sevenCircle.setTextColor(getResources().getColor(R.color.md_red_500));
        }

        long eigthCirc = totalMatch.getBase() - SystemClock.elapsedRealtime() + 1931000;
        if (eigthCirc > 0) {
            long seconds = (eigthCirc / 1000) % 60;
            long minutes = (eigthCirc / 1000) / 60;

            if (seconds < 10) {
                eigthCircle.setText((eigthCirc / 1000) / 60 + ":0" + (eigthCirc / 1000) % 60);
            } else {
                eigthCircle.setText((eigthCirc / 1000) / 60 + ":" + (eigthCirc / 1000) % 60);
            }

            if (sevenCirc < 0 && minutes >= 1) {
                eigthCircle.setTextColor(getResources().getColor(R.color.md_green_A400));
            }

            if (minutes == 0 && seconds == 30) {
                eigthCircle.setTextColor(getResources().getColor(R.color.md_orange_500));

                createNotification("8th Circle Starts in 30 Seconds!");
            }
        } else {
            eigthCircle.setTextColor(getResources().getColor(R.color.md_red_500));
        }

        long airDropMil = totalMatch.getBase() - SystemClock.elapsedRealtime() + airDropTimer;
        if (airDropMil > 0) {
            long seconds = (airDropMil / 1000) % 60;
            long minutes = (airDropMil / 1000) / 60;

            if (seconds < 10) {
                airDrop.setText((airDropMil / 1000) / 60 + ":0" + (airDropMil / 1000) % 60);
            } else {
                airDrop.setText((airDropMil / 1000) / 60 + ":" + (airDropMil / 1000) % 60);
            }
        } else if (airDropMil < 0) {
            createAirDropNotification("Air Drop inbound!");
            //Reset drop to two minutes-ish (wait for feedback before final)
            airDropTimer = airDropTimer + airDropDelay;
        }
    }

    private void startAlarmManager() {
//        AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
//        Log.d("TIMER BASE", totalMatch.getBase() + " : " + SystemClock.elapsedRealtime() + " : " + (totalMatch.getBase() - SystemClock.elapsedRealtime() + 10000));
//
//        alarmManager.set(AlarmManager.RTC, totalMatch.getBase() + 10000, PendingIntent.getBroadcast(getActivity(), 1, intent, PendingIntent.FLAG_UPDATE_CURRENT));
//
//        for (PendingIntent i : circlePendingIntents) {
//            switch (circlePendingIntents.indexOf(i)) {
//                case 0:
//                    break;
//            }
//        }
    }

    private void stopAlarmManager() {
//        Log.d("TIMER", "ALARM STOP");
//        AlarmManager manager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
//        manager.cancel(PendingIntent.getBroadcast(getActivity(), 1, intent, PendingIntent.FLAG_UPDATE_CURRENT));
    }
}