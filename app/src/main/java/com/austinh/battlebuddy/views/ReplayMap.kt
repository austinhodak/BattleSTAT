package com.austinh.battlebuddy.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import com.austinh.battlebuddy.R
import com.austinh.battlebuddy.models.LogCharacter
import com.austinh.battlebuddy.models.LogPlayerPosition
import com.austinh.battlebuddy.models.SafeZoneCircle
import com.austinh.battlebuddy.stats.matchdetails.replay.ReplayDraw
import com.austinh.battlebuddy.stats.matchdetails.replay.ReplaySettings
import com.austinh.battlebuddy.viewmodels.models.MatchModel
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import org.jetbrains.anko.toast
import kotlin.math.roundToInt

class ReplayMap constructor(context: Context, attr: AttributeSet? = null) : SubsamplingScaleImageView(context, attr) {

    lateinit var match: MatchModel
    var replayDraw = ReplayDraw()

    init {
        setPanLimit(SubsamplingScaleImageView.PAN_LIMIT_INSIDE)
        //this.setMinimumScaleType(SCALE_TYPE_CENTER_CROP)
    }

    private val paint = Paint()

    private var drawCircle: SafeZoneCircle? = null

    var offset: Float = 1.0f

    var settings: ReplaySettings? = null

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if (!isReady) {
            return
        }

        val scaleX: Float = if (this.scale > 2.00) {
            2F
        } else {
            this.scale
        }

        Log.d("ZOOM", "SCALE: $scale - ZOOM: $scaleX")

        paint.reset()
        paint.isAntiAlias = true

        if (settings?.showCarePackages == true)
            for (care in match.carePackageList) {
                if (care.itemPackage.elapsedTime <= settings?.elapsedSeconds ?: 0) {
                    val vPin = sourceToViewCoord(care.itemPackage.location.x.toFloat() / 100f / offset.toFloat(), care.itemPackage.location.y.toFloat() / 100f / offset.toFloat())!!
                    vPin.x -= (72f * scaleX) / 2
                    vPin.y -= (68f * scaleX) / 2
                    canvas?.drawBitmap(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(resources, R.drawable.care_package), (72f * scaleX).roundToInt(), (68f * scaleX).roundToInt(), false), vPin.x, vPin.y, paint)
                }
            }

        if (drawCircle != null) {
            val circlePaint = Paint()
            circlePaint.style = Paint.Style.STROKE
            circlePaint.color = Color.WHITE
            circlePaint.strokeWidth = 4f

            val cirlcePin = sourceToViewCoord(drawCircle!!.position.x.toFloat() / 100f / offset.toFloat(), drawCircle!!.position.y.toFloat() / 100f / offset.toFloat())!!
            canvas?.drawCircle(cirlcePin.x, cirlcePin.y, drawCircle!!.radius.toFloat() / 100f / offset.toFloat() * this.scale, circlePaint)
        }

        if (replayDraw.redZoneCircle != null) {
            val circlePaint = Paint()
            circlePaint.style = Paint.Style.FILL
            circlePaint.color = Color.parseColor("#80F44336")
            circlePaint.strokeWidth = 4f

            val cirlcePin = sourceToViewCoord(replayDraw.redZoneCircle!!.position.x.toFloat() / 100f / offset.toFloat(), replayDraw.redZoneCircle!!.position.y.toFloat() / 100f / offset.toFloat())!!
            canvas?.drawCircle(cirlcePin.x, cirlcePin.y, replayDraw.redZoneCircle!!.radius.toFloat() / 100f / offset.toFloat() * this.scale, circlePaint)
        }

        if (replayDraw.blueZoneCircle != null) {
            val circlePaint = Paint()
            circlePaint.style = Paint.Style.STROKE
            circlePaint.color = Color.BLUE
            circlePaint.strokeWidth = 4f

            val cirlcePin = sourceToViewCoord(replayDraw.blueZoneCircle!!.position.x.toFloat() / 100f / offset.toFloat(), replayDraw.blueZoneCircle!!.position.y.toFloat() / 100f / offset.toFloat())!!
            canvas?.drawCircle(cirlcePin.x, cirlcePin.y, replayDraw.blueZoneCircle!!.radius.toFloat() / 100f / offset.toFloat() * this.scale, circlePaint)
        }

        if (replayDraw.players != null) {
            for (player in replayDraw.players!!) {
                val circlePaint = Paint()
                circlePaint.style = Paint.Style.FILL
                circlePaint.color = match.teamColors[player.value.character.teamId] ?: 0
                //circlePaint.setShadowLayer(20  * scaleX, player.value.character.location.x.toFloat() / 100f / offset.toFloat(), player.value.character.location.y.toFloat() / 100f / offset.toFloat(), Color.BLACK)
                circlePaint.isAntiAlias = true

                val cirlcePin = sourceToViewCoord(player.value.character.location.x.toFloat() / 100f / offset.toFloat(), player.value.character.location.y.toFloat() / 100f / offset.toFloat())!!
                canvas?.drawCircle(cirlcePin.x, cirlcePin.y, 16 * scaleX, circlePaint)

                circlePaint.reset()
                circlePaint.style = Paint.Style.STROKE
                circlePaint.strokeWidth = 3 * scaleX
                circlePaint.isAntiAlias = true
                circlePaint.color = Color.BLACK

                canvas?.drawCircle(cirlcePin.x, cirlcePin.y, 15 * scaleX, circlePaint)

                circlePaint.reset()
                circlePaint.style = Paint.Style.STROKE
                circlePaint.strokeWidth = 3 * scaleX
                circlePaint.color = when {
                    player.value.character.health >= 80 -> Color.WHITE
                    player.value.character.health >= 50 -> resources.getColor(R.color.md_orange_500)
                    player.value.character.health >= 25 -> Color.RED
                    else -> Color.WHITE
                }
                circlePaint.isAntiAlias = true

                canvas?.drawArc(cirlcePin.x - (30 * scaleX) / 2, cirlcePin.y - (30 * scaleX) / 2, cirlcePin.x + (30 * scaleX) / 2, cirlcePin.y + (30 * scaleX) / 2, 270f, (3.6 * player.value.character.health).toFloat(), false, circlePaint)
            }
        }
    }

    fun updateMap(settings: ReplaySettings) {
        this.settings = settings

        val gameState = match.gameStates.findLast { it.gameState.elapsedTime <= settings.elapsedSeconds }
                ?: return

        if (settings.circleSettings.showRedZones && gameState.gameState.redZonePosition.isValidCirclePosition() && gameState.gameState.redZoneRadius > 0) {
            replayDraw.redZoneCircle = gameState.getRedzoneCircle()
            invalidate()
        } else {
            replayDraw.redZoneCircle = null
            invalidate()
        }

        if (settings.circleSettings.showCircle && gameState.common.isGame >= 1.0) {
            if (gameState.gameState.poisonGasWarningPosition.isValidCirclePosition() && gameState.gameState.poisonGasWarningRadius > 0) {
                drawCircle = SafeZoneCircle(
                        gameState.gameState.poisonGasWarningPosition,
                        gameState.gameState.poisonGasWarningRadius
                )
                invalidate()
            } else {
                drawCircle = null
                invalidate()
            }

            replayDraw.blueZoneCircle = SafeZoneCircle(
                    gameState.gameState.safetyZonePosition,
                    gameState.gameState.safetyZoneRadius
            )
            invalidate()
        } else {
            replayDraw.blueZoneCircle = null
            invalidate()
        }

        replayDraw.players = HashMap()

        match.logPlayerPositions.filter { it.elapsedTime <= settings.elapsedSeconds && it.elapsedTime >= settings.elapsedSeconds - 10 }.forEach {
            (replayDraw.players as HashMap<String, LogPlayerPosition>)[it.character.accountId] = it
        }
    }

    fun zoomToPlayer(logCharacter: LogCharacter, mContext: Context) {
        if (settings == null || replayDraw.players == null) return
        val player = replayDraw.players!![logCharacter.accountId]
        //val player = match.logPlayerPositions.find { it.character.accountId == logCharacter.accountId && it.elapsedTime <= settings!!.elapsedSeconds && it.elapsedTime >= settings!!.elapsedSeconds - 10 }
        if (player == null) {
            mContext.toast("Player not on map.")
            return
        }

        val victimPoint = PointF(player.character.location.x.toFloat() / 100f / offset, player.character.location.y.toFloat() / 100f / offset)

        Log.d("POINT", "$victimPoint -- ${player.character.location} -- $sWidth -- $sHeight")

        setScaleAndCenter(5f, PointF(victimPoint.x, victimPoint.y))
    }
}