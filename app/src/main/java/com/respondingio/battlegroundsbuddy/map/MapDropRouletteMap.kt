package com.respondingio.battlegroundsbuddy.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.respondingio.battlegroundsbuddy.R
import kotlinx.android.synthetic.main.map_drop_roulette_map.*
import org.jetbrains.anko.backgroundColor

class MapDropRouletteMap : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.map_drop_roulette_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        when (arguments?.getString("mapName")) {
            "erangel" -> {
                dropMap?.setImageDrawable(resources.getDrawable(R.drawable.erangel))
                view.backgroundColor = resources.getColor(R.color.md_blue_900)
            }
        }
    }
}