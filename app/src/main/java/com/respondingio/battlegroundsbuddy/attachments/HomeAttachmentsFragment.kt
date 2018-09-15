package com.respondingio.battlegroundsbuddy.attachments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.respondingio.battlegroundsbuddy.R.layout
import kotlinx.android.synthetic.main.fragment_home_weapons.tabs
import kotlinx.android.synthetic.main.fragment_home_weapons.viewpager

class HomeAttachmentsFragment : Fragment() {
    internal inner class ViewPagerAdapter(manager: FragmentManager) : FragmentPagerAdapter(manager) {

        private val title = arrayOf("Muzzle", "Upper Rail", "Lower Rail", "Magazine", "Stock")

        override fun getCount(): Int {
            return title.size
        }

        override fun getItem(position: Int): Fragment {
            val currentFragment: Fragment?
            val bundle = Bundle()
            currentFragment = HomeAttachmentsList()
            bundle.putInt("pos", position)
            currentFragment.arguments = bundle
            return currentFragment
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return title[position]
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(layout.fragment_home_weapons, container, false)
    }

    override fun onStart() {
        super.onStart()
        viewpager?.adapter = ViewPagerAdapter(childFragmentManager)
        tabs?.setupWithViewPager(viewpager)
    }
}
