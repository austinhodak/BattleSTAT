package com.respondingio.battlegroundsbuddy.rss


import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.respondingio.battlegroundsbuddy.MainActivity
import com.respondingio.battlegroundsbuddy.R
import kotlinx.android.synthetic.main.activity_main.toolbar_title

/**
 * A simple [Fragment] subclass.
 *
 */
class HomeUpdatesFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_home_fragment_bottom, container, false)
        // Inflate the layout for this fragment
        view.findViewById<BottomNavigationView>(R.id.bottom_navigation).setOnNavigationItemSelectedListener { menuitem: MenuItem ->
            when (menuitem.itemId) {
                R.id.pc_menu -> {
                    val homeFragment2 = WindowsUpdatesFragment()
                    activity!!.supportFragmentManager.beginTransaction().replace(R.id.home_frame, homeFragment2)
                            .commit()

                    (activity as MainActivity).toolbar_title.text = "PC Feed"
                }
                R.id.xbox_menu -> {
                    val homeFragment2 = XboxRSS()
                    activity!!.supportFragmentManager.beginTransaction().replace(R.id.home_frame, homeFragment2)
                            .commit()

                    (activity as MainActivity).toolbar_title.text = "Xbox Feed"
                }
                R.id.mobile_menu -> {
                    val homeFragment2 = MobileRSS()
                    activity!!.supportFragmentManager.beginTransaction().replace(R.id.home_frame, homeFragment2)
                            .commit()

                    (activity as MainActivity).toolbar_title.text = "Mobile Feed"
                }
            }
            true
        }

        (activity as MainActivity).toolbar_title.text = "PC Feed"

        view.findViewById<BottomNavigationView>(R.id.bottom_navigation).selectedItemId = R.id.pc_menu
        val homeFragment2 = WindowsUpdatesFragment()
        activity!!.supportFragmentManager.beginTransaction().replace(R.id.home_frame, homeFragment2)
                .commit()

        return view
    }
}
