package com.austinh.battlebuddy.premium

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.austinh.battlebuddy.MainActivityKT
import com.austinh.battlebuddy.R
import com.austinh.battlebuddy.utils.Premium
import kotlinx.android.synthetic.main.activity_upgrade.*
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class UpgradeActivity : AppCompatActivity() {

    private lateinit var billingClient: BillingClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upgrade)

        setSupportActionBar(upgradeToolbar)
        title = "Upgrade"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        billingClient = BillingClient.newBuilder(this).setListener { responseCode, purchases ->
            if (responseCode == BillingClient.BillingResponse.OK && purchases != null) {
                for (purchase in purchases) {
                    Premium.handlePurchase(purchase)

                    when (purchase.sku) {
                        "level_1" -> {
                            level1Buy?.text = "ALREADY PURCHASED"
                            level1Buy?.isEnabled = false
                        }
                        "level_2" -> {
                            level2Buy?.text = "ALREADY PURCHASED"
                            level2Buy?.isEnabled = false
                        }
                        "level_3" -> {
                            level3Buy?.text = "ALREADY PURCHASED"
                            level3Buy?.isEnabled = false
                        }
                    }
                }

                val intent = Intent(applicationContext, MainActivityKT::class.java)
                val mPendingIntentId = 1001
                val mPendingIntent = PendingIntent.getActivity(applicationContext, mPendingIntentId, intent, PendingIntent.FLAG_CANCEL_CURRENT)
                val mgr = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent)
                System.exit(0)
                finish()
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
                        Premium.handlePurchase(purchase)

                        when (purchase.sku) {
                            "level_1" -> {
                                level1Buy?.text = "ALREADY PURCHASED"
                                level1Buy?.isEnabled = false
                            }
                            "level_2" -> {
                                level2Buy?.text = "ALREADY PURCHASED"
                                level2Buy?.isEnabled = false
                            }
                            "level_3" -> {
                                level3Buy?.text = "ALREADY PURCHASED"
                                level3Buy?.isEnabled = false
                            }
                        }
                    }
                }
            }
        })


        level1Buy?.setOnClickListener {
            val flowParams = BillingFlowParams.newBuilder()
                    .setSku("level_1")
                    .setType(BillingClient.SkuType.INAPP)
                    .build()

            billingClient.launchBillingFlow(this@UpgradeActivity, flowParams)
        }

        level2Buy?.setOnClickListener {
            val flowParams = BillingFlowParams.newBuilder()
                    .setSku("level_2")
                    .setType(BillingClient.SkuType.INAPP)
                    .build()

            billingClient.launchBillingFlow(this@UpgradeActivity, flowParams)
        }

        level3Buy?.setOnClickListener {
            val flowParams = BillingFlowParams.newBuilder()
                    .setSku("level_3")
                    .setType(BillingClient.SkuType.INAPP)
                    .build()

            billingClient.launchBillingFlow(this@UpgradeActivity, flowParams)
        }



        GlobalScope.launch(Dispatchers.Main, CoroutineStart.DEFAULT) {
            for (item in Premium.getIAPs()) {
                Log.d("UPGRADE", "PRICE: ${item.price}")
                when (item.sku) {
                    "level_1" -> level1Price?.text = item.price
                    "level_2" -> level2Price?.text = item.price
                    "level_3" -> level3Price?.text = item.price
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }
}