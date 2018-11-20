package com.respondingio.battlegroundsbuddy.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.respondingio.battlegroundsbuddy.R
import kotlinx.android.synthetic.main.fragment_map_drop_picker.*

class MapDropRoulettePicker : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_map_drop_picker, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        selectErangel?.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("mapName", "erangel")
            Navigation.findNavController(requireActivity(), R.id.weaponDetailNavHost).navigate(R.id.mapSelected, bundle)
        }
    }
}