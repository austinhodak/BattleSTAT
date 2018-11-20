package com.respondingio.battlegroundsbuddy.weapondetail

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.WhichButton
import com.afollestad.materialdialogs.actions.setActionButtonEnabled
import com.afollestad.materialdialogs.input.input
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.respondingio.battlegroundsbuddy.R
import com.respondingio.battlegroundsbuddy.snacky.Snacky
import com.respondingio.battlegroundsbuddy.viewmodels.WeaponDetailViewModel
import com.respondingio.battlegroundsbuddy.weapondetail.WeaponHome.Fragment.*
import com.respondingio.battlegroundsbuddy.weapons.CompareWeaponPicker
import com.respondingio.battlegroundsbuddy.weapons.WeaponComments
import kotlinx.android.synthetic.main.new_weapon_home_test.*
import org.jetbrains.anko.longToast
import org.jetbrains.anko.startActivity
import java.text.SimpleDateFormat
import java.util.*

class WeaponHome : AppCompatActivity() {

    private val viewModel: WeaponDetailViewModel by lazy {
        ViewModelProviders.of(this).get(WeaponDetailViewModel::class.java)
    }

    var mMenu: Menu? = null

    private var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>? = null

    override fun onStart() {
        super.onStart()
        viewModel.getWeaponData(intent.getStringExtra("weaponPath") ?: "")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.new_weapon_home_test)
        setSupportActionBar(bar)

        val bundle = Bundle()
        bundle.putString("weaponPath", intent.getStringExtra("weaponPath") ?: "/weapons/sniper_rifles/weapons/kar98")
        bundle.putString("weaponClass", intent.getStringExtra("weaponClass") ?: "Sniper Rifle")
        val weaponHomeFragment = WeaponHomeFragment()
        weaponHomeFragment.arguments = bundle

        supportFragmentManager.beginTransaction().replace(R.id.weapon_frame, weaponHomeFragment).setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out).commit()

        bottomSheetBehavior = BottomSheetBehavior.from(bottomDrawerSheet)
        bottomDrawerSheet.post {
            bottomSheetBehavior?.isHideable = true
            bottomSheetBehavior?.state = BottomSheetBehavior.STATE_HIDDEN

            //bottomSheetBehavior?.peekHeight = 0
        }

        bar?.setNavigationOnClickListener {
            bottomDrawerSheet?.visibility = View.VISIBLE
            Log.d("NAV", bottomSheetBehavior?.state.toString())

            bottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED

            Log.d("NAV", bottomSheetBehavior?.state.toString())
        }

        bottomDrawer?.setNavigationItemSelectedListener {
            when(it.itemId) {
                R.id.weaponNavOverview -> {
                    weaponHomeFAB.setImageResource(R.drawable.ic_compare_arrows_black_24dp)
                    weaponHomeFAB.show()
                    val bundle = Bundle()
                    bundle.putString("weaponPath", intent.getStringExtra("weaponPath") ?: "/weapons/sniper_rifles/weapons/kar98")
                    bundle.putString("weaponClass", intent.getStringExtra("weaponClass") ?: "Sniper Rifle")
                    val weaponHomeFragment = WeaponHomeFragment()
                    weaponHomeFragment.arguments = bundle

                    supportFragmentManager.beginTransaction().replace(R.id.weapon_frame, weaponHomeFragment).setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out).commit()
                }
                R.id.weaponNavComments -> {
                    weaponHomeFAB.setImageResource(R.drawable.ic_edit_24dp)
                    weaponHomeFAB.show()
                    val bundle = Bundle()
                    bundle.putString("weaponPath", intent.getStringExtra("weaponPath") ?: "/weapons/sniper_rifles/weapons/kar98")
                    bundle.putString("weaponClass", intent.getStringExtra("weaponClass") ?: "Sniper Rifle")
                    bundle.putString("weaponKey", intent.getStringExtra("weaponKey") ?: "")
                    val weaponHomeFragment = WeaponComments()
                    weaponHomeFragment.arguments = bundle

                    supportFragmentManager.beginTransaction().replace(R.id.weapon_frame, weaponHomeFragment).setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out).commit()
                }
                R.id.weaponNavDamageChart -> {
                    weaponHomeFAB.hide()
                    val bundle = Bundle()
                    bundle.putString("weaponPath", intent.getStringExtra("weaponPath") ?: "/weapons/sniper_rifles/weapons/kar98")
                    bundle.putString("weaponClass", intent.getStringExtra("weaponClass") ?: "Sniper Rifle")
                    val damageChart = DamageChart()
                    damageChart.arguments = bundle

                    supportFragmentManager.beginTransaction().replace(R.id.weapon_frame, damageChart).setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out).commit()
                }
                else ->  weaponHomeFAB.setImageResource(R.drawable.ic_compare_arrows_black_24dp)
            }
            bottomSheetBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
            return@setNavigationItemSelectedListener false
        }

        weaponHomeFAB?.setOnClickListener {
            when (getCurrentFragment()) {
                OVERVIEW -> {
                    //Launch compare activity
                    startActivity<CompareWeaponPicker>("firstWeapon" to intent.getStringExtra("weaponPath"), "weapon_name" to intent.getStringExtra("weaponName"))
                }
                COMMENTS -> {
                    if (FirebaseAuth.getInstance().currentUser == null) {
                        longToast("You must be logged in to comment.")
                        return@setOnClickListener
                    }

                    MaterialDialog(this@WeaponHome)
                            .title(text = "Add a Comment")
                            .input(hint = "Your Comment") { dialog, text ->
                                dialog.setActionButtonEnabled(WhichButton.POSITIVE, false)
                                val UID = FirebaseAuth.getInstance().currentUser?.uid ?: "Unknown User"
                                val key = FirebaseDatabase.getInstance().getReference("/comments_weapons/${intent.getStringExtra("weaponKey")}").push().key
                                val path = "/comments_weapons/${intent.getStringExtra("weaponKey")}/$key"

                                Log.d("COMMENT", "$path -- $key -- $UID")

                                val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                                val date = Date()

                                val childUpdates = HashMap<String, Any>()
                                childUpdates["$path/comment"] = text.toString()
                                childUpdates["$path/user"] = UID
                                if (FirebaseAuth.getInstance().currentUser!!.displayName!!.isEmpty()) {
                                    childUpdates["$path/user_name"] = UID
                                } else {
                                    childUpdates["$path/user_name"] = FirebaseAuth.getInstance().currentUser?.displayName.toString()
                                }
                                childUpdates["$path/timestamp"] = dateFormat.format(date)
                                childUpdates["$path/weapon_path"] = intent.getStringExtra("weaponPath")

                                childUpdates["/users/$UID/weapon_comments/${intent.getStringExtra("weaponKey")}/$key"] = true

                                FirebaseDatabase.getInstance().reference.updateChildren(childUpdates).addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Snacky.builder().setActivity(this@WeaponHome).success().setText("Comment Posted.").show()
                                        dialog.dismiss()
                                    } else {
                                        Snacky.builder().setActivity(this@WeaponHome).error().setText("Error posting.").show()
                                        dialog.setActionButtonEnabled(WhichButton.POSITIVE, true)
                                    }
                                }
                            }
                            .positiveButton(text = "Post")
                            .negativeButton(text = "Cancel")
                            .show()
                }
            }
        }
    }

    override fun onBackPressed() {
        if (bottomSheetBehavior != null && bottomSheetBehavior?.state == BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.weapo_home_new, menu)
        mMenu = menu
        return super.onCreateOptionsMenu(menu)
    }

    fun getCurrentFragment(): Fragment {
        if (supportFragmentManager.findFragmentById(R.id.weapon_frame) is WeaponHomeFragment) {
            return OVERVIEW
        } else if(supportFragmentManager.findFragmentById(R.id.weapon_frame) is WeaponComments) {
            return COMMENTS
        }
        return NONE
    }

    enum class Fragment {
        NONE,
        OVERVIEW,
        DAMAGE_CHART,
        COMMENTS,
        ATTACHMENTS,
        SOUNDS
    }
}