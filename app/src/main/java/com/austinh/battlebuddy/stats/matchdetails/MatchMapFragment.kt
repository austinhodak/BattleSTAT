package com.austinh.battlebuddy.stats.matchdetails

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import co.zsmb.materialdrawerkt.builders.drawer
import co.zsmb.materialdrawerkt.draweritems.expandable.expandableItem
import co.zsmb.materialdrawerkt.draweritems.switchable.secondarySwitchItem
import co.zsmb.materialdrawerkt.draweritems.switchable.switchItem
import com.austinh.battlebuddy.R
import com.austinh.battlebuddy.viewmodels.MatchDetailViewModel
import com.austinh.battlebuddy.viewmodels.models.MatchModel
import com.mikepenz.materialdrawer.Drawer

class MatchMapFragment : Fragment() {

    lateinit var matchModel: MatchModel
    private var mRightDrawer: Drawer? = null

    private val viewModel: MatchDetailViewModel by lazy {
        ViewModelProviders.of(requireActivity()).get(MatchDetailViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewModel.mMatchData.observe(this, Observer { match ->
            matchModel = match
        })

        setHasOptionsMenu(true)

        return inflater.inflate(R.layout.maptile_activity, container, false)
    }

    override fun onResume() {
        super.onResume()

        mRightDrawer = drawer {
            //primaryDrawer = (activity as MatchDetailActivity).mDrawer
            gravity = Gravity.END
            selectedItem = -1
            expandableItem("Safe Zones") {
                icon = R.drawable.lightning
                selectable = false
                secondarySwitchItem("Phase 1") {
                    selectable = false
                }
                secondarySwitchItem("Phase 2") {
                    selectable = false
                }
                secondarySwitchItem("Phase 3") {
                    selectable = false
                }
                secondarySwitchItem("Phase 4") {
                    selectable = false
                }
                secondarySwitchItem("Phase 5") {
                    selectable = false
                }
                secondarySwitchItem("Phase 6") {
                    selectable = false
                }
                secondarySwitchItem("Phase 7") {
                    selectable = false
                }
                secondarySwitchItem("Phase 8") {
                    selectable = false
                }
            }

            switchItem("Show Red Zones") {
                icon = R.drawable.explosion_color
                selectable = false
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.match_map, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.match_map_drawer) {
            mRightDrawer?.openDrawer()
        }
        return super.onOptionsItemSelected(item)
    }
}