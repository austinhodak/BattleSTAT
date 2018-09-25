package com.respondingio.battlegroundsbuddy.weapondetail

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.respondingio.battlegroundsbuddy.R
import com.respondingio.battlegroundsbuddy.viewmodels.WeaponDetailViewModel
import com.respondingio.battlegroundsbuddy.weapondetail.WeaponHome.Fragment.COMMENTS
import com.respondingio.battlegroundsbuddy.weapondetail.WeaponHome.Fragment.NONE
import com.respondingio.battlegroundsbuddy.weapondetail.WeaponHome.Fragment.OVERVIEW
import com.respondingio.battlegroundsbuddy.weapons.CompareWeaponPicker
import com.respondingio.battlegroundsbuddy.weapons.WeaponComments
import kotlinx.android.synthetic.main.new_weapon_home_test.bar
import kotlinx.android.synthetic.main.new_weapon_home_test.bottomDrawer
import kotlinx.android.synthetic.main.new_weapon_home_test.bottomDrawerSheet
import kotlinx.android.synthetic.main.new_weapon_home_test.weaponHomeFAB
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.startActivity

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
        bottomSheetBehavior?.isHideable = true
        bottomSheetBehavior?.state = BottomSheetBehavior.STATE_HIDDEN

        bar?.setNavigationOnClickListener {
            bottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
        }

        bottomDrawer?.setNavigationItemSelectedListener {
            when(it.itemId) {
                R.id.weaponNavOverview -> {
                    weaponHomeFAB.setImageResource(R.drawable.ic_compare_arrows_black_24dp)
                    val bundle = Bundle()
                    bundle.putString("weaponPath", intent.getStringExtra("weaponPath") ?: "/weapons/sniper_rifles/weapons/kar98")
                    bundle.putString("weaponClass", intent.getStringExtra("weaponClass") ?: "Sniper Rifle")
                    val weaponHomeFragment = WeaponHomeFragment()
                    weaponHomeFragment.arguments = bundle

                    supportFragmentManager.beginTransaction().replace(R.id.weapon_frame, weaponHomeFragment).setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out).commit()
                }
                R.id.weaponNavComments -> {
                    weaponHomeFAB.setImageResource(R.drawable.ic_edit_24dp)
                    val bundle = Bundle()
                    bundle.putString("weaponPath", intent.getStringExtra("weaponPath") ?: "/weapons/sniper_rifles/weapons/kar98")
                    bundle.putString("weaponClass", intent.getStringExtra("weaponClass") ?: "Sniper Rifle")
                    bundle.putString("weaponKey", intent.getStringExtra("weaponKey") ?: "")
                    val weaponHomeFragment = WeaponComments()
                    weaponHomeFragment.arguments = bundle

                    supportFragmentManager.beginTransaction().replace(R.id.weapon_frame, weaponHomeFragment).setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out).commit()
                }
                else ->  weaponHomeFAB.setImageResource(R.drawable.ic_compare_arrows_black_24dp)
            }
            bottomSheetBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
            return@setNavigationItemSelectedListener false
        }

        weaponHomeFAB?.onClick {
            when (getCurrentFragment()) {
                OVERVIEW -> {
                    //Launch compare activity
                    startActivity<CompareWeaponPicker>("firstWeapon" to intent.getStringExtra("weaponPath"), "weapon_name" to intent.getStringExtra("weaponName"))
                }
                COMMENTS -> {
                    //TODO CREATE NEW COMMENT DIALOG
                    MaterialDialog(this@WeaponHome)
                            .title(text = "Add a Comment")
                            .input(hint = "Your Comment") { dialog, text ->

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

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return true
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