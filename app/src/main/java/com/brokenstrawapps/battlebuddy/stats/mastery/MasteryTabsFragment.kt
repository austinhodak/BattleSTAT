package com.brokenstrawapps.battlebuddy.stats.mastery

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.brokenstrawapps.battlebuddy.R
import com.brokenstrawapps.battlebuddy.models.PlayerListModel
import com.brokenstrawapps.battlebuddy.utils.Premium
import com.brokenstrawapps.battlebuddy.utils.Weapons
import com.brokenstrawapps.battlebuddy.weapons.MainWeaponsList
import com.google.android.gms.ads.AdListener
import kotlinx.android.synthetic.main.fragment_home_weapons.*
import java.util.*

class MasteryTabsFragment : Fragment() {

    private val mStringArray = ArrayList<Weapons.CATEGORY>()

    internal inner class ViewPagerAdapter

    (manager: FragmentManager) : FragmentPagerAdapter(manager) {

        val player = arguments!!.getSerializable("selectedPlayer") as PlayerListModel

        override fun getCount(): Int {
            return mStringArray.size
        }

        override fun getItem(position: Int): Fragment {
            val currentFragment: Fragment
            val bundle = Bundle()
            currentFragment = MasteryWeaponListFragment()

            bundle.putInt("pos", position)
            bundle.putSerializable("category", mStringArray[position])
            bundle.putSerializable("player", player)
            /*if (tabs?.tabCount == 10 && position == 0) {
                bundle.putBoolean("isFavoriteTab", true)
            } else if (tabs?.tabCount == 10) {
                bundle.putInt("pos", position - 1)
            } else {
                bundle.putInt("pos", position)
            }
*/
            currentFragment.arguments = bundle
            return currentFragment
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return mStringArray[position].title
        }
    }

    private var mSharedPreferences: SharedPreferences? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        mSharedPreferences = requireActivity().getSharedPreferences("com.brokenstrawapps.battlebuddy", Context.MODE_PRIVATE)
        return inflater.inflate(R.layout.fragment_mastery_tabs, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupTabs()

        if (!Premium.isAdFreeUser()) {
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
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        weaponListAd?.destroy()
    }

    private fun setupTabs() {

        val list = Weapons.CATEGORY.values()

        val title = arrayOf(
                getString(R.string.assault_rifles),
                getString(R.string.sniper_rifles),
                "DMRs",
                "SMGs",
                getString(R.string.shotguns),
                getString(R.string.pistols),
                "LMGs",
                getString(R.string.throwables),
                getString(R.string.melee), "Misc"
        )

        mStringArray.addAll(list)

        val adapter = ViewPagerAdapter(childFragmentManager)
        viewpager?.adapter = adapter

        tabs?.setupWithViewPager(viewpager)

        /*if (activity != null) {
            val sharedPreferences = requireActivity().getSharedPreferences("com.brokenstrawapps.battlebuddy", Context.MODE_PRIVATE)
            val favs = sharedPreferences.getStringSet("favoriteWeapons", null)
            if (favs != null && !favs.isEmpty()) {
                tabs?.addTab(tabs?.newTab()?.setCustomView(R.layout.tab_fav)!!, 0)

                tabs?.setScrollPosition(1, 0f, true)
                viewpager.currentItem = 1

                val emoji = 0x2B50

                mStringArray.add(0, String(Character.toChars(emoji)))
                adapter.notifyDataSetChanged()
            }
        }*/
    }
}
