package com.respondingio.battlegroundsbuddy.stats


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.respondingio.battlegroundsbuddy.R

class MoreFragment : Fragment() {

    private var mainView: View? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        mainView = inflater.inflate(R.layout.fragment_more, container, false)

        return mainView
    }


}
