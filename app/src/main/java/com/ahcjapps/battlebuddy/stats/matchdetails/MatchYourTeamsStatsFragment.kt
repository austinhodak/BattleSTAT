package com.ahcjapps.battlebuddy.stats.matchdetails

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import butterknife.ButterKnife
import com.google.android.material.tabs.TabLayout
import com.ahcjapps.battlebuddy.R
import com.ahcjapps.battlebuddy.viewmodels.MatchDetailViewModel
import com.ahcjapps.battlebuddy.viewmodels.models.MatchModel

class MatchYourTeamsStatsFragment : Fragment() {

    private val viewModel: MatchDetailViewModel by lazy {
        ViewModelProviders.of(requireActivity()).get(MatchDetailViewModel::class.java)
    }

    lateinit var match: MatchModel

    internal inner class ViewPagerAdapter(manager: FragmentManager, match: MatchModel) : FragmentStatePagerAdapter(manager) {

        override fun getCount(): Int {
            return titles.size
        }

        override fun getItem(position: Int): Fragment {
            val bundle = Bundle()
            val playerID: String = titles[ArrayList(titles.keys)[position]].toString()
            bundle.putSerializable("playerID", playerID)
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

    private var tabLayout: TabLayout? = null

    private var viewPager: ViewPager? = null

    private lateinit var mActivity: MatchDetailActivity

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.fragment_team_stats_tabs, container, false)

        ButterKnife.bind(this, view)

        mActivity = activity as MatchDetailActivity
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.mMatchData.observe(requireActivity(), Observer { match ->
            for (par in match.currentPlayerRoster?.relationships?.participants?.data!!) {
                titles[match.participantHash[par.id]?.attributes?.stats?.name!!] = par.id
            }
            this.match = match
            setupAdapter(match)
        })
    }

    private fun setupAdapter(match: MatchModel) {
        viewPager = view?.findViewById<View>(R.id.viewpager) as ViewPager
        val adapter = ViewPagerAdapter(childFragmentManager, match)
        viewPager!!.adapter = adapter

        tabLayout = view?.findViewById<View>(R.id.tabs) as TabLayout
        tabLayout!!.setupWithViewPager(viewPager)
    }
}