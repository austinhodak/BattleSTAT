package com.respondingio.battlegroundsbuddy.stats

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import com.github.paolorotolo.appintro.AppIntro2
import com.github.paolorotolo.appintro.AppIntroFragment
import com.github.paolorotolo.appintro.model.SliderPage
import com.respondingio.battlegroundsbuddy.R

class StatsOnboarding : AppIntro2() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sliderPage = SliderPage()
        sliderPage.title = "Player Stats (Beta)"
        sliderPage.description = resources.getString(R.string.statsOnBoarding)
        sliderPage.bgColor = Color.parseColor("#e65100")

        addSlide(AppIntroFragment.newInstance(sliderPage))
    }

    override fun onSkipPressed(currentFragment: Fragment) {
        super.onSkipPressed(currentFragment)
        // Do something when users tap on Skip button.
    }

    override fun onDonePressed(currentFragment: Fragment) {
        super.onDonePressed(currentFragment)
        // Do something when users tap on Done button.
        val sharedPreferences = getSharedPreferences("com.respondingio.battlegroundsbuddy", Context.MODE_PRIVATE)
        sharedPreferences.edit().putBoolean("firstStatsLaunch", false).apply()

        startActivity(Intent(this, MainStatsActivity::class.java))
    }

    override fun onSlideChanged(oldFragment: Fragment?, newFragment: Fragment?) {
        super.onSlideChanged(oldFragment, newFragment)
    }
}