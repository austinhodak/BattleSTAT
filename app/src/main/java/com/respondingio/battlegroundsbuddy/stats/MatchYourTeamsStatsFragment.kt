package com.respondingio.battlegroundsbuddy.stats

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import butterknife.ButterKnife
import com.google.android.material.tabs.TabLayout
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.respondingio.battlegroundsbuddy.R

class MatchYourTeamsStatsFragment : Fragment() {

    internal inner class ViewPagerAdapter(manager: FragmentManager) : FragmentPagerAdapter(manager) {

        override fun getCount(): Int {
            return titles.size
        }

        override fun getItem(position: Int): Fragment {
            val bundle = Bundle()
            var playerID: String = titles[ArrayList(titles.keys)[position]].toString()
            Log.d("TABS", playerID)
            bundle.putSerializable("player", mActivity.participantHash[playerID])
            bundle.putSerializable("participantList", arguments?.getSerializable("participantList"))
            bundle.putSerializable("rosterList", arguments?.getSerializable("rosterList"))
            bundle.putSerializable("killList", arguments?.getSerializable("killList"))
            bundle.putString("matchCreatedAt", arguments?.getString("matchCreatedAt"))
            bundle.putBoolean("isTabs", true)
            val matchesPlayerStatsFragment = MatchPlayerStatsFragment()
            matchesPlayerStatsFragment.arguments = bundle
            return matchesPlayerStatsFragment
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return ArrayList(titles.keys)[position]
        }
    }

    private val titles = LinkedHashMap<String, String>()

    private val db = FirebaseFirestore.getInstance()

    private val storage = FirebaseStorage.getInstance()

    private var tabLayout: TabLayout? = null

    private var viewPager: ViewPager? = null

    private lateinit var mActivity: MatchDetailActivity

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.fragment_team_stats_tabs, container, false)

        ButterKnife.bind(this, view)

        mActivity = activity as MatchDetailActivity

        for (par in mActivity.currentPlayerRoster?.relationships?.participants?.data!!) {
            titles[mActivity.participantHash[par.id]?.attributes?.stats?.name!!] = par.id
        }

        viewPager = view.findViewById<View>(R.id.viewpager) as ViewPager
        val adapter = ViewPagerAdapter(childFragmentManager)
        viewPager!!.adapter = adapter

        tabLayout = view.findViewById<View>(R.id.tabs) as TabLayout
        tabLayout!!.setupWithViewPager(viewPager)

        return view
    }
}