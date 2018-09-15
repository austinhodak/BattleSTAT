package com.respondingio.battlegroundsbuddy.weapons

import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.respondingio.battlegroundsbuddy.R
import com.respondingio.battlegroundsbuddy.R.layout
import kotlinx.android.synthetic.main.fragment_home_weapons.tabs
import kotlinx.android.synthetic.main.fragment_home_weapons.viewpager
import java.util.ArrayList
import java.util.Arrays

class HomeWeaponsFragment : Fragment() {

    private val mStringArray = ArrayList<String>()

    private val title = arrayOf("Assault Rifles", "Sniper Rifles", "SMGs", "Shotguns", "Pistols", "LMGs", "Throwables", "Melee", "Misc")

    internal inner class ViewPagerAdapter

    (manager: FragmentManager) : FragmentPagerAdapter(manager) {

        override fun getCount(): Int {
            return mStringArray.size
        }

        override fun getItem(position: Int): Fragment {
            val currentFragment: Fragment
            val bundle = Bundle()
            currentFragment = MainWeaponsList()

            if (tabs?.tabCount == 10 && position == 0) {
                bundle.putBoolean("isFavoriteTab", true)
            } else if (tabs?.tabCount == 10) {
                bundle.putInt("pos", position - 1)
            } else {
                bundle.putInt("pos", position)
            }

            currentFragment.arguments = bundle
            return currentFragment
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return mStringArray[position]
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(layout.fragment_home_weapons, container, false)
    }

    override fun onStart() {
        super.onStart()
        setupTabs()
    }

    private fun setupTabs() {
        mStringArray.addAll(Arrays.asList(*title))

        val adapter = ViewPagerAdapter(childFragmentManager)
        viewpager?.adapter = adapter

        tabs?.setupWithViewPager(viewpager)

        if (activity != null) {
            val sharedPreferences = requireActivity().getSharedPreferences("com.austinhodak.pubgcenter", MODE_PRIVATE)
            val favs = sharedPreferences.getStringSet("favoriteWeapons", null)
            if (favs != null && !favs.isEmpty()) {
                tabs?.addTab(tabs?.newTab()?.setCustomView(R.layout.tab_fav)!!, 0)

                tabs?.setScrollPosition(1, 0f, true)
                viewpager.currentItem = 1

                val emoji = 0x2B50

                mStringArray.add(0, String(Character.toChars(emoji)))
                adapter.notifyDataSetChanged()
            }
        }
    }
}
