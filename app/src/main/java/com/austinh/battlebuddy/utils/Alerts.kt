package com.austinh.battlebuddy.utils

import android.content.Context
import android.content.SharedPreferences

object Alerts {
    lateinit var mSharedPreferences: SharedPreferences

    fun init(context: Context) {
        mSharedPreferences = context.getSharedPreferences("com.austinh.battlebuddy", Context.MODE_PRIVATE)
    }

    /**
     * Set the selected alert active
     *
     * @param alert Takes an Alert
     * @param enabled Whether the alert is active or not
     */
    fun setAlertActive(alert: Alert, enabled: Boolean) {
        mSharedPreferences.edit().putBoolean(alert.tag, enabled).apply()
    }

    /**
     * Checks if alert is active
     *
     * @param alert Takes an Alert
     * @return Is active or not
     */
    fun isAlertActive(alert: Alert): Boolean {
        return mSharedPreferences.getBoolean(alert.tag, false)
    }

    enum class Alert(val tag: String) {
        PC_MAINT("ALERT_PC_MAINT"),
        PC_UPDATE("ALERT_PC_UPDATE"),
        XBOX_MAINT("ALERT_XBOX_MAINT"),
        XBOX_UPDATE("ALERT_XBOX_UPDATE"),
        MAJOR_NEWS("ALERT_MAJOR_NEWS"),
        PS4_MAINT("ALERT_PS4_MAINT"),
        PS4_UPDATE("ALERT_PS4_UPDATE")
    }
}