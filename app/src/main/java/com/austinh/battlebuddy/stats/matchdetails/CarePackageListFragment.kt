package com.austinh.battlebuddy.stats.matchdetails

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.austinh.battlebuddy.R
import com.austinh.battlebuddy.models.LogCarePackageSpawn
import com.austinh.battlebuddy.models.LogItem
import com.austinh.battlebuddy.utils.Telemetry
import com.austinh.battlebuddy.viewmodels.MatchDetailViewModel
import com.austinh.battlebuddy.viewmodels.models.MatchModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_stats_kill_feed.*
import net.idik.lib.slimadapter.SlimAdapter
import net.idik.lib.slimadapter.SlimInjector
import java.util.*

class CarePackageListFragment: Fragment() {

    private val viewModel: MatchDetailViewModel by lazy {
        ViewModelProviders.of(requireActivity()).get(MatchDetailViewModel::class.java)
    }

    private var killFeedList: List<LogCarePackageSpawn> = ArrayList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        //setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_stats_kill_feed, container, false)
    }

    private lateinit var mAdapter: SlimAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.mMatchData.observe(this, Observer { match ->
            setupAdapter(match)
        })
    }

    private fun setupAdapter(matchModel: MatchModel?) {

        killFeedList = matchModel!!.carePackageList.sortedBy { it._D }

        kill_feed_rv.layoutManager = LinearLayoutManager(activity)

        mAdapter  = SlimAdapter.create().attachTo(kill_feed_rv).register(R.layout.care_package_list_item, SlimInjector<LogCarePackageSpawn> { data, injector ->
            val itemList = injector.findViewById<RecyclerView>(R.id.care_package_item_list)
            itemList.layoutManager = LinearLayoutManager(activity)

            injector.text(R.id.carePackageTime, data.getTimeString(matchModel.attributes?.createdAt!!))
            injector.text(R.id.carePackageName, "Care Package #${killFeedList.indexOf(data) + 1}")

            SlimAdapter.create().attachTo(itemList).register(R.layout.care_package_content_item, SlimInjector<LogItem> { itemData, inj ->
                inj.text(R.id.packageItemQTY, "${itemData.stackCount}x")
                inj.text(R.id.packageItemName, Telemetry.getItemIds(requireContext())[itemData.itemId].toString())

                when (itemData.category) {
                    "Ammunition" -> inj.image(R.id.packageItemIcon, R.drawable.icons8_ammo)
                    "Attachment" -> inj.image(R.id.packageItemIcon, R.drawable.icons8_magazine)
                    "Weapon" -> inj.image(R.id.packageItemIcon, R.drawable.icons8_rifle)
                    "Equipment",
                    "Use" -> {
                        when (itemData.subCategory) {
                            "Backpack" -> inj.image(R.id.packageItemIcon, R.drawable.backpack)
                            "Boost",
                            "Heal" -> inj.image(R.id.packageItemIcon, R.drawable.med)
                            "Headgear" -> inj.image(R.id.packageItemIcon, R.drawable.icons8_helmet)
                            "Vest" -> inj.image(R.id.packageItemIcon, R.drawable.vest)
                        }
                    }
                }

                val gsReference = FirebaseStorage.getInstance().getReferenceFromUrl("gs://pubg-center.appspot.com/itemIdIcons/${itemData.itemId}.png")
                val factory = DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build()

                Glide.with(this)
                        .asDrawable()
                        .load(gsReference)
                        .apply(RequestOptions().override(100,100))
                        .transition(DrawableTransitionOptions.withCrossFade(factory))
                        .into(inj.findViewById(R.id.packageItemIcon))

            }).updateData(data.itemPackage.items)
        }).updateData(killFeedList)
    }

    override fun onDestroy() {
        super.onDestroy()
        kill_feed_rv?.adapter = null
        kill_feed_rv?.layoutManager = null
    }
}