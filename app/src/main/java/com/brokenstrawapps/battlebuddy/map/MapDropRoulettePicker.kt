package com.brokenstrawapps.battlebuddy.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.brokenstrawapps.battlebuddy.R
import kotlinx.android.synthetic.main.fragment_map_drop_picker.*

class MapDropRoulettePicker : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_map_drop_picker, container, false)

        val erangel = view.findViewById<ImageView>(R.id.erangel_image)
        Glide.with(this)
                .load(R.drawable.erangel_shadow)
                .into(erangel)

        val miramar = view.findViewById<ImageView>(R.id.miramar_image)
        Glide.with(this)
                .load(R.drawable.miramar_shadow)
                .into(miramar)

        val sanhok = view.findViewById<ImageView>(R.id.sanhok_image)
        Glide.with(this)
                .load(R.drawable.sanhok_new)
                .into(sanhok)

        val vikendi = view.findViewById<ImageView>(R.id.vikendi_image)
        Glide.with(this)
                .load(R.drawable.vikendi_shadow)
                .into(vikendi)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapToolbar?.setNavigationIcon(R.drawable.ic_arrow_back_24dp)
        mapToolbar?.setNavigationOnClickListener {
            activity?.onBackPressed()
        }

        selectErangel?.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("mapName", "erangel")
            Navigation.findNavController(requireActivity(), R.id.weaponDetailNavHost).navigate(R.id.mapSelected, bundle)
            activity?.title = "Erangel"
        }
        selectMiramar?.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("mapName", "miramar")
            Navigation.findNavController(requireActivity(), R.id.weaponDetailNavHost).navigate(R.id.mapSelected, bundle)
            activity?.title = "Miramar"
        }
        selectSanhok?.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("mapName", "sanhok")
            Navigation.findNavController(requireActivity(), R.id.weaponDetailNavHost).navigate(R.id.mapSelected, bundle)
            activity?.title = "Sanhok"
        }
        selectVikendi?.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("mapName", "vikendi")
            Navigation.findNavController(requireActivity(), R.id.weaponDetailNavHost).navigate(R.id.mapSelected, bundle)
            activity?.title = "Vikendi"
        }
    }
}