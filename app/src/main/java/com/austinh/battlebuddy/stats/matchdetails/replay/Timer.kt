package com.austinh.battlebuddy.stats.matchdetails.replay

import android.os.Handler
import android.util.Log
import android.widget.TextView
import java.text.DecimalFormat

class Timer {

    lateinit var textView: TextView
    var start = System.currentTimeMillis()
    var current = System.currentTimeMillis()
    var elapsedTime: Long = 0
    var started = false
    var paused = false
    var logEnabled = true
    var clockDelay: Long = 33
    var handler = Handler()

    private val runnable: Runnable = Runnable { run() }


    private var onTickListener: OnTickListener? = null

    /**
     * Set an OnTickListener to listen for clock changes.
     *
     * @param onTickListener a reference to the interface implementation.
     * @since 1.0
     */

    fun setOnTickListener(onTickListener: OnTickListener) {
        this.onTickListener = onTickListener
    }

    interface OnTickListener {
        /**
         * Called every time the clock 'ticks'. The stopwatch ticks after a delay of 100ms (or as specified).
         * @since 1.2
         * @param stopwatch Reference to the currently calling stopwatch.
         */
        fun onTick(stopwatch: Timer)
    }

    /**
     * Formats time in either of the following formats depending on time passed - SS.ss, MM:SS.ss, HH:MM:SS.
     *
     * @param elapsedTime time in milliseconds which has to be formatted
     * @return formatted time in String form
     * @since 1.1
     */
    internal fun getFormattedTime(elapsedTime: Long): String {
        val displayTime = StringBuilder()

        val milliseconds = (elapsedTime % 1000 / 10).toInt()
        val seconds = (elapsedTime / 1000 % 60).toInt()
        val minutes = (elapsedTime / (60 * 1000) % 60).toInt()
        val hours = (elapsedTime / (60 * 60 * 1000)).toInt()

        val f = DecimalFormat("00")

        when {
            minutes == 0 -> displayTime.append(f.format(minutes.toLong())).append(":").append(f.format(seconds.toLong())).append('.').append(f.format(milliseconds.toLong()))
            hours == 0 -> displayTime.append(f.format(minutes.toLong())).append(":").append(f.format(seconds.toLong())).append('.').append(f.format(milliseconds.toLong()))
            else -> displayTime.append(hours).append(":").append(f.format(minutes.toLong())).append(":").append(f.format(seconds.toLong()))
        }

        return displayTime.toString()
    }

    /**
     * Starts the stopwatch at the current time. Cannot be called again without calling stop() first.
     *
     * @throws IllegalStateException if the stopwatch has already been started.
     * @see .stop
     * @see .isStarted
     * @since 1.0
     */
    fun start() {
        if (started)
            throw IllegalStateException("Already Started")
        else {
            started = true
            paused = false
            start = System.currentTimeMillis()
            current = System.currentTimeMillis()
            elapsedTime = 0

            handler.post(runnable)
        }
    }

    /**
     * Stops the stopwatch. Stopwatch cannot be resumed from current time later.
     *
     * @throws IllegalStateException if stopwatch has not been started yet.
     * @see .start
     * @see .isStarted
     * @since 1.0
     */
    fun stop() {
        if (!started)
            throw IllegalStateException("Not Started")
        else {
            updateElapsed(System.currentTimeMillis())
            started = false
            paused = false
            handler.removeCallbacks(runnable)
        }
    }

    /**
     * Pauses the stopwatch. Using this allows you to resume the stopwatch from the current state.
     *
     * @throws IllegalStateException if stopwatch is already paused or not started yet.
     * @see .resume
     * @see .isPaused
     * @since 1.0
     */
    fun pause() {
        if (paused)
            throw IllegalStateException("Already Paused")
        else if (!started)
            throw IllegalStateException("Not Started")
        else {
            updateElapsed(System.currentTimeMillis())
            paused = true
            handler.removeCallbacks(runnable)
        }
    }

    /**
     * Used to resume the stopwatch from the current time after being paused.
     *
     * @throws IllegalStateException if stopwatch is not paused or not started yet.
     * @see .pause
     * @see .isPaused
     * @since 1.0
     */
    fun resume() {
        if (!paused)
            throw IllegalStateException("Not Paused")
        else if (!started)
            throw IllegalStateException("Not Started")
        else {
            paused = false
            current = System.currentTimeMillis()
            handler.post(runnable)
        }
    }

    /**
     * Updates the time in elapsed and lap time and then updates the current time.
     *
     * @param time Current time in millis. Passing any other value may result in odd behaviour
     * @since 1.1
     */
    private fun updateElapsed(time: Long) {
        elapsedTime += time - current
        current = time
    }


    /**
     * The main thread responsible for updating and displaying the time
     *
     * @since 1.1
     */
    private fun run() {
        if (!started || paused) {
            handler.removeCallbacks(runnable)
            return
        }
        updateElapsed(System.currentTimeMillis())
        handler.postDelayed(runnable, clockDelay)

        if (logEnabled) {
            Log.d("STOPWATCH", (elapsedTime / 1000).toString() + " seconds, " + elapsedTime % 1000 + " milliseconds")
            Log.d("STOPWATCH", elapsedTime.toString() + " elapsed")
        }

        if (onTickListener != null)
            onTickListener?.onTick(this)

        if (textView != null) {
            val displayTime = getFormattedTime(elapsedTime)
            textView.text = displayTime
        }
    }
}