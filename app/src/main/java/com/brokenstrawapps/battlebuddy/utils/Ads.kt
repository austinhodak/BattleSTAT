package com.brokenstrawapps.battlebuddy.utils

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdRequest

object Ads {

    lateinit var mSharedPreferences: SharedPreferences
    private const val ADS_CONSENT = "personalized_ads_enabled"

    fun init(context: Context) {
        mSharedPreferences = context.getSharedPreferences("com.brokenstrawapps.battlebuddy", Context.MODE_PRIVATE)
    }


    /**
     * Get the adRequest Builder and add Test Device is neccessary
     * @return AdRequest Builder
     */
    fun getAdBuilder(): AdRequest? {
        if (!isConsentGiven()) {
            val bundle = Bundle()
            bundle.putString("npa", "1")
            return AdRequest.Builder()
                    .addNetworkExtrasBundle(AdMobAdapter::class.java, bundle)
                    .addTestDevice("C056F91F1F8DDD938A0ADC1D97436D8C")
                    .build()
        }
        return AdRequest.Builder()
                .addTestDevice("C056F91F1F8DDD938A0ADC1D97436D8C")
                .build()
    }

    /**
     * Set the consent status
     * @param isGiven Consent is given or not
     */
    fun setConsentGiven(isGiven: Boolean) {
        mSharedPreferences.edit().putBoolean(ADS_CONSENT, isGiven).apply()
    }


    /**
     * Check is consent is given
     * @return true or false
     */
    fun isConsentGiven(): Boolean {
        return mSharedPreferences.getBoolean(ADS_CONSENT, false)
    }
}