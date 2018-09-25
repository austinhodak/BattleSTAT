package com.respondingio.battlegroundsbuddy.weapons

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.bumptech.glide.Glide
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.FirebaseAnalytics.Event
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.FirebaseStorage
import com.respondingio.battlegroundsbuddy.R
import com.respondingio.battlegroundsbuddy.models.WeaponSound
import com.respondingio.battlegroundsbuddy.snacky.Snacky
import kotlinx.android.synthetic.main.fragment_home_weapons_tab.boat_shots
import kotlinx.android.synthetic.main.fragment_home_weapons_tab.buggy_shots
import kotlinx.android.synthetic.main.fragment_home_weapons_tab.burst_layout
import kotlinx.android.synthetic.main.fragment_home_weapons_tab.dacia_shots
import kotlinx.android.synthetic.main.fragment_home_weapons_tab.desc_text
import kotlinx.android.synthetic.main.fragment_home_weapons_tab.mc_shots
import kotlinx.android.synthetic.main.fragment_home_weapons_tab.overview_full_stats
import kotlinx.android.synthetic.main.fragment_home_weapons_tab.suggest_button
import kotlinx.android.synthetic.main.fragment_home_weapons_tab.uaz_shots
import kotlinx.android.synthetic.main.fragment_home_weapons_tab.weaponAmmoTV
import kotlinx.android.synthetic.main.fragment_home_weapons_tab.weaponBaseDamageTV
import kotlinx.android.synthetic.main.fragment_home_weapons_tab.weaponBody0Card
import kotlinx.android.synthetic.main.fragment_home_weapons_tab.weaponBody0TV
import kotlinx.android.synthetic.main.fragment_home_weapons_tab.weaponBody1Card
import kotlinx.android.synthetic.main.fragment_home_weapons_tab.weaponBody1TV
import kotlinx.android.synthetic.main.fragment_home_weapons_tab.weaponBody2Card
import kotlinx.android.synthetic.main.fragment_home_weapons_tab.weaponBody2TV
import kotlinx.android.synthetic.main.fragment_home_weapons_tab.weaponBody3Card
import kotlinx.android.synthetic.main.fragment_home_weapons_tab.weaponBody3TV
import kotlinx.android.synthetic.main.fragment_home_weapons_tab.weaponBurstDelayTV
import kotlinx.android.synthetic.main.fragment_home_weapons_tab.weaponBurstShotTV
import kotlinx.android.synthetic.main.fragment_home_weapons_tab.weaponDetailAttachmentRV
import kotlinx.android.synthetic.main.fragment_home_weapons_tab.weaponDetailSoundsCard
import kotlinx.android.synthetic.main.fragment_home_weapons_tab.weaponDetailSoundsHeader
import kotlinx.android.synthetic.main.fragment_home_weapons_tab.weaponDetailSoundsList
import kotlinx.android.synthetic.main.fragment_home_weapons_tab.weaponFiringModeTV
import kotlinx.android.synthetic.main.fragment_home_weapons_tab.weaponHead0Card
import kotlinx.android.synthetic.main.fragment_home_weapons_tab.weaponHead0TV
import kotlinx.android.synthetic.main.fragment_home_weapons_tab.weaponHead1Card
import kotlinx.android.synthetic.main.fragment_home_weapons_tab.weaponHead1TV
import kotlinx.android.synthetic.main.fragment_home_weapons_tab.weaponHead2Card
import kotlinx.android.synthetic.main.fragment_home_weapons_tab.weaponHead2TV
import kotlinx.android.synthetic.main.fragment_home_weapons_tab.weaponHead3Card
import kotlinx.android.synthetic.main.fragment_home_weapons_tab.weaponHead3TV
import kotlinx.android.synthetic.main.fragment_home_weapons_tab.weaponMagSize
import kotlinx.android.synthetic.main.fragment_home_weapons_tab.weaponMiscPickup
import kotlinx.android.synthetic.main.fragment_home_weapons_tab.weaponMiscReady
import kotlinx.android.synthetic.main.fragment_home_weapons_tab.weaponPowerTV
import kotlinx.android.synthetic.main.fragment_home_weapons_tab.weaponRangeTV
import kotlinx.android.synthetic.main.fragment_home_weapons_tab.weaponReloadDurFullTV
import kotlinx.android.synthetic.main.fragment_home_weapons_tab.weaponReloadDurTacTV
import kotlinx.android.synthetic.main.fragment_home_weapons_tab.weaponReloadMethodTV
import kotlinx.android.synthetic.main.fragment_home_weapons_tab.weaponSpeedTV
import kotlinx.android.synthetic.main.fragment_home_weapons_tab.weaponTBSTV
import kotlinx.android.synthetic.main.fragment_home_weapons_tab.weapon_desc_arrow
import kotlinx.android.synthetic.main.fragment_home_weapons_tab.weapon_desc_card
import kotlinx.android.synthetic.main.fragment_home_weapons_tab.weapon_desc_card_content
import kotlinx.android.synthetic.main.fragment_home_weapons_tab.weapon_desc_layout
import kotlinx.android.synthetic.main.fragment_home_weapons_tab.wiki_button
import net.idik.lib.slimadapter.SlimAdapter
import net.idik.lib.slimadapter.SlimInjector
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.support.v4.browse
import org.jetbrains.anko.support.v4.email
import org.jetbrains.anko.support.v4.onUiThread
import org.jetbrains.anko.support.v4.startActivity
import java.io.File
import java.io.IOException
import java.util.Comparator
import java.util.HashMap

class WeaponStatsOverview : Fragment() {

    lateinit var mFirebaseAnalytics: FirebaseAnalytics
    lateinit var mFirebaseStore: FirebaseFirestore
    lateinit var mFirebaseStorage: FirebaseStorage
    lateinit var mSharedPreferences: SharedPreferences
    lateinit var mAttachmentsAdapter: SlimAdapter
    lateinit var mSoundsAdapter: SlimAdapter
    private val soundsList = ArrayList<Any>()
    private val listenerList = ArrayList<ListenerRegistration>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home_weapons_tab, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        suggest_button.setOnClickListener { email("pubgbattlebuddy@gmail.com", "Battlegrounds Battle Buddy Suggestion") }
        weaponDetailAttachmentRV.isNestedScrollingEnabled = false
        setupSoundsList()

        weapon_desc_card.setOnClickListener {
            if (weapon_desc_card_content?.visibility == View.GONE) {
                weapon_desc_card_content?.visibility = View.VISIBLE

                weapon_desc_arrow.setImageDrawable(resources.getDrawable(R.drawable.ic_arrow_drop_up_24dp))
            } else {
                weapon_desc_card_content?.visibility = View.GONE

                weapon_desc_arrow.setImageDrawable(resources.getDrawable(R.drawable.ic_arrow_drop_down_24dp))
            }
        }

        if (arguments != null) {
            loadWeapon(arguments!!.getString("weaponPath")!!)
        }
    }

    override fun onStart() {
        super.onStart()


        overview_full_stats.setOnClickListener {
            startActivity<WeaponDamageChart>("weaponPath" to arguments!!.getString("weaponPath"), "weaponKey" to arguments!!.getString("weaponKey"), "weaponClass" to arguments!!.getString("weaponClass"), "weaponName" to arguments!!.getString("weaponName"))
        }
    }

    override fun onStop() {
        super.onStop()
        for (i in listenerList) {
            i.remove()
        }

        weaponDetailAttachmentRV.adapter = null
        weaponDetailSoundsList.adapter = null
    }

    private fun loadWeapon(string: String) {
        listenerList.add(mFirebaseStore.document(string).addSnapshotListener { data, firebaseFirestoreException ->
            if (firebaseFirestoreException != null || data == null || !data.exists()) return@addSnapshotListener

            if (data.contains("desc") && data.get("desc") != null) {
                if (data.contains("wiki") && data.get("wiki") != null) {
                    wiki_button?.setOnClickListener {
                        browse(data.getString("wiki")!!)
                    }
                } else {
                    wiki_button?.visibility = View.INVISIBLE
                }

                desc_text.text = data.getString("desc")!!.replace("<br>", "\n").replace("  ", "\n\n")
            } else {
                weapon_desc_layout.visibility = View.GONE
            }

            val bundle = Bundle()
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, data.id)
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME,
                    data.getString("weapon_name"))
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "weapon_view")
            mFirebaseAnalytics.logEvent(Event.VIEW_ITEM, bundle)

            weaponAmmoTV.text = data.getString("ammo") ?: "--"
            weaponMagSize.text = data.getString("ammoPerMag") ?: "--"
            weaponBaseDamageTV.text = data.getString("damageBody0") ?: "--"
            weaponSpeedTV.text = "${data.getString("speed") ?: "--"} m/s"
            weaponPowerTV.text = data.getString("power") ?: "--"
            weaponRangeTV.text = data.getString("range") ?: "--"
            weaponTBSTV.text = data.getString("TBS") ?: "--"
            weaponBurstShotTV.text = data.getString("burstShots") ?: "--"
            weaponBurstDelayTV.text = data.getString("burstDelay") ?: "--"
            
            if (data.getString("firingModes") != null) {
                weaponFiringModeTV.text = data.getString("firingModes")!!.toUpperCase()
                
                if (data.getString("firingModes")!!.contains("BURST")) {
                    burst_layout.visibility = View.VISIBLE
                }
            }
            
            weaponReloadDurFullTV.text = data.getString("reloadDurationFull") ?: "--"
            weaponReloadDurTacTV.text = data.getString("reloadDurationTac") ?: "--"
            weaponReloadMethodTV.text = data.getString("reloadMethod") ?: "--"
            weaponMiscPickup.text = data.getString("pickupDelay") ?: "--"
            weaponMiscReady.text = data.getString("readyDelay") ?: "--"
            
            if (data.contains("incomplete")) {
                Snacky.builder().setView(activity!!.findViewById(R.id.rl)).info().setDuration(2000)
                        .setText("Complete stats not available for this weapon.")
                        .show()
            }

            loadAttachments(data)
            loadVehicleDamage(data)
            loadDamageStats(data)
            
            soundsList.clear()
            
            doAsync {
                if (data.contains("audio")) {
                    updateAudioVisibility(View.VISIBLE)
                    val audioObject = data.get("audio") as HashMap<String, String>?
                    Log.d("WEAPON", audioObject!!.toString())

                    for (s in audioObject.keys) {
                        if (audioObject[s]!!.isEmpty()) {
                            continue
                        }
                        when (s) {
                            "normal-single" -> soundsList.add(WeaponSound(s, "Normal Single", audioObject[s]!!))
                            "normal-burst" -> soundsList.add(WeaponSound(s, "Normal Burst", audioObject[s]!!))
                            "normal-auto" -> soundsList.add(WeaponSound(s, "Normal Auto", audioObject[s]!!))
                            "suppressed-single" -> soundsList.add(WeaponSound(s, "Suppressed Single", audioObject[s]!!))
                            "suppressed-auto" -> soundsList.add(WeaponSound(s, "Suppressed Auto", audioObject[s]!!))
                            "suppressed-burst" -> soundsList.add(WeaponSound(s, "Suppressed Burst", audioObject[s]!!))
                            "reloading" -> soundsList.add(WeaponSound(s, "Reloading", audioObject[s]!!))
                        }
                    }

                    soundsList.sortWith(Comparator { o, t1 ->
                        if (o is WeaponSound && t1 is WeaponSound) {
                            o.title.compareTo(t1.title, ignoreCase = true)
                        } else {
                            0
                        }
                    })

                    mSoundsAdapter.updateData(soundsList)
                } else {
                    updateAudioVisibility(View.GONE)
                }
            }
        })
    }

    private fun loadAttachments(data: DocumentSnapshot) {
        if (!data.contains("attachments")) return

        val attachmentsList = ArrayList<DocumentSnapshot>()

        weaponDetailAttachmentRV.layoutManager = LinearLayoutManager(requireActivity())
        mAttachmentsAdapter = SlimAdapter.create().register(R.layout.weapon_list_item) { data: DocumentSnapshot, injector ->
            if (!data.exists()) {
                return@register
            }

            if (data.get("icon") != null) {
                val gsReference = mFirebaseStorage
                        .getReferenceFromUrl(data.getString("icon")!!)

                if (activity != null)
                    Glide.with(this)
                            .load(gsReference)
                            .into(injector.findViewById(R.id.helmetItem64) as ImageView)

            }

            if (data.contains("name")) {
                injector.text(R.id.weaponItemName, data.getString("name"))
            }

            if (data.contains("location")) {
                injector.text(R.id.weaponItemSubtitle, data.getString("location"))
            }

            injector.clicked(R.id.top_layout) {
                if (data.contains("stats")) {
                    var stats = ""
                    stats = data.getString("stats")!!.replace("<br>".toRegex(), "")
                    stats = stats.replace(" +", "\n+")
                    stats = stats.replace(" -", "\n-")
                    val materialDialog = MaterialDialog(activity!!)
                            .title(text = data.getString("name")!!)
                            .message(text = stats)
                            .positiveButton(text = "OK")
                            .show()
                }
            }
        }.attachTo(weaponDetailAttachmentRV).updateData(attachmentsList)

        val list = data.get("attachments") as List<DocumentReference>?
        for (snap in list!!) {
            listenerList.add(snap.addSnapshotListener { attachment, e ->
                attachmentsList.add(attachment!!)

                try {
                    attachmentsList.sortWith(Comparator { s1, s2 -> s1.getString("name")!!.compareTo(s2.getString("name")!!, ignoreCase = true) })
                } catch (e1: Exception) {
                    e1.printStackTrace()
                }

                mAttachmentsAdapter.updateData(attachmentsList)
            })
        }
    }

    private fun loadDamageStats(data: DocumentSnapshot) {
        listenerList.add(data.reference.collection("stats").document("damage").addSnapshotListener(
                EventListener { damageData, e ->
                    val cardViewList = java.util.ArrayList<CardView>()
                    cardViewList.add(weaponBody0Card)
                    cardViewList.add(weaponBody1Card)
                    cardViewList.add(weaponBody2Card)
                    cardViewList.add(weaponBody3Card)
                    cardViewList.add(weaponHead0Card)
                    cardViewList.add(weaponHead1Card)
                    cardViewList.add(weaponHead2Card)
                    cardViewList.add(weaponHead3Card)

                    for (cardView in cardViewList) {
                        cardView.setOnClickListener {
                            val title: String
                            val color: Int

                            when {
                                cardView.cardBackgroundColor.defaultColor == resources
                                        .getColor(R.color.md_red_500) -> {
                                    title = "1 Hit to Kill"
                                    color = resources.getColor(R.color.md_red_500)
                                }
                                cardView.cardBackgroundColor.defaultColor == resources
                                        .getColor(R.color.md_deep_orange_500) -> {
                                    title = "2 Hits to Kill"
                                    color = resources.getColor(R.color.md_deep_orange_500)
                                }
                                cardView.cardBackgroundColor.defaultColor == resources
                                        .getColor(R.color.md_amber_500) -> {
                                    title = "3 Hits to Kill"
                                    color = resources.getColor(R.color.md_amber_500)
                                }
                                cardView.cardBackgroundColor.defaultColor == resources
                                        .getColor(R.color.md_green_500) -> {
                                    title = "4 Hits to Kill"
                                    color = resources.getColor(R.color.md_green_500)
                                }
                                else -> {
                                    title = "5+ Hits to Kill"
                                    color = resources.getColor(R.color.md_grey_800)
                                }
                            }

                            if (activity != null)
                                MaterialDialog(activity!!)
                                        .title(text = title)
                                        .message( text =
                                                "This is assuming the shooter is within normal range of the gun used.")
                                        .positiveButton(text = "CLOSE")
                                        .show()
                        }
                    }

                    if (activity == null) {
                        return@EventListener
                    }

                    if (damageData != null && damageData.exists()) {
                        if (damageData.getString("body0") != null) {
                            val cardView = weaponBody0Card
                            weaponBody0TV.text = damageData.getString("body0")
                            if (damageData.getString("body0HTK") != null) {
                                when (damageData.getString("body0HTK")) {
                                    "1" -> cardView.setCardBackgroundColor(
                                            resources.getColor(R.color.md_red_500))
                                    "2" -> cardView.setCardBackgroundColor(
                                            resources.getColor(R.color.md_deep_orange_500))
                                    "3" -> cardView.setCardBackgroundColor(
                                            resources.getColor(R.color.md_amber_500))
                                    "4" -> cardView.setCardBackgroundColor(
                                            resources.getColor(R.color.md_green_500))
                                }
                            }
                        }

                        if (damageData.getString("body1") != null) {
                            val tv = weaponBody1TV
                            val cardView = weaponBody1Card
                            tv.text = damageData.getString("body1")
                            if (damageData.getString("body1HTK") != null) {
                                when (damageData.getString("body1HTK")) {
                                    "1" -> cardView.setCardBackgroundColor(
                                            resources.getColor(R.color.md_red_500))
                                    "2" -> cardView.setCardBackgroundColor(
                                            resources.getColor(R.color.md_deep_orange_500))
                                    "3" -> cardView.setCardBackgroundColor(
                                            resources.getColor(R.color.md_amber_500))
                                    "4" -> cardView.setCardBackgroundColor(
                                            resources.getColor(R.color.md_green_500))
                                }
                            }
                        }

                        if (damageData.getString("body2") != null) {
                            val tv = weaponBody2TV
                            val cardView = weaponBody2Card
                            tv.text = damageData.getString("body2")
                            if (damageData.getString("body2HTK") != null) {
                                when (damageData.getString("body2HTK")) {
                                    "1" -> cardView.setCardBackgroundColor(
                                            resources.getColor(R.color.md_red_500))
                                    "2" -> cardView.setCardBackgroundColor(
                                            resources.getColor(R.color.md_deep_orange_500))
                                    "3" -> cardView.setCardBackgroundColor(
                                            resources.getColor(R.color.md_amber_500))
                                    "4" -> cardView.setCardBackgroundColor(
                                            resources.getColor(R.color.md_green_500))
                                }
                            }
                        }

                        if (damageData.getString("body3") != null) {
                            val tv = weaponBody3TV
                            val cardView = weaponBody3Card
                            tv.text = damageData.getString("body3")
                            if (damageData.getString("body3HTK") != null) {
                                when (damageData.getString("body3HTK")) {
                                    "1" -> cardView.setCardBackgroundColor(
                                            resources.getColor(R.color.md_red_500))
                                    "2" -> cardView.setCardBackgroundColor(
                                            resources.getColor(R.color.md_deep_orange_500))
                                    "3" -> cardView.setCardBackgroundColor(
                                            resources.getColor(R.color.md_amber_500))
                                    "4" -> cardView.setCardBackgroundColor(
                                            resources.getColor(R.color.md_green_500))
                                }
                            }

                        }

                        if (damageData.getString("head0") != null) {
                            val tv = weaponHead0TV
                            val cardView = weaponHead0Card
                            tv.text = damageData.getString("head0")
                            if (damageData.getString("head0HTK") != null) {
                                when (damageData.getString("head0HTK")) {
                                    "1" -> cardView.setCardBackgroundColor(
                                            resources.getColor(R.color.md_red_500))
                                    "2" -> cardView.setCardBackgroundColor(
                                            resources.getColor(R.color.md_deep_orange_500))
                                    "3" -> cardView.setCardBackgroundColor(
                                            resources.getColor(R.color.md_amber_500))
                                    "4" -> cardView.setCardBackgroundColor(
                                            resources.getColor(R.color.md_green_500))
                                }
                            }

                        }

                        if (damageData.getString("head1") != null) {
                            val tv = weaponHead1TV
                            val cardView = weaponHead1Card
                            tv.text = damageData.getString("head1")
                            if (damageData.getString("head1HTK") != null) {
                                when (damageData.getString("head1HTK")) {
                                    "1" -> cardView.setCardBackgroundColor(
                                            resources.getColor(R.color.md_red_500))
                                    "2" -> cardView.setCardBackgroundColor(
                                            resources.getColor(R.color.md_deep_orange_500))
                                    "3" -> cardView.setCardBackgroundColor(
                                            resources.getColor(R.color.md_amber_500))
                                    "4" -> cardView.setCardBackgroundColor(
                                            resources.getColor(R.color.md_green_500))
                                }
                            }
                        }

                        if (damageData.getString("head2") != null) {
                            val tv = weaponHead2TV
                            val cardView = weaponHead2Card
                            tv.text = damageData.getString("head2")
                            if (damageData.getString("head2HTK") != null) {
                                when (damageData.getString("head2HTK")) {
                                    "1" -> cardView.setCardBackgroundColor(
                                            resources.getColor(R.color.md_red_500))
                                    "2" -> cardView.setCardBackgroundColor(
                                            resources.getColor(R.color.md_deep_orange_500))
                                    "3" -> cardView.setCardBackgroundColor(
                                            resources.getColor(R.color.md_amber_500))
                                    "4" -> cardView.setCardBackgroundColor(
                                            resources.getColor(R.color.md_green_500))
                                }
                            }
                        }

                        if (damageData.getString("head3") != null) {
                            val tv = weaponHead3TV
                            val cardView = weaponHead3Card
                            tv.text = damageData.getString("head3")
                            if (damageData.getString("head3HTK") != null) {
                                when (damageData.getString("head3HTK")) {
                                    "1" -> cardView.setCardBackgroundColor(
                                            resources.getColor(R.color.md_red_500))
                                    "2" -> cardView.setCardBackgroundColor(
                                            resources.getColor(R.color.md_deep_orange_500))
                                    "3" -> cardView.setCardBackgroundColor(
                                            resources.getColor(R.color.md_amber_500))
                                    "4" -> cardView.setCardBackgroundColor(
                                            resources.getColor(R.color.md_green_500))
                                }
                            }
                        }
                    }
                }))
    }

    private fun loadVehicleDamage(data: DocumentSnapshot) {
        if (data.getString("damageBody0") == null) {
            return
        }
        try {
            val damage = java.lang.Float.parseFloat(data.getString("damageBody0")!!)

            val buggy = String.format("%.0f", Math.ceil((1540 / damage).toDouble())) + " Shots"
            val dacia = String.format("%.0f", Math.ceil((1820 / damage).toDouble())) + " Shots"
            val mc = String.format("%.0f", Math.ceil((1025 / damage).toDouble())) + " Shots"
            val boat = String.format("%.0f", Math.ceil((1520 / damage).toDouble())) + " Shots"
            val uaz = String.format("%.0f", Math.ceil((1820 / damage).toDouble())) + " Shots"

            buggy_shots?.text = buggy
            dacia_shots?.text = dacia
            mc_shots?.text = mc
            boat_shots?.text = boat
            uaz_shots?.text = uaz
        } catch (e: NumberFormatException) {
            e.printStackTrace()
        }

    }

    private fun updateAudioVisibility(vis: Int) {
        onUiThread {
            weaponDetailSoundsCard?.visibility = vis
            weaponDetailSoundsHeader?.visibility = vis
            weaponDetailSoundsList?.visibility = vis
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(requireActivity())
        mFirebaseStore = FirebaseFirestore.getInstance()
        mFirebaseStorage = FirebaseStorage.getInstance()

        mSharedPreferences = requireActivity().getSharedPreferences("com.austinhodak.pubgcenter", MODE_PRIVATE)


    }

    private fun setupSoundsList() {
        weaponDetailSoundsList.layoutManager = LinearLayoutManager(requireActivity())
        mSoundsAdapter = SlimAdapter.create().attachTo(weaponDetailSoundsList).register(R.layout.weapon_audio_list_item, SlimInjector<WeaponSound> { (value, title, url), injector ->
            injector.text(R.id.weaponAudioText, title)

            val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(url)

            val isPlaying = booleanArrayOf(false)
            val isLoaded = booleanArrayOf(false)

            val mediaPlayer = MediaPlayer()
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)

            var localFile: File? = null
            try {
                localFile = File.createTempFile(value, "ogg")
            } catch (e: IOException) {
                e.printStackTrace()
            }

            val finalLocalFile = localFile
            injector.clicked(R.id.weaponAudioPlay, object : OnClickListener {
                override fun onClick(view: View) {
                    if (!isPlaying[0]) {
                        isPlaying[0] = true
                        injector.image(R.id.weaponAudioPlay, R.drawable.ic_pause_circle_filled_white_24dp)
                        try {
                            startPlaying()
                        } catch (e: IllegalStateException) {
                            e.printStackTrace()
                        }

                    } else {
                        mediaPlayer.stop()
                        isPlaying[0] = false
                        injector.image(R.id.weaponAudioPlay, R.drawable.ic_play_circle_filled_white_24dp)
                    }
                }

                private fun startPlaying() {
                    injector.visible(R.id.audioPg)
                    if (finalLocalFile != null && !isLoaded[0]) {
                        storageReference.getFile(finalLocalFile).addOnSuccessListener {
                            isLoaded[0] = true
                            injector.gone(R.id.audioPg)
                            try {
                                mediaPlayer.reset()
                                mediaPlayer.setDataSource(requireActivity(), Uri.parse(finalLocalFile.absolutePath))
                                mediaPlayer.prepare()
                                mediaPlayer.start()
                            } catch (e: IOException) {
                                e.printStackTrace()
                            } catch (e: IllegalStateException) {
                                e.printStackTrace()
                            }
                        }
                    } else {
                        injector.gone(R.id.audioPg)
                        try {
                            mediaPlayer.reset()
                            mediaPlayer.setDataSource(requireActivity(), Uri.parse(finalLocalFile!!.absolutePath))
                            mediaPlayer.prepare()
                            mediaPlayer.start()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }

                    }
                }
            })

            mediaPlayer.setOnCompletionListener { mediaPlayer ->
                mediaPlayer.stop()
                if (isPlaying[0]) {
                    isPlaying[0] = false
                    injector.image(R.id.weaponAudioPlay, R.drawable.ic_play_circle_filled_white_24dp)
                }
            }
        })
    }
}