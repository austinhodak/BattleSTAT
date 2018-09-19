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
import kotlinx.android.synthetic.main.home_weapons_list.pg
import kotlinx.android.synthetic.main.home_weapons_list.weapon_list_rv
import net.idik.lib.slimadapter.SlimAdapter
import net.idik.lib.slimadapter.SlimInjector
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.util.ArrayList


class VehiclesFragment : Fragment() {

    private var adapter: SlimAdapter? = null

    private val mList = ArrayList<Vehicle>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(layout.home_weapons_list, container, false)
    }

    override fun onStart() {
        super.onStart()
        pg?.visibility = View.VISIBLE

        setupAdapter()

        mList.add(Vehicle().setVehicleName("Buggy").setVehicleType("Land").setOccupants("2 ••").setSpeed("100 Km/H").setHealth("~1540").setImage(R.drawable.buggy))
        mList.add(Vehicle().setVehicleName("UAZ (Open Top)").setVehicleType("Land").setOccupants("4 ••••").setSpeed("130 Km/H").setHealth("~1820").setImage(R.drawable.uaz_car))
        mList.add(Vehicle().setVehicleName("UAZ (Closed Top)").setVehicleType("Land").setOccupants("4 ••••").setSpeed("130 Km/H").setHealth("~1820").setImage(R.drawable.uaz_cloth))
        mList.add(Vehicle().setVehicleName("Motorcycle (w/Sidecar)").setVehicleType("Land").setOccupants("3 •••").setSpeed("130 Km/H").setHealth("~1025").setImage(R.drawable.motorcycle_car))
        mList.add(Vehicle().setVehicleName("Motorcycle").setVehicleType("Land").setOccupants("2 ••").setSpeed("152 Km/H").setHealth("~1025").setImage(R.drawable.motorcycle))
        mList.add(Vehicle().setVehicleName("Dacia 1300").setVehicleType("Land").setOccupants("4 ••••").setSpeed("139 Km/H").setHealth("~1820").setImage(R.drawable.dacia))
        mList.add(Vehicle().setVehicleName("Van").setVehicleType("Land").setOccupants("6 ••••••").setSpeed("- Km/H").setHealth("-").setImage(R.drawable.van))
        mList.add(Vehicle().setVehicleName("Pickup").setVehicleType("Land").setOccupants("4 ••••").setSpeed("- Km/H").setHealth("-").setImage(R.drawable.truck))
        mList.add(Vehicle().setVehicleName("PG-117").setVehicleType("Water").setOccupants("5 •••••").setSpeed("90 Km/H").setHealth("~1520").setImage(R.drawable.boat))
        mList.add(Vehicle().setVehicleName("Aquarail").setVehicleType("Water").setOccupants("2 ••").setSpeed("90 Km/H").setHealth("-").setImage(R.drawable.aquarail))
        mList.add(Vehicle().setVehicleName("Tukshai").setVehicleType("Land").setOccupants("3 •••").setSpeed("- Km/H").setHealth("-").setImage(R.drawable.tuktuk))
        mList.add(Vehicle().setVehicleName("Scooter").setVehicleType("Land").setOccupants("2 ••").setSpeed("- Km/H").setHealth("~1025").setImage(R.drawable.scooter))
        mList.add(Vehicle().setVehicleName("Mirado").setVehicleType("Land").setOccupants("4 ••••").setSpeed("152 Km/H").setHealth("-").setImage(R.drawable.mirado))
        mList.add(Vehicle().setVehicleName("Rony").setVehicleType("Land").setOccupants("4 ••••").setSpeed("- Km/H").setHealth("-").setImage(R.drawable.rony))

        doAsync {
            mList.sortWith(Comparator { vehicle, t1 -> vehicle.vehicleName.compareTo(t1.vehicleName, ignoreCase = true) })
            adapter?.updateData(mList)

            uiThread { pg?.visibility = View.GONE }
        }
    }

    private fun setupAdapter() {
        val linearLayoutManager = LinearLayoutManager(activity ?: return)
        weapon_list_rv.layoutManager = linearLayoutManager

        adapter = SlimAdapter.create().attachTo(weapon_list_rv).register(R.layout.fragment_vehicle_item, SlimInjector<Vehicle> { data, injector ->
            Glide.with(this).load(data.image).into(injector.findViewById(R.id.vehicle_image) as ImageView)
            injector.text(R.id.weapon_title, data.vehicleName)
            injector.text(R.id.weapon_subtitle, data.vehicleType)
            injector.text(R.id.weapon_range, data.occupants)
            injector.text(R.id.weapon_body_dmg, data.speed)
            injector.text(R.id.weapon_head_dmg, data.health)
        })
    }
}
