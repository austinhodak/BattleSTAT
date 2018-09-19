package com.respondingio.battlegroundsbuddy.weapons

import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout.LayoutParams
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.respondingio.battlegroundsbuddy.R
import com.respondingio.battlegroundsbuddy.snacky.Snacky
import kotlinx.android.synthetic.main.weapon_detail_activity.compare_fab
import kotlinx.android.synthetic.main.weapon_detail_activity.tabs
import kotlinx.android.synthetic.main.weapon_detail_activity.toolbar_title
import kotlinx.android.synthetic.main.weapon_detail_activity.top_bar_linear
import kotlinx.android.synthetic.main.weapon_detail_activity.viewpager
import kotlinx.android.synthetic.main.weapon_detail_activity.weapon_detail_toolbar
import org.jetbrains.anko.startActivity

class WeaponDetailsActivity : AppCompatActivity() {

    private var weaponID: String? = null
    private var weaponClass: String? = null
    private var weaponKey: String? = null
    private var weaponName = "Weapon Details"

    private var mSharedPreferences: SharedPreferences? = null
    private lateinit var mFirebaseAnalytics: FirebaseAnalytics
    private lateinit var mFirebaseStore: FirebaseFirestore
    private var weaponListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.weapon_detail_activity)

        mSharedPreferences = getSharedPreferences("com.austinhodak.pubgcenter", MODE_PRIVATE)
        
        setupToolbar()
        setupFirebase()
        
        if (intent == null) {
            return
        }
        
        weaponID = intent.getStringExtra("weaponPath")
        weaponClass = intent.getStringExtra("weaponClass")
        weaponKey = intent.getStringExtra("weaponKey")
        weaponName = intent.getStringExtra("weaponName")
        toolbar_title?.text = weaponName
        
        setupTabs()

        compare_fab.setOnClickListener {
            startActivity<CompareWeaponPicker>("firstWeapon" to weaponID, "weapon_name" to weaponName)
        }
    }

    override fun onStart() {
        super.onStart()
        loadWeapon(weaponID)
    }

    private fun loadWeapon(weaponID: String?) {
        top_bar_linear?.removeViews(1, top_bar_linear.childCount - 1)
        weaponListener = mFirebaseStore.document(weaponID!!).addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
            if (documentSnapshot != null && documentSnapshot.exists()) {
                if (documentSnapshot.contains("airDropOnly") && documentSnapshot.getBoolean("airDropOnly") != null) {
                    if (documentSnapshot.getBoolean("airDropOnly") == true) {
                        val imageView = ImageView(this@WeaponDetailsActivity)

                        val params = LayoutParams(convertDpToPixel(24f), convertDpToPixel(24f))

                        params.setMargins(convertDpToPixel(8f), 0, 0, 0)

                        imageView.layoutParams = params
                        //imageView.setBackground(getResources().getDrawable(R.drawable.chip_green_outline));
                        imageView.setImageDrawable(resources.getDrawable(R.drawable.ic_parachute))
                        //imageView.setPadding(convertDpToPixel(4), convertDpToPixel(4), convertDpToPixel(4), convertDpToPixel(4));

                        if (VERSION.SDK_INT >= VERSION_CODES.O) {
                            imageView.tooltipText = "Air Drop Only"
                        }

                        top_bar_linear?.addView(imageView)
                    }
                }

                if (documentSnapshot.contains("miramar_only") && documentSnapshot.getBoolean("miramar_only") != null) {
                    if (documentSnapshot.getBoolean("miramar_only") == true) {
                        val imageView = ImageView(this@WeaponDetailsActivity)

                        val params = LayoutParams(convertDpToPixel(24f), convertDpToPixel(24f))

                        params.setMargins(convertDpToPixel(8f), 0, 0, 0)

                        imageView.layoutParams = params
                        //imageView.setBackground(getResources().getDrawable(R.drawable.chip_green_outline));
                        imageView.setImageDrawable(resources.getDrawable(R.drawable.cactu))
                        //imageView.setPadding(convertDpToPixel(4), convertDpToPixel(4), convertDpToPixel(4), convertDpToPixel(4));

                        if (VERSION.SDK_INT >= VERSION_CODES.O) {
                            imageView.tooltipText = "Air Drop Only"
                        }

                        top_bar_linear?.addView(imageView)
                    }
                }

                if (documentSnapshot.contains("bestInClass") && documentSnapshot.getBoolean("bestInClass") != null) {
                    if (documentSnapshot.getBoolean("bestInClass") == true) {
                        val imageView = ImageView(this@WeaponDetailsActivity)

                        val params = LayoutParams(convertDpToPixel(24f), convertDpToPixel(24f))

                        params.setMargins(convertDpToPixel(8f), 0, 0, 0)

                        imageView.layoutParams = params
                        //imageView.setBackground(getResources().getDrawable(R.drawable.chip_green_outline));
                        imageView.setImageDrawable(resources.getDrawable(R.drawable.icons8_trophy))
                        //imageView.setPadding(convertDpToPixel(4), convertDpToPixel(4), convertDpToPixel(4), convertDpToPixel(4));

                        if (VERSION.SDK_INT >= VERSION_CODES.O) {
                            imageView.tooltipText = "Air Drop Only"
                        }

                        top_bar_linear?.addView(imageView)
                    }
                }

                if (documentSnapshot.contains("sanhok_only") && documentSnapshot.getBoolean("sanhok_only") != null) {
                    if (documentSnapshot.getBoolean("sanhok_only") == true) {
                        val imageView = ImageView(this@WeaponDetailsActivity)

                        val params = LayoutParams(convertDpToPixel(24f), convertDpToPixel(24f))

                        params.setMargins(convertDpToPixel(8f), 0, 0, 0)

                        imageView.layoutParams = params
                        //imageView.setBackground(getResources().getDrawable(R.drawable.chip_green_outline));
                        imageView.setImageDrawable(resources.getDrawable(R.drawable.sanhok_icon))
                        //imageView.setPadding(convertDpToPixel(4), convertDpToPixel(4), convertDpToPixel(4), convertDpToPixel(4));

                        if (VERSION.SDK_INT >= VERSION_CODES.O) {
                            imageView.tooltipText = "Air Drop Only"
                        }

                        top_bar_linear?.addView(imageView)
                    }
                }


            }

            weaponListener?.remove()
        }
    }

    override fun onStop() {
        super.onStop()
        weaponListener?.remove()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.weapon_detail, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.favorite_weapon -> {
                if (item.icon.constantState == resources.getDrawable(R.drawable.ic_star_border_24dp).constantState) {
                    item.setIcon(R.drawable.ic_star_gold_24dp)

                    val favs = mSharedPreferences?.getStringSet("favoriteWeapons", null)
                    if (favs != null && weaponID != null) {
                        val newSet = HashSet<String>(favs)
                        newSet.add(weaponID!!)
                        mSharedPreferences?.edit()?.putStringSet("favoriteWeapons", newSet)?.apply()
                    } else {
                        val favsNew = java.util.HashSet<String>()
                        favsNew.add(weaponID!!)
                        mSharedPreferences?.edit()?.putStringSet("favoriteWeapons", favsNew)?.apply()
                    }

                    Snacky.builder().setActivity(this).setText("Added to favorites").build().show()
                } else {
                    item.setIcon(R.drawable.ic_star_border_24dp)

                    val favs = mSharedPreferences?.getStringSet("favoriteWeapons", null)
                    if (favs != null && favs.contains(weaponID)) {
                        favs.remove(weaponID)

                        mSharedPreferences?.edit()?.remove("favoriteWeapons")?.apply()
                        mSharedPreferences?.edit()?.putStringSet("favoriteWeapons", favs)?.apply()
                    }
                }
            }
            R.id.compare_menu -> startActivity<CompareWeaponPicker>("firstWeapon" to weaponID, "weapon_name" to weaponName)
        }
        return false
    }

    @SuppressLint("RestrictedApi")
    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        val alertMenuItem = menu?.findItem(R.id.favorite_weapon)
        val favs = mSharedPreferences?.getStringSet("favoriteWeapons", null)

        if (favs != null) {
            if (favs.contains(weaponID)) {
                alertMenuItem?.setIcon(R.drawable.ic_star_gold_24dp)
            } else {
                alertMenuItem?.setIcon(R.drawable.ic_star_border_24dp)
            }
        }

        if (VERSION.SDK_INT < VERSION_CODES.LOLLIPOP) {
            val compare = menu?.findItem(R.id.compare_menu)
            compare?.isVisible = true

            compare_fab.visibility = View.GONE
        }
        return super.onPrepareOptionsMenu(menu)
    }

    private fun setupTabs() {
        viewpager?.adapter = ViewPagerAdapter(supportFragmentManager)
        tabs?.setupWithViewPager(viewpager)
        viewpager?.currentItem = 1
        tabs?.setScrollPosition(1, 0f, true)
        
        viewpager?.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrollStateChanged(p0: Int) {
                
            }

            override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {
                
            }

            override fun onPageSelected(p0: Int) {
                if (p0 == 0) {
                    hideFab()
                } else {
                    showFab()
                }
            }
        })
    }

    @SuppressLint("RestrictedApi")
    private fun showFab() {
        if (compare_fab?.visibility == View.GONE) {
            compare_fab?.visibility = View.VISIBLE
            compare_fab?.scaleX = 0f
            compare_fab?.scaleY = 0f
            compare_fab?.animate()?.scaleX(1f)?.scaleY(1f)?.setDuration(200)?.setListener(object : AnimatorListener {
                        override fun onAnimationCancel(animation: Animator) {

                        }

                        override fun onAnimationEnd(animation: Animator) {
                            compare_fab?.visibility = View.VISIBLE
                            compare_fab?.scaleX = 1f
                            compare_fab?.scaleY = 1f
                        }

                        override fun onAnimationRepeat(animation: Animator) {

                        }

                        override fun onAnimationStart(animation: Animator) {

                        }
                    })?.start()
        }
    }

    @SuppressLint("RestrictedApi")
    private fun hideFab() {
        if (compare_fab?.visibility == View.GONE) { return }
            compare_fab?.scaleX = 1f
            compare_fab?.scaleY = 1f
            compare_fab?.animate()?.scaleX(0f)?.scaleY(0f)?.setDuration(200)?.setListener(object : AnimatorListener {
                override fun onAnimationCancel(animation: Animator) {

                }

                override fun onAnimationEnd(animation: Animator) {
                    compare_fab?.visibility = View.GONE
                }

                override fun onAnimationRepeat(animation: Animator) {

                }

                override fun onAnimationStart(animation: Animator) {

                }
            })?.start()
    }

    private fun setupFirebase() {
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
        mFirebaseStore = FirebaseFirestore.getInstance()
    }

    private fun setupToolbar() {
        setSupportActionBar(weapon_detail_toolbar)
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setDisplayShowHomeEnabled(true)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    fun convertDpToPixel(dp: Float): Int {
        val resources = this@WeaponDetailsActivity.resources
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                resources.displayMetrics
        ).toInt()
    }

    internal inner class ViewPagerAdapter(manager: FragmentManager) : FragmentPagerAdapter(manager) {

        private val title = arrayOf("Comments", "Overview", "Spread", "Deviation", "Recoil", "Sway", "Camera DOF")

        override fun getCount(): Int {
            return title.size
        }

        override fun getItem(position: Int): Fragment {
            val currentFragment: Fragment
            when (position) {
                0 -> currentFragment = WeaponComments()
                1 -> currentFragment = WeaponStatsOverview()
                2 -> currentFragment = WeaponDetailSpread()
                3 -> currentFragment = WeaponDetailDeviation()
                4 -> currentFragment = WeaponDetailRecoil()
                5 -> currentFragment = WeaponDetailSway()
                6 -> currentFragment = WeaponDetailCamera()
                else -> currentFragment = WeaponDetailOverview()
            }

            val bundle = Bundle()
            bundle.putString("weaponClass", weaponClass)
            bundle.putString("weaponPath", weaponID)
            bundle.putString("weaponKey", weaponKey)
            bundle.putString("weaponName", weaponName)
            currentFragment.arguments = bundle
            return currentFragment
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return title[position]
        }
    }
}