package com.respondingio.battlegroundsbuddy.stats


import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.os.IBinder
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import com.afollestad.materialdialogs.MaterialDialog
import com.android.vending.billing.IInAppBillingService
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.reward.RewardItem
import com.google.android.gms.ads.reward.RewardedVideoAd
import com.google.android.gms.ads.reward.RewardedVideoAdListener
import com.google.android.gms.tasks.Task
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.FirebaseFunctionsException
import com.google.firebase.functions.FirebaseFunctionsException.Code
import com.respondingio.battlegroundsbuddy.R
import com.respondingio.battlegroundsbuddy.models.PlayerStats
import com.respondingio.battlegroundsbuddy.models.PrefPlayer
import de.mateware.snacky.Snacky
import kotlinx.android.synthetic.main.fragment_your_stats.stats_assists
import kotlinx.android.synthetic.main.fragment_your_stats.stats_boosts
import kotlinx.android.synthetic.main.fragment_your_stats.stats_damageDealt
import kotlinx.android.synthetic.main.fragment_your_stats.stats_dbnos
import kotlinx.android.synthetic.main.fragment_your_stats.stats_deaths
import kotlinx.android.synthetic.main.fragment_your_stats.stats_headshots
import kotlinx.android.synthetic.main.fragment_your_stats.stats_heals
import kotlinx.android.synthetic.main.fragment_your_stats.stats_kd
import kotlinx.android.synthetic.main.fragment_your_stats.stats_killPoints
import kotlinx.android.synthetic.main.fragment_your_stats.stats_killStreak
import kotlinx.android.synthetic.main.fragment_your_stats.stats_kills
import kotlinx.android.synthetic.main.fragment_your_stats.stats_longestKill
import kotlinx.android.synthetic.main.fragment_your_stats.stats_longestSurv
import kotlinx.android.synthetic.main.fragment_your_stats.stats_losses
import kotlinx.android.synthetic.main.fragment_your_stats.stats_mostkills
import kotlinx.android.synthetic.main.fragment_your_stats.stats_refresh
import kotlinx.android.synthetic.main.fragment_your_stats.stats_revives
import kotlinx.android.synthetic.main.fragment_your_stats.stats_rideDist
import kotlinx.android.synthetic.main.fragment_your_stats.stats_roadKills
import kotlinx.android.synthetic.main.fragment_your_stats.stats_roundsplayed
import kotlinx.android.synthetic.main.fragment_your_stats.stats_suicides
import kotlinx.android.synthetic.main.fragment_your_stats.stats_teamKills
import kotlinx.android.synthetic.main.fragment_your_stats.stats_time
import kotlinx.android.synthetic.main.fragment_your_stats.stats_top10s
import kotlinx.android.synthetic.main.fragment_your_stats.stats_updated
import kotlinx.android.synthetic.main.fragment_your_stats.stats_vehicleDestroy
import kotlinx.android.synthetic.main.fragment_your_stats.stats_walkDist
import kotlinx.android.synthetic.main.fragment_your_stats.stats_weaponsAqd
import kotlinx.android.synthetic.main.fragment_your_stats.stats_weeklyPoints
import kotlinx.android.synthetic.main.fragment_your_stats.stats_winPoints
import kotlinx.android.synthetic.main.fragment_your_stats.stats_winloss
import kotlinx.android.synthetic.main.fragment_your_stats.stats_wins
import org.jetbrains.anko.support.v4.onRefresh
import org.jetbrains.anko.textColor
import java.util.HashMap

class MainStatsFragment : Fragment(), RewardedVideoAdListener {


    private var playerName: String? = null
    private var playerID: String? = null
    private var shardID: String? = null
    private var gameMode: String? = null
    private var seasonID: String? = null

    private var mainView: View? = null

    internal var mBundle = arguments

    private var mDatabase: DatabaseReference? = null

    private var mFunctions: FirebaseFunctions? = null

    private var iap: IInAppBillingService? = null

    private var isOutdated = false

    private var TAG = MainStatsFragment::class.java.simpleName

    private var lastUpdated: Long? = null

    private var updateTimeout: Long = 15

    private var isPremium: Boolean = false

    lateinit var previousPlayer: PlayerStats

    private lateinit var mRewardedVideoAd: RewardedVideoAd

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        mainView = inflater.inflate(R.layout.fragment_your_stats, container, false)
        //mainView?.visibility = View.INVISIBLE

        return mainView
    }

    private var mSharedPreferences: SharedPreferences? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mDatabase = FirebaseDatabase.getInstance().reference
        mFunctions = FirebaseFunctions.getInstance()
        mSharedPreferences = activity!!.getSharedPreferences("com.respondingio.battlegroundsbuddy", Context.MODE_PRIVATE)

        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(activity)
        mRewardedVideoAd.rewardedVideoAdListener = this

        val serviceIntent = Intent("com.android.vending.billing.InAppBillingService.BIND")
        serviceIntent.`package` = "com.android.vending"
        requireActivity().bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)

        isPremium = mSharedPreferences!!.getBoolean("premiumV1", false)

        if (mSharedPreferences!!.getBoolean("premiumV1", false)) {
            updateTimeout = 2
        }

        var player: PrefPlayer = arguments!!.getSerializable("player") as PrefPlayer

        playerName = player.playerName
        playerID = player.playerID
        shardID = player.selectedShardID
        gameMode = player.selectedGamemode
        seasonID = player.selectedSeason

        if (arguments!!.containsKey("previousPlayer")) {
            previousPlayer = arguments!!.getSerializable("previousPlayer") as PlayerStats
            setPreviousStats(previousPlayer)
        }

        if (playerID != null && shardID != null && seasonID != null)
            loadPlayerStats()

        Log.d("STATS", "$playerID - $shardID - $seasonID")

        val refreshLayout = mainView?.findViewById<SwipeRefreshLayout>(R.id.stats_refresh)

        refreshLayout?.setColorSchemeResources(R.color.md_orange_500, R.color.md_pink_500)
        refreshLayout?.onRefresh {
            if (playerID != null && shardID != null && gameMode != null && seasonID != null) {
                if (getTimeSinceLastUpdated() / 60 > updateTimeout) {
                    refreshLayout.isRefreshing = true
                    pullNewPlayerStats()
                } else if (!isPremium) {
                    mRewardedVideoAd.loadAd("ca-app-pub-1946691221734928/1941699809",
                            AdRequest.Builder().build())
                    refreshLayout.isRefreshing = false
                    Snacky.builder().setActivity(activity!!).setBackgroundColor(Color.parseColor("#3F51B5")).setText("You can refresh once every $updateTimeout minutes.")
                            .setActionText("UPGRADE OR WATCH AN AD").setDuration(5000).setActionTextColor(Color.WHITE).setActionClickListener {
                                MaterialDialog.Builder(requireActivity())
                                        .title("Upgrade or Watch An Ad")
                                        .content(R.string.upgrade)
                                        .backgroundColorRes(R.color.md_orange_700)
                                        .titleColorRes(R.color.md_white_1000)
                                        .contentColorRes(R.color.md_white_1000)
                                        .positiveColorRes(R.color.md_white_1000)
                                        .neutralColorRes(R.color.md_white_1000)
                                        .negativeColorRes(R.color.md_white_1000)
                                        .positiveText("GO PREMIUM ($2.99)")
                                        .neutralText("NEVERMIND")
                                        .negativeText("WATCH AN AD")
                                        .onNegative { dialog, which ->
                                            if (mRewardedVideoAd.isLoaded) {
                                                mRewardedVideoAd.show()
                                            } else {
                                                Snacky.builder().setActivity(requireActivity()).info().setText("No ad loaded, please try again.").show()
                                            }
                                        }
                                        .onNeutral { dialog, which ->
                                            dialog.dismiss()
                                        }
                                        .onPositive { dialog, which ->
                                            val buyIntentBundle = iap?.getBuyIntent(3, requireActivity().packageName,
                                                    "plus_v1", "inapp", "")

                                            val pendingIntent = buyIntentBundle?.getParcelable<PendingIntent>("BUY_INTENT")

                                            if (pendingIntent != null) {

                                                val REQUEST_CODE = 1002

                                                requireActivity().startIntentSenderForResult(pendingIntent.intentSender,
                                                        REQUEST_CODE, Intent(), 0, 0,
                                                        0)
                                            }
                                        }.show()
                            }.build().show()
                } else if (isPremium) {
                    mRewardedVideoAd.loadAd("ca-app-pub-3940256099942544/5224354917",
                            AdRequest.Builder().build())

                    refreshLayout.isRefreshing = false
                    Snacky.builder().setActivity(activity!!).setBackgroundColor(Color.parseColor("#3F51B5")).setText("You can refresh once every $updateTimeout minutes with premium. This is a PUBG API limit.").setActionClickListener {
                        if (mRewardedVideoAd.isLoaded) {
                            mRewardedVideoAd.show()
                        } else {
                            Snacky.builder().setActivity(requireActivity()).info().setText("No ad loaded, please try again.").show()
                        }
                    }.setActionText("WATCH AN AD").setDuration(5000).setActionTextColor(Color.WHITE).build().show()
                }
            } else {
                Snacky.builder().setActivity(activity!!).warning().setText("Must Select Gamemode and Season before refreshing.").show()
                refreshLayout.isRefreshing = false
            }
        }
    }

    private fun loadPlayerStats() {
        mDatabase?.child("user_stats/$playerID/season_data/$shardID/$seasonID")?.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(data: DataSnapshot) {
                if (data.exists()) {
                    if (data.hasChild("stats/$gameMode")) {
                        updateStats(data.child("stats/$gameMode").getValue(PlayerStats::class.java)!!)

                        stats_refresh?.isRefreshing = false
                    }

                    lastUpdated = data.child("lastUpdated").value as Long?

                    val relTime = DateUtils
                            .getRelativeTimeSpanString(lastUpdated!! * 1000L,
                                    System.currentTimeMillis(),
                                    DateUtils.MINUTE_IN_MILLIS)

                    stats_updated?.text = "Updated: $relTime"

                    val activity = activity as MainStatsActivity?

                    Log.d("LASTUPDATED", getTimeSinceLastUpdated().toString())

                    if (getTimeSinceLastUpdated() / 60 > updateTimeout) {
                        //activity?.showOutdated()
                    } else {
                        //activity?.hideOutdated(
                    }
                } else {
                    Log.w(TAG, "Stats not available in database, run function. - $playerID")
                    stats_refresh?.isRefreshing = true

                    pullNewPlayerStats()
                }
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    lateinit var currentPlayer: PlayerStats

    @SuppressLint("SetTextI18n")
    private fun updateStats(playerStats: PlayerStats) {
        currentPlayer = playerStats
        if (this::previousPlayer.isInitialized) {
            val animator = ValueAnimator.ofInt(previousPlayer.kills, playerStats.kills)
            animator.duration = 500
            animator.interpolator = AccelerateDecelerateInterpolator()
            animator.addUpdateListener {
                stats_kills?.text = it.animatedValue.toString()
            }
            animator.start()
        } else {
            stats_kills?.text = playerStats.kills.toString()
        }

        if (this::previousPlayer.isInitialized) {
            val animator = ValueAnimator.ofInt(previousPlayer.damageDealt.toInt(), playerStats.damageDealt.toInt())
            animator.duration = 500
            animator.interpolator = AccelerateDecelerateInterpolator()
            animator.addUpdateListener {
                stats_damageDealt?.text = it.animatedValue.toString()
            }
            animator.start()
        } else {
            stats_damageDealt?.text = playerStats.damageDealt.toInt().toString()
        }

        stats_assists?.text = playerStats.assists.toString()
        stats_boosts?.text = playerStats.boosts.toString()
        stats_dbnos?.text = playerStats.dBNOs.toString()
        stats_damageDealt?.text = Math.round(playerStats.damageDealt).toString()
        stats_headshots?.text = playerStats.headshotKills.toString()
        stats_heals?.text = playerStats.heals.toString()
        stats_killPoints?.text = String.format("%.0f", Math.rint(playerStats.killPoints))
        stats_longestKill?.text = "${String.format("%.0f", Math.rint(playerStats.longestKill))}m"
        stats_longestSurv?.text = "${String.format("%.0f", Math.ceil(playerStats.longestTimeSurvived / 60))} Min"
        stats_losses?.text = playerStats.losses.toString()
        stats_killStreak?.text = playerStats.maxKillStreaks.toString()
        stats_revives?.text = playerStats.revives.toString()
        stats_rideDist?.text = "${String.format("%.0f", Math.ceil(playerStats.rideDistance))}m"
        stats_roadKills?.text = playerStats.roadKills.toString()
        stats_mostkills?.text = playerStats.roundMostKills.toString()
        stats_roundsplayed?.text = playerStats.roundsPlayed.toString()
        stats_suicides?.text = playerStats.suicides.toString()
        stats_teamKills?.text = playerStats.teamKills.toString()
        stats_time?.text = "${String.format("%.0f", Math.ceil(playerStats.timeSurvived / 60))} Min"
        stats_top10s?.text = playerStats.top10s.toString()
        stats_vehicleDestroy?.text = playerStats.vehicleDestroys.toString()
        stats_walkDist?.text = "${String.format("%.0f", Math.ceil(playerStats.walkDistance))}m"
        stats_weaponsAqd?.text = playerStats.weaponsAcquired.toString()
        stats_weeklyPoints?.text = playerStats.weeklyKills.toString()
        stats_winPoints?.text = String.format("%.0f", Math.rint(playerStats.winPoints))
        stats_wins?.text = playerStats.wins.toString()
        stats_deaths?.text = playerStats.losses.toString()

        if (playerStats.kills > 0 && playerStats.losses > 0) {
            stats_kd?.text = String.format("%.2f", playerStats.kills.toDouble() / playerStats.losses.toDouble())
        } else {
            stats_kd?.text = "0"
        }

        if (playerStats.wins > 0 && playerStats.losses > 0) {
            stats_winloss?.text = String.format("%.2f", playerStats.wins.toDouble() / playerStats.losses.toDouble())
        } else {
            stats_winloss?.text = "0"
        }

        when {
            playerStats.kills.toDouble() / playerStats.losses.toDouble() >= 2.00 -> stats_kd?.textColor = resources.getColor(R.color.md_green_A700)
            playerStats.kills.toDouble() / playerStats.losses.toDouble() >= 1.00 -> stats_kd?.textColor = resources.getColor(R.color.md_orange_A400)
            stats_kd?.text != "0" -> stats_kd?.textColor = resources.getColor(R.color.md_red_A400)
        }

        mainView?.visibility = View.VISIBLE
    }

    fun setPreviousStats(playerStats: PlayerStats) {
        stats_assists?.text = playerStats.assists.toString()
        stats_boosts?.text = playerStats.boosts.toString()
        stats_dbnos?.text = playerStats.dBNOs.toString()
        stats_damageDealt?.text = Math.round(playerStats.damageDealt).toString()
        stats_headshots?.text = playerStats.headshotKills.toString()
        stats_heals?.text = playerStats.heals.toString()
        stats_killPoints?.text = String.format("%.0f", Math.rint(playerStats.killPoints))
        stats_longestKill?.text = "${String.format("%.0f", Math.rint(playerStats.longestKill))}m"
        stats_longestSurv?.text = "${String.format("%.0f", Math.ceil(playerStats.longestTimeSurvived / 60))} Min"
        stats_losses?.text = playerStats.losses.toString()
        stats_killStreak?.text = playerStats.maxKillStreaks.toString()
        stats_revives?.text = playerStats.revives.toString()
        stats_rideDist?.text = "${String.format("%.0f", Math.ceil(playerStats.rideDistance))}m"
        stats_roadKills?.text = playerStats.roadKills.toString()
        stats_mostkills?.text = playerStats.roundMostKills.toString()
        stats_roundsplayed?.text = playerStats.roundsPlayed.toString()
        stats_suicides?.text = playerStats.suicides.toString()
        stats_teamKills?.text = playerStats.teamKills.toString()
        stats_time?.text = "${String.format("%.0f", Math.ceil(playerStats.timeSurvived / 60))} Min"
        stats_top10s?.text = playerStats.top10s.toString()
        stats_vehicleDestroy?.text = playerStats.vehicleDestroys.toString()
        stats_walkDist?.text = "${String.format("%.0f", Math.ceil(playerStats.walkDistance))}m"
        stats_weaponsAqd?.text = playerStats.weaponsAcquired.toString()
        stats_weeklyPoints?.text = playerStats.weeklyKills.toString()
        stats_winPoints?.text = String.format("%.0f", Math.rint(playerStats.winPoints))
        stats_wins?.text = playerStats.wins.toString()
        stats_deaths?.text = playerStats.losses.toString()

        if (playerStats.kills > 0 && playerStats.losses > 0) {
            stats_kd?.text = String.format("%.2f", playerStats.kills.toDouble() / playerStats.losses.toDouble())
        } else {
            stats_kd?.text = "0"
        }

        if (playerStats.wins > 0 && playerStats.losses > 0) {
            stats_winloss?.text = String.format("%.2f", playerStats.wins.toDouble() / playerStats.losses.toDouble())
        } else {
            stats_winloss?.text = "0"
        }

        when {
            playerStats.kills.toDouble() / playerStats.losses.toDouble() >= 2.00 -> stats_kd?.textColor = resources.getColor(R.color.md_green_A700)
            playerStats.kills.toDouble() / playerStats.losses.toDouble() >= 1.00 -> stats_kd?.textColor = resources.getColor(R.color.md_orange_A400)
            stats_kd?.text != "0" -> stats_kd?.textColor = resources.getColor(R.color.md_red_A400)
        }
    }

    fun getTimeSinceLastUpdated(): Long {
        if (lastUpdated == null) return 0
        return Math.abs(lastUpdated?.minus((System.currentTimeMillis() / 1000))!!)
    }

    private fun pullNewPlayerStats() {
        startPlayerStatsFunction(playerID.toString(), shardID.toString(), seasonID.toString())?.addOnCompleteListener {
            if (!it.isSuccessful) {
                val e = it.exception
                if (e is FirebaseFunctionsException) {
                    val code = e.code

                    Log.e("PullNewStats", "onComplete: " + e.message)

                    if (code == Code.NOT_FOUND) {
                        Snacky.builder().setActivity(activity).info().setText(e.message.toString()).setDuration(
                                Snacky.LENGTH_LONG).show()
                    } else if (code == Code.RESOURCE_EXHAUSTED) {
                        Snacky.builder().setActivity(activity).error().setText("API limit reached, try again in a minute.").setDuration(
                                Snacky.LENGTH_LONG).show()
                    } else {
                        Snacky.builder().setActivity(activity).error().setText("Unknown error.").setDuration(
                                Snacky.LENGTH_LONG).show()
                    }
                }

                stats_refresh.isRefreshing = false
                return@addOnCompleteListener
            }

            Log.d(TAG, "Successfully refreshed player stats.")
        }
    }

    private fun startPlayerStatsFunction(playerID: String, shardID: String, seasonID: String): Task<Map<String, Any>>? {
        val data = HashMap<String, Any>()
        data["playerID"] = playerID
        data["shardID"] = shardID
        data["seasonID"] = seasonID

        return mFunctions?.getHttpsCallable("loadPlayerStats")?.call(data)?.continueWith { task ->
            val result = task.result.data as Map<String, Any>
            Log.d("REQUEST", result.toString())
            result
        }
    }

    override fun onRewardedVideoAdClosed() {
        if (hasReward) {
            Snacky.builder().setActivity(requireActivity()).success().setText("Refresh Rewarded. Thank you!").show()
            stats_refresh.isRefreshing = true
            pullNewPlayerStats()
            hasReward = false
        }
    }

    override fun onRewardedVideoAdLeftApplication() {
    }

    override fun onRewardedVideoAdLoaded() {
    }

    override fun onRewardedVideoAdOpened() {
    }

    override fun onRewardedVideoCompleted() {

    }

    private var hasReward: Boolean = false

    override fun onRewarded(p0: RewardItem?) {
        hasReward = true
        val bundle = Bundle()
        bundle.putString("refresh_type", "ad_reward")
        FirebaseAnalytics.getInstance(requireActivity()).logEvent("stat_refresh", bundle)
    }

    override fun onRewardedVideoStarted() {
    }

    override fun onRewardedVideoAdFailedToLoad(p0: Int) {
    }

    override fun onPause() {
        super.onPause()
        mRewardedVideoAd.pause(activity)
    }

    override fun onResume() {
        super.onResume()
        mRewardedVideoAd.resume(activity)
    }

    override fun onDestroy() {
        super.onDestroy()
        mRewardedVideoAd.destroy(activity)
    }

    private var serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(p0: ComponentName?) {
            iap = null
        }

        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            iap = IInAppBillingService.Stub.asInterface(p1)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1002) {
            val responseCode = data?.getIntExtra("RESPONSE_CODE", 0)
            val purchaseData = data?.getStringExtra("INAPP_PURCHASE_DATA")
            val dataSignature = data?.getStringExtra("INAPP_DATA_SIGNATURE")

            if (resultCode == Activity.RESULT_OK) {
                val m2SharedPreferences = requireActivity().getSharedPreferences("com.austinhodak.pubgcenter", Context.MODE_PRIVATE)

                mSharedPreferences?.edit()?.putBoolean("premiumV1", true)?.apply()
                m2SharedPreferences.edit().putBoolean("removeAds", true).apply()
                Snacky.builder().setActivity(requireActivity()).info().setText("Thanks! Enjoy the new stuff!").show()
            }
        }
    }
}
