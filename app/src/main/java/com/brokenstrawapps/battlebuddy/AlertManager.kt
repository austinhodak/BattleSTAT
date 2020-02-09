package com.brokenstrawapps.battlebuddy

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.messaging.FirebaseMessaging
import com.brokenstrawapps.battlebuddy.models.PrefPlayer
import com.brokenstrawapps.battlebuddy.utils.Alerts
import kotlinx.android.synthetic.main.dialog_alert_manager.*
import net.idik.lib.slimadapter.SlimAdapter
import org.jetbrains.anko.appcompat.v7.navigationIconResource

class AlertManager : AppCompatActivity() {

    private var players: MutableList<PrefPlayer> = ArrayList()
    lateinit var mAdapter: SlimAdapter
    private var userAccountID = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.fragment_fade_enter, R.anim.fragment_fade_exit)
        setContentView(R.layout.dialog_alert_manager)

        val enabledColor = resources.getColor(R.color.timelineGreen)
        val pcEnabledColor = resources.getColor(R.color.timelineBlue)
        val ps4EnabledColor = Color.parseColor("#003791")
        val orangeColor = resources.getColor(R.color.timelineOrange)
        val disabledColor = resources.getColor(R.color.md_grey_850)

        if (intent.action != null) {
            toolbar_title?.text = "Pick a Player"
            toolbar.navigationIconResource = R.drawable.ic_close_24dp
        } else {
            toolbar.navigationIconResource = R.drawable.ic_arrow_back_24dp
        }

        toolbar.setNavigationOnClickListener { onBackPressed() }

        val mFCM = FirebaseMessaging.getInstance()

        Alerts.init(applicationContext)

        if (Alerts.isAlertActive(Alerts.Alert.PC_MAINT)) {
            pcMaintCard?.setCardBackgroundColor(pcEnabledColor)
        } else {
            pcMaintCard?.setCardBackgroundColor(disabledColor)
        }

        if (Alerts.isAlertActive(Alerts.Alert.PC_UPDATE)) {
            pcUpdateCard?.setCardBackgroundColor(pcEnabledColor)
        } else {
            pcUpdateCard?.setCardBackgroundColor(disabledColor)
        }

        if (Alerts.isAlertActive(Alerts.Alert.XBOX_MAINT)) {
            xboMaintCard?.setCardBackgroundColor(enabledColor)
        } else {
            xboMaintCard?.setCardBackgroundColor(disabledColor)
        }

        if (Alerts.isAlertActive(Alerts.Alert.XBOX_UPDATE)) {
            xboxUpdateCard?.setCardBackgroundColor(enabledColor)
        } else {
            xboxUpdateCard?.setCardBackgroundColor(disabledColor)
        }

        if (Alerts.isAlertActive(Alerts.Alert.MAJOR_NEWS)) {
            pubgUpdateMajorCard?.setCardBackgroundColor(orangeColor)
        } else {
            pubgUpdateMajorCard?.setCardBackgroundColor(disabledColor)
        }

        if (Alerts.isAlertActive(Alerts.Alert.PS4_MAINT)) {
            ps4MainCard?.setCardBackgroundColor(ps4EnabledColor)
        } else {
            ps4MainCard?.setCardBackgroundColor(disabledColor)
        }

        if (Alerts.isAlertActive(Alerts.Alert.PS4_UPDATE)) {
            ps4UpdateCard?.setCardBackgroundColor(ps4EnabledColor)
        } else {
            ps4UpdateCard?.setCardBackgroundColor(disabledColor)
        }

        pcMaintCard?.setOnClickListener {
            if (!Alerts.isAlertActive(Alerts.Alert.PC_MAINT)) {
                pcMaintCard?.setCardBackgroundColor(pcEnabledColor)
                Alerts.setAlertActive(Alerts.Alert.PC_MAINT, true)
                mFCM.subscribeToTopic(Alerts.Alert.PC_MAINT.tag).addOnFailureListener {
                    Alerts.setAlertActive(Alerts.Alert.PC_MAINT, false)
                    pcMaintCard?.setCardBackgroundColor(disabledColor)
                }
            } else {
                pcMaintCard?.setCardBackgroundColor(disabledColor)
                Alerts.setAlertActive(Alerts.Alert.PC_MAINT, false)
                mFCM.unsubscribeFromTopic(Alerts.Alert.PC_MAINT.tag).addOnFailureListener {
                    pcMaintCard?.setCardBackgroundColor(pcEnabledColor)
                    Alerts.setAlertActive(Alerts.Alert.PC_MAINT, true)
                }
            }
        }

        pcUpdateCard?.setOnClickListener {
            if (!Alerts.isAlertActive(Alerts.Alert.PC_UPDATE)) {
                pcUpdateCard?.setCardBackgroundColor(pcEnabledColor)
                Alerts.setAlertActive(Alerts.Alert.PC_UPDATE, true)
                mFCM.subscribeToTopic(Alerts.Alert.PC_UPDATE.tag).addOnFailureListener {
                    pcUpdateCard?.setCardBackgroundColor(disabledColor)
                    Alerts.setAlertActive(Alerts.Alert.PC_UPDATE, false)
                }
            } else {
                pcUpdateCard?.setCardBackgroundColor(disabledColor)
                Alerts.setAlertActive(Alerts.Alert.PC_UPDATE, false)
                mFCM.unsubscribeFromTopic(Alerts.Alert.PC_UPDATE.tag).addOnFailureListener {
                    pcUpdateCard?.setCardBackgroundColor(pcEnabledColor)
                    Alerts.setAlertActive(Alerts.Alert.PC_UPDATE, true)
                }
            }
        }

        xboMaintCard?.setOnClickListener {
            if (!Alerts.isAlertActive(Alerts.Alert.XBOX_MAINT)) {
                xboMaintCard?.setCardBackgroundColor(enabledColor)
                Alerts.setAlertActive(Alerts.Alert.XBOX_MAINT, true)
                mFCM.subscribeToTopic(Alerts.Alert.XBOX_MAINT.tag).addOnFailureListener {
                    xboMaintCard?.setCardBackgroundColor(disabledColor)
                    Alerts.setAlertActive(Alerts.Alert.XBOX_MAINT, false)
                }
            } else {
                xboMaintCard?.setCardBackgroundColor(disabledColor)
                Alerts.setAlertActive(Alerts.Alert.XBOX_MAINT, false)
                mFCM.unsubscribeFromTopic(Alerts.Alert.XBOX_MAINT.tag).addOnFailureListener {
                    Alerts.setAlertActive(Alerts.Alert.XBOX_MAINT, true)
                    xboMaintCard?.setCardBackgroundColor(enabledColor)
                }
            }
        }

        xboxUpdateCard?.setOnClickListener {
            if (!Alerts.isAlertActive(Alerts.Alert.XBOX_UPDATE)) {
                Alerts.setAlertActive(Alerts.Alert.XBOX_UPDATE, true)
                xboxUpdateCard?.setCardBackgroundColor(enabledColor)
                mFCM.subscribeToTopic(Alerts.Alert.XBOX_UPDATE.tag).addOnFailureListener {
                    xboxUpdateCard?.setCardBackgroundColor(disabledColor)
                    Alerts.setAlertActive(Alerts.Alert.XBOX_UPDATE, false)
                }
            } else {
                xboxUpdateCard?.setCardBackgroundColor(disabledColor)
                Alerts.setAlertActive(Alerts.Alert.XBOX_UPDATE, false)
                mFCM.unsubscribeFromTopic(Alerts.Alert.XBOX_UPDATE.tag).addOnFailureListener {
                    xboxUpdateCard?.setCardBackgroundColor(enabledColor)
                    Alerts.setAlertActive(Alerts.Alert.XBOX_UPDATE, true)
                }
            }
        }

        pubgUpdateMajorCard?.setOnClickListener {
            if (!Alerts.isAlertActive(Alerts.Alert.MAJOR_NEWS)) {
                Alerts.setAlertActive(Alerts.Alert.MAJOR_NEWS, true)
                pubgUpdateMajorCard?.setCardBackgroundColor(orangeColor)
                mFCM.subscribeToTopic(Alerts.Alert.MAJOR_NEWS.tag).addOnFailureListener {
                    pubgUpdateMajorCard?.setCardBackgroundColor(disabledColor)
                    Alerts.setAlertActive(Alerts.Alert.MAJOR_NEWS, false)
                }
            } else {
                pubgUpdateMajorCard?.setCardBackgroundColor(disabledColor)
                Alerts.setAlertActive(Alerts.Alert.MAJOR_NEWS, false)
                mFCM.unsubscribeFromTopic(Alerts.Alert.MAJOR_NEWS.tag).addOnFailureListener {
                    pubgUpdateMajorCard?.setCardBackgroundColor(orangeColor)
                    Alerts.setAlertActive(Alerts.Alert.MAJOR_NEWS, true)
                }
            }
        }

        ps4MainCard?.setOnClickListener {
            if (!Alerts.isAlertActive(Alerts.Alert.PS4_MAINT)) {
                ps4MainCard?.setCardBackgroundColor(ps4EnabledColor)
                Alerts.setAlertActive(Alerts.Alert.PS4_MAINT, true)
                mFCM.subscribeToTopic(Alerts.Alert.PS4_MAINT.tag).addOnFailureListener {
                    ps4MainCard?.setCardBackgroundColor(disabledColor)
                    Alerts.setAlertActive(Alerts.Alert.PS4_MAINT, false)
                }
            } else {
                ps4MainCard?.setCardBackgroundColor(disabledColor)
                Alerts.setAlertActive(Alerts.Alert.PS4_MAINT, false)
                mFCM.unsubscribeFromTopic(Alerts.Alert.PS4_MAINT.tag).addOnFailureListener {
                    Alerts.setAlertActive(Alerts.Alert.PS4_MAINT, true)
                    ps4MainCard?.setCardBackgroundColor(ps4EnabledColor)
                }
            }
        }

        ps4UpdateCard?.setOnClickListener {
            if (!Alerts.isAlertActive(Alerts.Alert.PS4_UPDATE)) {
                Alerts.setAlertActive(Alerts.Alert.PS4_UPDATE, true)
                ps4UpdateCard?.setCardBackgroundColor(ps4EnabledColor)
                mFCM.subscribeToTopic(Alerts.Alert.PS4_UPDATE.tag).addOnFailureListener {
                    ps4UpdateCard?.setCardBackgroundColor(disabledColor)
                    Alerts.setAlertActive(Alerts.Alert.PS4_UPDATE, false)
                }
            } else {
                ps4UpdateCard?.setCardBackgroundColor(disabledColor)
                Alerts.setAlertActive(Alerts.Alert.PS4_UPDATE, false)
                mFCM.unsubscribeFromTopic(Alerts.Alert.PS4_UPDATE.tag).addOnFailureListener {
                    ps4UpdateCard?.setCardBackgroundColor(ps4EnabledColor)
                    Alerts.setAlertActive(Alerts.Alert.PS4_UPDATE, true)
                }
            }
        }
    }


    override fun onPause() {
        super.onPause()
        overridePendingTransition(R.anim.nav_default_pop_enter_anim, R.anim.nav_default_pop_exit_anim)
    }

}