package com.brokenstrawapps.battlebuddy.stats.compare

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.graphics.Typeface.*
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.brokenstrawapps.battlebuddy.R
import com.brokenstrawapps.battlebuddy.models.Gamemode
import com.brokenstrawapps.battlebuddy.models.PlayerListModel
import com.brokenstrawapps.battlebuddy.snacky.Snacky
import com.brokenstrawapps.battlebuddy.utils.Ranks
import com.brokenstrawapps.battlebuddy.viewmodels.models.PlayerModel
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.player_compare_main_stats.*
import net.idik.lib.slimadapter.SlimAdapter
import org.jetbrains.anko.appcompat.v7.navigationIconResource
import java.io.Serializable
import java.text.DecimalFormat
import kotlin.math.roundToInt

class ComparePlayersActivity : AppCompatActivity() {

    private lateinit var firstPlayer: ComparePlayerModel
    private lateinit var secondPlayer: ComparePlayerModel
    private val statsList: MutableList<StatItem> = ArrayList()

    private val formatter = DecimalFormat("#,###")

    private lateinit var firstAdapter: SlimAdapter
    private lateinit var distanceAdapter: SlimAdapter
    private lateinit var killsAdapter: SlimAdapter
    private lateinit var healingAdapter: SlimAdapter
    private lateinit var damageAdapter: SlimAdapter
    private lateinit var timesAdapter: SlimAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.fragment_fade_enter, R.anim.fragment_fade_exit)
        setContentView(R.layout.player_compare_main_stats)

        setSupportActionBar(toolbar)
        toolbar.navigationIconResource = R.drawable.ic_arrow_back_24dp
        toolbar.setNavigationOnClickListener { onBackPressed() }

        setupAdapters()

        if (intent != null && intent.hasExtra("firstPlayer") && intent.hasExtra("secondPlayer")) {
            firstPlayer = intent.getSerializableExtra("firstPlayer") as ComparePlayerModel
            secondPlayer = intent.getSerializableExtra("secondPlayer") as ComparePlayerModel
            
            updateStats(firstPlayer, secondPlayer, Gamemode.SOLO, false)
        } else Snacky.builder().setActivity(this).error().setText("Error loading players...").show()

        //val defaultGamemode = intent.getSerializableExtra("gamemode") as Gamemode

        compare_player_tabs?.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(p0: TabLayout.Tab?) {

            }

            override fun onTabUnselected(p0: TabLayout.Tab?) {

            }

            override fun onTabSelected(p0: TabLayout.Tab?) {
                when (p0?.position) {
                    0 -> updateGamemode(Gamemode.SOLO)
                    1 -> updateGamemode(Gamemode.SOLOFPP)
                    2 -> updateGamemode(Gamemode.DUO)
                    3 -> updateGamemode(Gamemode.DUOFPP)
                    4 -> updateGamemode(Gamemode.SQUAD)
                    5 -> updateGamemode(Gamemode.SQUADFPP)
                    else -> updateGamemode(Gamemode.SOLO, true)
                }
            }
        })
    }
    
    @SuppressLint("SetTextI18n")
    private fun updateStats(player1: ComparePlayerModel, player2: ComparePlayerModel, gamemode: Gamemode, overall: Boolean) {
        statsList.clear()

        player1Name?.text = player1.player.playerName
        player2Name?.text = player2.player.playerName

        var p1Stats = when (gamemode) {
            Gamemode.SOLO -> player1.stats.soloStats
            Gamemode.SOLOFPP -> player1.stats.soloFPPStats
            Gamemode.DUO -> player1.stats.duoStats
            Gamemode.DUOFPP -> player1.stats.duoFPPStats
            Gamemode.SQUAD -> player1.stats.squadStats
            Gamemode.SQUADFPP -> player1.stats.squadFPPStats
        }

        var p2Stats = when (gamemode) {
            Gamemode.SOLO -> player2.stats.soloStats
            Gamemode.SOLOFPP -> player2.stats.soloFPPStats
            Gamemode.DUO -> player2.stats.duoStats
            Gamemode.DUOFPP -> player2.stats.duoFPPStats
            Gamemode.SQUAD -> player2.stats.squadStats
            Gamemode.SQUADFPP -> player2.stats.squadFPPStats
        }

        if (overall) {
            p1Stats = player1.stats.getOverallStats()
            p2Stats = player2.stats.getOverallStats()

            player1Title?.text = player1.stats.getHighestRank().title.toUpperCase() + " " + player1.stats.getHighestRankLevel()
            player1TopLL?.backgroundTintList = ColorStateList.valueOf(resources.getColor(Ranks.getRankColor(player1.stats.getHighestRank())))
            player1Icon?.setImageResource(Ranks.getRankIcon(player1.stats.getHighestRank()))

            player2Title?.text = player2.stats.getHighestRank().title.toUpperCase() + " " + player2.stats.getHighestRankLevel()
            player2TopLL?.backgroundTintList = ColorStateList.valueOf(resources.getColor(Ranks.getRankColor(player2.stats.getHighestRank())))
            player2Icon?.setImageResource(Ranks.getRankIcon(player2.stats.getHighestRank()))

            statsList.add(StatItem(Sections.TOP, "Points", formatter.format(Math.floor(player1.stats.getHighestRankPoints()).toInt()), formatter.format(Math.floor(player2.stats.getHighestRankPoints()).toInt())))
            statsList.add(StatItem(Sections.TOP, "Best Points", formatter.format(Math.floor(player1.stats.getHighestBestRankPoints()).toInt()), formatter.format(Math.floor(player2.stats.getHighestBestRankPoints()).toInt())))
        } else {
            player1Title?.text = p1Stats.getRank().title.toUpperCase() + " " + p1Stats.getRankLevel(true)
            player1TopLL?.backgroundTintList = ColorStateList.valueOf(resources.getColor(Ranks.getRankColor(p1Stats.getRank())))
            player1Icon?.setImageResource(Ranks.getRankIcon(p1Stats.getRank()))

            player2Title?.text = p2Stats.getRank().title.toUpperCase() + " " + p2Stats.getRankLevel(true)
            player2TopLL?.backgroundTintList = ColorStateList.valueOf(resources.getColor(Ranks.getRankColor(p2Stats.getRank())))
            player2Icon?.setImageResource(Ranks.getRankIcon(p2Stats.getRank()))

            statsList.add(StatItem(Sections.TOP, "Points", formatter.format(Math.floor(p1Stats.rankPoints).toInt()), formatter.format(Math.floor(p2Stats.rankPoints).toInt())))
            statsList.add(StatItem(Sections.TOP, "Best Points", formatter.format(Math.floor(p1Stats.bestRankPoint).toInt()), formatter.format(Math.floor(p2Stats.bestRankPoint).toInt())))
        }

        statsList.add(StatItem(Sections.TOP, "Wins", p1Stats.wins, p2Stats.wins))
        statsList.add(StatItem(Sections.TOP, "Top 10s", p1Stats.top10s, p2Stats.top10s))
        statsList.add(StatItem(Sections.TOP, "Kills", p1Stats.kills, p2Stats.kills))
        statsList.add(StatItem(Sections.TOP, "Headshots", p1Stats.headshotKills, p2Stats.headshotKills))
        statsList.add(StatItem(Sections.TOP, "K/D", String.format("%.2f", p1Stats.kills.toDouble() / p1Stats.losses.toDouble()), String.format("%.2f", p2Stats.kills.toDouble() / p2Stats.losses.toDouble())))
        statsList.add(StatItem(Sections.TOP, "Win %", "${String.format("%.2f", (p1Stats.wins.toDouble() / p1Stats.roundsPlayed.toDouble()) * 100)}%", "${String.format("%.2f", (p2Stats.wins.toDouble() / p2Stats.roundsPlayed.toDouble()) * 100)}%"))
        statsList.add(StatItem(Sections.TOP, "Assists", p1Stats.assists, p2Stats.assists))
        statsList.add(StatItem(Sections.TOP, "Knocks", p1Stats.dBNOs, p2Stats.dBNOs))
        statsList.add(StatItem(Sections.TOP, "Rounds Played", p1Stats.roundsPlayed, p2Stats.roundsPlayed))
        statsList.add(StatItem(Sections.TOP, "Most Kills", p1Stats.roundMostKills, p2Stats.roundMostKills))
        statsList.add(StatItem(Sections.TOP, "Avg. Damage", String.format("%.0f", Math.ceil(p1Stats.damageDealt / p1Stats.roundsPlayed.toDouble())), String.format("%.0f", Math.ceil(p2Stats.damageDealt / p2Stats.roundsPlayed.toDouble()))))
        statsList.add(StatItem(Sections.TOP, "Headshot %", "${String.format("%.2f", (p1Stats.headshotKills.toDouble() / p1Stats.kills.toDouble()) * 100)}%", "${String.format("%.2f", (p2Stats.headshotKills.toDouble() / p2Stats.kills.toDouble()) * 100)}%"))

        firstAdapter.updateData(statsList.filter { it.placing == Sections.TOP })


        statsList.add(StatItem(Sections.DISTANCE, "Riding", "${String.format("%.0f", Math.ceil(p1Stats.rideDistance))}m", "${String.format("%.0f", Math.ceil(p2Stats.rideDistance))}m"))
        statsList.add(StatItem(Sections.DISTANCE, "Walking", "${String.format("%.0f", Math.ceil(p1Stats.walkDistance))}m", "${String.format("%.0f", Math.ceil(p2Stats.walkDistance))}m"))
        statsList.add(StatItem(Sections.DISTANCE, "Swimming", "${String.format("%.0f", Math.ceil(p1Stats.swimDistance))}m", "${String.format("%.0f", Math.ceil(p2Stats.swimDistance))}m"))

        distanceAdapter.updateData(statsList.filter { it.placing == Sections.DISTANCE })


        statsList.add(StatItem(Sections.KILLS, "Longest Kill", "${String.format("%.0f", Math.ceil(p1Stats.longestKill))}m", "${String.format("%.0f", Math.ceil(p2Stats.longestKill))}m"))
        statsList.add(StatItem(Sections.KILLS, "Road Kills", p1Stats.roadKills.toString(), p2Stats.roadKills.toString()))
        statsList.add(StatItem(Sections.KILLS, "Team Kills", p1Stats.teamKills.toString(), p2Stats.teamKills.toString()))

        killsAdapter.updateData(statsList.filter { it.placing == Sections.KILLS })


        statsList.add(StatItem(Sections.HEALING, "Boosts", p1Stats.boosts.toString(), p2Stats.boosts.toString()))
        statsList.add(StatItem(Sections.HEALING, "Heals", p1Stats.heals.toString(), p2Stats.heals.toString()))
        statsList.add(StatItem(Sections.HEALING, "Revives", p1Stats.revives.toString(), p2Stats.revives.toString()))

        healingAdapter.updateData(statsList.filter { it.placing == Sections.HEALING })


        statsList.add(StatItem(Sections.DAMAGE, "Damage Dealt", p1Stats.damageDealt.roundToInt().toString(), p2Stats.damageDealt.roundToInt().toString()))
        statsList.add(StatItem(Sections.DAMAGE, "Cars Destroyed", p1Stats.vehicleDestroys.toString(), p2Stats.vehicleDestroys.toString()))
        //statsList.add(StatItem(Sections.DAMAGE, "--", p1Stats.revives.toString(), p2Stats.revives.toString()))

        damageAdapter.updateData(statsList.filter { it.placing == Sections.DAMAGE })


        statsList.add(StatItem(Sections.TIME, "Longest", p1Stats.getLongTimeSurvived(), p2Stats.getLongTimeSurvived()))
        statsList.add(StatItem(Sections.TIME, "Total", p1Stats.getTotalTimeSurvived(), p2Stats.getTotalTimeSurvived()))

        timesAdapter.updateData(statsList.filter { it.placing == Sections.TIME })
    }

    private fun updateGamemode(gamemode: Gamemode, isOverall: Boolean = false) {
        updateStats(firstPlayer, secondPlayer, gamemode, isOverall)
    }

    private fun setupAdapters() {
        topCompareRV?.layoutManager = LinearLayoutManager(this)
        firstAdapter = SlimAdapter.create().updateData(statsList).register<StatItem>(R.layout.compare_stat_item) { data, injector ->
            injector.text(R.id.statName, data.statName)
            injector.text(R.id.statLeft, data.getPlayer1Stat().statText)
            injector.text(R.id.statRight, data.getPlayer2Stat().statText)

            injector.typeface(R.id.statLeft, data.getPlayer1Stat().typeface)
            injector.typeface(R.id.statRight, data.getPlayer2Stat().typeface)

            when {
                data.isPlayer1Better() == null -> {
                    injector.textColor(R.id.statLeft, resources.getColor(R.color.md_white_1000))
                    injector.textColor(R.id.statRight, resources.getColor(R.color.md_white_1000))
                }
                data.isPlayer1Better()!! -> {
                    injector.textColor(R.id.statLeft, resources.getColor(R.color.md_green_500))
                    injector.textColor(R.id.statRight, resources.getColor(R.color.md_white_1000))
                }
                else -> {
                    injector.textColor(R.id.statRight, resources.getColor(R.color.md_green_500))
                    injector.textColor(R.id.statLeft, resources.getColor(R.color.md_white_1000))
                }
            }
        }.attachTo(topCompareRV)

        distanceCompareRV?.layoutManager = LinearLayoutManager(this)
        distanceAdapter = SlimAdapter.create().updateData(statsList).register<StatItem>(R.layout.compare_stat_item) { data, injector ->
            injector.text(R.id.statName, data.statName)
            injector.text(R.id.statLeft, data.getPlayer1Stat().statText)
            injector.text(R.id.statRight, data.getPlayer2Stat().statText)

            injector.typeface(R.id.statLeft, data.getPlayer1Stat().typeface)
            injector.typeface(R.id.statRight, data.getPlayer2Stat().typeface)

            when {
                data.isPlayer1Better() == null -> {
                    injector.textColor(R.id.statLeft, resources.getColor(R.color.md_white_1000))
                    injector.textColor(R.id.statRight, resources.getColor(R.color.md_white_1000))
                }
                data.isPlayer1Better()!! -> {
                    injector.textColor(R.id.statLeft, resources.getColor(R.color.md_green_500))
                    injector.textColor(R.id.statRight, resources.getColor(R.color.md_white_1000))
                }
                else -> {
                    injector.textColor(R.id.statRight, resources.getColor(R.color.md_green_500))
                    injector.textColor(R.id.statLeft, resources.getColor(R.color.md_white_1000))
                }
            }
        }.attachTo(distanceCompareRV)

        killsCompareRV?.layoutManager = LinearLayoutManager(this)
        killsAdapter = SlimAdapter.create().updateData(statsList).register<StatItem>(R.layout.compare_stat_item) { data, injector ->
            injector.text(R.id.statName, data.statName)
            injector.text(R.id.statLeft, data.getPlayer1Stat().statText)
            injector.text(R.id.statRight, data.getPlayer2Stat().statText)

            injector.typeface(R.id.statLeft, data.getPlayer1Stat().typeface)
            injector.typeface(R.id.statRight, data.getPlayer2Stat().typeface)

            when {
                data.isPlayer1Better() == null -> {
                    injector.textColor(R.id.statLeft, resources.getColor(R.color.md_white_1000))
                    injector.textColor(R.id.statRight, resources.getColor(R.color.md_white_1000))
                }
                data.isPlayer1Better()!! -> {
                    injector.textColor(R.id.statLeft, resources.getColor(R.color.md_green_500))
                    injector.textColor(R.id.statRight, resources.getColor(R.color.md_white_1000))
                }
                else -> {
                    injector.textColor(R.id.statRight, resources.getColor(R.color.md_green_500))
                    injector.textColor(R.id.statLeft, resources.getColor(R.color.md_white_1000))
                }
            }
        }.attachTo(killsCompareRV)

        healingCompareRV?.layoutManager = LinearLayoutManager(this)
        healingAdapter = SlimAdapter.create().updateData(statsList).register<StatItem>(R.layout.compare_stat_item) { data, injector ->
            injector.text(R.id.statName, data.statName)
            injector.text(R.id.statLeft, data.getPlayer1Stat().statText)
            injector.text(R.id.statRight, data.getPlayer2Stat().statText)

            injector.typeface(R.id.statLeft, data.getPlayer1Stat().typeface)
            injector.typeface(R.id.statRight, data.getPlayer2Stat().typeface)

            when {
                data.isPlayer1Better() == null -> {
                    injector.textColor(R.id.statLeft, resources.getColor(R.color.md_white_1000))
                    injector.textColor(R.id.statRight, resources.getColor(R.color.md_white_1000))
                }
                data.isPlayer1Better()!! -> {
                    injector.textColor(R.id.statLeft, resources.getColor(R.color.md_green_500))
                    injector.textColor(R.id.statRight, resources.getColor(R.color.md_white_1000))
                }
                else -> {
                    injector.textColor(R.id.statRight, resources.getColor(R.color.md_green_500))
                    injector.textColor(R.id.statLeft, resources.getColor(R.color.md_white_1000))
                }
            }
        }.attachTo(healingCompareRV)

        damageCompareRV?.layoutManager = LinearLayoutManager(this)
        damageAdapter = SlimAdapter.create().updateData(statsList).register<StatItem>(R.layout.compare_stat_item) { data, injector ->
            injector.text(R.id.statName, data.statName)
            injector.text(R.id.statLeft, data.getPlayer1Stat().statText)
            injector.text(R.id.statRight, data.getPlayer2Stat().statText)

            injector.typeface(R.id.statLeft, data.getPlayer1Stat().typeface)
            injector.typeface(R.id.statRight, data.getPlayer2Stat().typeface)

            when {
                data.isPlayer1Better() == null -> {
                    injector.textColor(R.id.statLeft, resources.getColor(R.color.md_white_1000))
                    injector.textColor(R.id.statRight, resources.getColor(R.color.md_white_1000))
                }
                data.isPlayer1Better()!! -> {
                    injector.textColor(R.id.statLeft, resources.getColor(R.color.md_green_500))
                    injector.textColor(R.id.statRight, resources.getColor(R.color.md_white_1000))
                }
                else -> {
                    injector.textColor(R.id.statRight, resources.getColor(R.color.md_green_500))
                    injector.textColor(R.id.statLeft, resources.getColor(R.color.md_white_1000))
                }
            }
        }.attachTo(damageCompareRV)

        timesCompareRV?.layoutManager = LinearLayoutManager(this)
        timesAdapter = SlimAdapter.create().updateData(statsList).register<StatItem>(R.layout.compare_stat_item) { data, injector ->
            injector.text(R.id.statName, data.statName)
            injector.text(R.id.statLeft, data.getPlayer1Stat().statText)
            injector.text(R.id.statRight, data.getPlayer2Stat().statText)

            injector.typeface(R.id.statLeft, data.getPlayer1Stat().typeface)
            injector.typeface(R.id.statRight, data.getPlayer2Stat().typeface)

            when {
                data.isPlayer1Better() == null -> {
                    injector.textColor(R.id.statLeft, resources.getColor(R.color.md_white_1000))
                    injector.textColor(R.id.statRight, resources.getColor(R.color.md_white_1000))
                }
                data.isPlayer1Better()!! -> {
                    injector.textColor(R.id.statLeft, resources.getColor(R.color.md_green_500))
                    injector.textColor(R.id.statRight, resources.getColor(R.color.md_white_1000))
                }
                else -> {
                    injector.textColor(R.id.statRight, resources.getColor(R.color.md_green_500))
                    injector.textColor(R.id.statLeft, resources.getColor(R.color.md_white_1000))
                }
            }
        }.attachTo(timesCompareRV)
    }

    override fun onPause() {
        super.onPause()
        overridePendingTransition(R.anim.nav_default_pop_enter_anim, R.anim.nav_default_pop_exit_anim)
    }
}

data class ComparePlayerModel (val player: PlayerListModel, val stats: PlayerModel) : Serializable

data class StatItem (
        val placing: Sections,
        val statName: String,
        val player1Value: Any,
        val player2Value: Any
) {
    fun getPlayer1Stat(): Stat {
        val stat = Stat(statText = player1Value.toString())

        if (player1Value !is String && player2Value !is String) {
            //Is a number
            val p1 = player1Value.toString().toDouble()
            val p2 = player2Value.toString().toDouble()

            if (p1 > p2) stat.typeface = DEFAULT_BOLD
        }

        return stat
    }

    fun getPlayer2Stat(): Stat {
        val stat = Stat(statText = player2Value.toString())

        if (player1Value !is String && player2Value !is String) {
            //Is a number
            val p1 = player1Value.toString().toDouble()
            val p2 = player2Value.toString().toDouble()

            if (p2 > p1) stat.typeface = DEFAULT_BOLD
        }

        return stat
    }

    fun isPlayer1Better(): Boolean? {
        return null
        if (player1Value !is String && player2Value !is String) {
            //Is a number
            val p1 = player1Value.toString().toDouble()
            val p2 = player2Value.toString().toDouble()

            return p1 > p2
        }

        return null
    }

    data class Stat (var typeface: Typeface = DEFAULT, val statText: String)
}

enum class Sections {
    TOP,
    DISTANCE,
    KILLS,
    HEALING,
    DAMAGE,
    TIME
}