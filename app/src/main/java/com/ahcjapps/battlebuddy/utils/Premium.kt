package com.ahcjapps.battlebuddy.utils

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import com.android.billingclient.api.*
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object Premium {

    lateinit var mOldSharedPreferences: SharedPreferences
    lateinit var mSharedPreferences: SharedPreferences
    lateinit var billingClient: BillingClient

    fun init(context: Context) {
        mOldSharedPreferences = context.getSharedPreferences("com.austinhodak.pubgcenter", Context.MODE_PRIVATE)
        mSharedPreferences = context.getSharedPreferences("com.austinh.battlebuddy", Context.MODE_PRIVATE)
        setupBilling(context)

        GlobalScope.launch(Dispatchers.Main, CoroutineStart.DEFAULT) { getIAPs() }

    }

    private fun setupBilling(context: Context) {
        billingClient = BillingClient.newBuilder(context).setListener { responseCode, purchases ->
            if (responseCode == BillingClient.BillingResponse.OK && purchases != null) {
                for (purchase in purchases) {
                    handlePurchase(purchase)
                }
            } else if (responseCode == BillingClient.BillingResponse.USER_CANCELED) {
                // Handle an error caused by a user cancelling the purchase flow.
            } else {
                // Handle any other error codes.
            }
        }.build()

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {}

            override fun onBillingSetupFinished(responseCode: Int) {
                if (responseCode == BillingClient.BillingResponse.OK) {
                    // The billing client is ready. You can query purchases here.
                    val purchases = billingClient.queryPurchases(BillingClient.SkuType.INAPP)
                    for (purchase in purchases.purchasesList) {
                        handlePurchase(purchase)
                    }

                    if (purchases.purchasesList.isEmpty()) {
                        clearUserLevel()
                    }
                }
            }
        })
    }

    fun isAdFreeUser() : Boolean {
        return when {
            mOldSharedPreferences.getBoolean("removeAds", false) -> true
            mSharedPreferences.getBoolean("premiumV1", false) -> true
            mSharedPreferences.getBoolean(Level.LEVEL_1.tag, false) -> true
            mSharedPreferences.getBoolean(Level.LEVEL_2.tag, false) -> true
            else -> mSharedPreferences.getBoolean(Level.LEVEL_3.tag, false)
        }
    }

    fun isPremiumUser() : Boolean {
        return when {
            mSharedPreferences.getBoolean("premiumV1", false) -> true
            mSharedPreferences.getBoolean(Level.LEVEL_1.tag, false) -> true
            mSharedPreferences.getBoolean(Level.LEVEL_2.tag, false) -> true
            else -> mSharedPreferences.getBoolean(Level.LEVEL_3.tag, false)
        }
    }

    fun getUserLevel() : Level {
        return when {
            mSharedPreferences.getBoolean(Level.LEVEL_3.tag, false) -> Level.LEVEL_3
            mSharedPreferences.getBoolean(Level.LEVEL_2.tag, false) -> Level.LEVEL_2
            mSharedPreferences.getBoolean("premiumV1", false) -> Level.LEVEL_3
            mOldSharedPreferences.getBoolean("removeAds", false) -> Level.LEVEL_1
            mSharedPreferences.getBoolean(Level.LEVEL_1.tag, false) -> Level.LEVEL_1
            else -> Level.FREE
        }
    }

    fun isUserLevel1() : Boolean {
        return if (mOldSharedPreferences.getBoolean("removeAds", false)) true
        else mSharedPreferences.getBoolean(Level.LEVEL_1.tag, false)
    }

    fun isUserLevel2() : Boolean {
        return mSharedPreferences.getBoolean(Level.LEVEL_2.tag, false)
    }

    fun isUserLevel3() : Boolean {
        return if (mOldSharedPreferences.getBoolean("premiumV1", false)) true
        else mSharedPreferences.getBoolean(Level.LEVEL_3.tag, false)
    }

    fun isUserLevel(level: Level): Boolean {
        return mSharedPreferences.getBoolean(level.tag, false)
    }

    fun setUserLevel(level: Level) {
        mSharedPreferences.edit().putBoolean(level.tag, true).apply()
    }

    fun clearUserLevel() {
        mSharedPreferences.edit()
                .remove("FREE")
                .remove("premiumV1")
                .remove("removeAds")
                .remove("LEVEL_1")
                .remove("LEVEL_2")
                .remove("LEVEL_3")
                .apply()
    }

    suspend fun getIAPs() : List<IAPItem> {
        return suspendCoroutine {
            val items: ArrayList<IAPItem> = ArrayList()

            val skuList = ArrayList<String>()
            skuList.add("level_1")
            skuList.add("level_2")
            skuList.add("level_3")
            val params = SkuDetailsParams.newBuilder().setSkusList(skuList).setType(BillingClient.SkuType.INAPP)
            billingClient.querySkuDetailsAsync(params.build()) { responseCode, skuDetailsList ->
                if (skuDetailsList == null) return@querySkuDetailsAsync
                for (item in skuDetailsList) {
                    items.add(IAPItem(item.sku, item.title, item.description, item.price))
                }
                it.resume(items)
            }
        }
    }

    fun handlePurchase(purchase: Purchase) {
        if (purchase.sku == "remove_ads") {
            mOldSharedPreferences.edit().putBoolean("removeAds", true).apply()
        }
        when (purchase.sku) {
            "remove_ads" -> {
                setUserLevel(Level.LEVEL_1)
            }
            "plus_v1" -> {
                setUserLevel(Level.LEVEL_3)
            }
            "level_1" -> {
                setUserLevel(Level.LEVEL_1)
            }
            "level_2" -> {
                setUserLevel(Level.LEVEL_2)
            }
            "level_3" -> {
                setUserLevel(Level.LEVEL_3)
            }
        }
    }

    fun onDestroy() {
        if (this::billingClient.isInitialized && billingClient.isReady) {
            billingClient.endConnection()
        }
    }

    fun launchBuyFlow(activity: Activity, params: BillingFlowParams, listener: (() -> Unit)) {
        val responseCode = billingClient.launchBillingFlow(activity, params)
    }

    fun getLevelText(level: Level): String {
        return when (level.tag) {
            "FREE" -> {
                "Free"
            }
            "LEVEL_1" -> {
                "Level 1"
            }
            "LEVEL_2" -> {
                "Level 2"
            }
            "LEVEL_3" -> {
                "Level 3"
            }
            else -> "Free"
        }
    }

    enum class Level (val tag: String) {
            FREE("FREE"),
            LEVEL_1("LEVEL_1"),
            LEVEL_2("LEVEL_2"),
            LEVEL_3("LEVEL_3")
    }

    data class IAPItem(
            val sku: String,
            val title: String,
            val description: String,
            val price: String
    )
}