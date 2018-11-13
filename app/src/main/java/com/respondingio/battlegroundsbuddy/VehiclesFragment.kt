package com.respondingio.battlegroundsbuddy


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.respondingio.battlegroundsbuddy.R.layout
import kotlinx.android.synthetic.main.home_weapons_list.*
import net.idik.lib.slimadapter.SlimAdapter
import net.idik.lib.slimadapter.SlimInjector
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.util.*


class VehiclesFragment : Fragment() {

    private var adapter: SlimAdapter? = null

    private var mList: MutableList<Vehicle> = ArrayList<Vehicle>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(layout.home_weapons_list, container, false)
    }

    override fun onStart() {
        super.onStart()
        pg?.visibility = View.VISIBLE

        setupAdapter()

        mList.add(Vehicle("Buggy", "Land", "2 ••", "100 Km/H", "~1540", R.drawable.buggy))
        mList.add(Vehicle("UAZ (Open Top)", "Land", "4 ••••", "130 Km/H", "~1820", R.drawable.uaz_car))
        mList.add(Vehicle("UAZ (Closed Top)", "Land", "4 ••••", "130 Km/H", "~1820", R.drawable.uaz_cloth))
        mList.add(Vehicle("Motorcycle (w/Sidecar)", "Land", "3 •••", "130 Km/H", "~1025", R.drawable.motorcycle_car))
        mList.add(Vehicle("Motorcycle", "Land", "2 ••", "152 Km/H", "~1025", R.drawable.motorcycle))
        mList.add(Vehicle("Dacia 1300", "Land", "4 ••••", "139 Km/H", "~1820", R.drawable.dacia))
        mList.add(Vehicle("Van", "Land", "6 ••••••", "110 Km/H", "~1680", R.drawable.van))
        mList.add(Vehicle("Pickup", "Land", "4 ••••", "72 Km/H", "-", R.drawable.truck))
        mList.add(Vehicle("PG-117", "Water", "5 •••••", "90 Km/H", "~1520", R.drawable.boat))
        mList.add(Vehicle("Aquarail", "Water", "2 ••", "90 Km/H", "-", R.drawable.aquarail))
        mList.add(Vehicle("Tukshai", "Land", "3 •••", "- Km/H", "-", R.drawable.tuktuk))
        mList.add(Vehicle("Scooter", "Land", "2 ••", "- Km/H", "~1025", R.drawable.scooter))
        mList.add(Vehicle("Mirado", "Land", "4 ••••", "152 Km/H", "-", R.drawable.mirado))
        mList.add(Vehicle("Rony", "Land", "4 ••••", "- Km/H", "-", R.drawable.rony))

        doAsync {
            mList = mList.sortedWith(compareBy { it.name }).toMutableList()
            adapter?.updateData(mList)

            uiThread { pg?.visibility = View.GONE }
        }
    }

    override fun onStop() {
        super.onStop()
        mList.clear()
        weapon_list_rv.adapter = null
    }

    private fun setupAdapter() {
        val linearLayoutManager = LinearLayoutManager(activity ?: return)
        weapon_list_rv.layoutManager = linearLayoutManager

        adapter = SlimAdapter.create().attachTo(weapon_list_rv).register(R.layout.fragment_vehicle_item, SlimInjector<Vehicle> { data, injector ->
            Glide.with(this).load(data.image).into(injector.findViewById(R.id.vehicle_image) as ImageView)
            injector.text(R.id.weapon_title, data.name)
            injector.text(R.id.weapon_subtitle, data.type)
            injector.text(R.id.weapon_range, data.occupants)
            injector.text(R.id.weapon_body_dmg, data.speed)
            injector.text(R.id.weapon_head_dmg, data.health)
        })
    }

    data class Vehicle(
            var name: String,
            var type: String,
            var occupants: String,
            var speed: String = "- Km/H",
            var health: String = "-",
            var image: Int
    )
}
