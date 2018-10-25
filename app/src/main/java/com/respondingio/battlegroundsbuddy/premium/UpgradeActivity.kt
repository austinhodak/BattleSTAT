package com.respondingio.battlegroundsbuddy.premium

import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.Purchase
import com.respondingio.battlegroundsbuddy.Premium
import com.respondingio.battlegroundsbuddy.R
import kotlinx.android.synthetic.main.activity_upgrade.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.sdk27.coroutines.onClick

class UpgradeActivity : AppCompatActivity() {

    lateinit private var billingClient: BillingClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upgrade)

        setSupportActionBar(upgradeToolbar)
        title = "Upgrade"
        upgradeToolbarWaterfall?.scrollView = upgradeScrollview
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

                setResult(Activity.RESULT_OK)
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


        level1Buy?.onClick {
            val flowParams = BillingFlowParams.newBuilder()
                    .setSku("level_1")
                    .setType(BillingClient.SkuType.INAPP)
                    .build()

            billingClient.launchBillingFlow(this@UpgradeActivity, flowParams)
        }

        level2Buy?.onClick {
            val flowParams = BillingFlowParams.newBuilder()
                    .setSku("level_2")
                    .setType(BillingClient.SkuType.INAPP)
                    .build()

            billingClient.launchBillingFlow(this@UpgradeActivity, flowParams)
        }

        level3Buy?.onClick {
            val flowParams = BillingFlowParams.newBuilder()
                    .setSku("level_3")
                    .setType(BillingClient.SkuType.INAPP)
                    .build()

            billingClient.launchBillingFlow(this@UpgradeActivity, flowParams)
        }

        launch(UI) {
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