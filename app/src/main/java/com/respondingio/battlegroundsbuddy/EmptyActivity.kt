package com.respondingio.battlegroundsbuddy

import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity

public class EmptyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)

        setContentView(R.layout.empty_layout)
    }
}