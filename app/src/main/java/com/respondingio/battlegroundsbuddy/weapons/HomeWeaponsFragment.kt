package com.respondingio.battlegroundsbuddy.weapons

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.respondingio.battlegroundsbuddy.R
import kotlinx.android.synthetic.main.fragment_home_weapons.tabs
import kotlinx.android.synthetic.main.fragment_home_weapons.viewpager
import kotlinx.android.synthetic.main.fragment_home_weapons.weaponListAd
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

    private var mSharedPreferences: SharedPreferences? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        mSharedPreferences = requireActivity().getSharedPreferences("com.austinhodak.pubgcenter", MODE_PRIVATE)
        return inflater.inflate(R.layout.fragment_home_weapons, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupTabs()
        if (mSharedPreferences != null && !mSharedPreferences!!.getBoolean("removeAds", false)) {
            weaponListAd?.adListener = object: AdListener() {
                override fun onAdFailedToLoad(p0: Int) {
                    super.onAdFailedToLoad(p0)
                    weaponListAd?.visibility = View.GONE
                }

                override fun onAdLoaded() {
                    super.onAdLoaded()
                    weaponListAd?.visibility = View.VISIBLE
                }
            }

            weaponListAd?.loadAd(AdRequest.Builder().build())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        weaponListAd?.destroy()
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
