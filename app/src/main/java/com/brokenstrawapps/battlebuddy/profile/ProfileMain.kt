package com.brokenstrawapps.battlebuddy.profile

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.WhichButton
import com.afollestad.materialdialogs.actions.setActionButtonEnabled
import com.afollestad.materialdialogs.input.getInputField
import com.afollestad.materialdialogs.input.input
import com.google.firebase.auth.FirebaseAuth
import com.brokenstrawapps.battlebuddy.R
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.android.synthetic.main.fragment_about.*
import org.jetbrains.anko.appcompat.v7.navigationIconResource

class ProfileMain : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.fragment_fade_enter, R.anim.fragment_fade_exit)
        setContentView(R.layout.fragment_about)
        setSupportActionBar(toolbar)

        toolbar.navigationIconResource = R.drawable.ic_arrow_back_24dp
        toolbar.setNavigationOnClickListener { onBackPressed() }

        val user = FirebaseAuth.getInstance().currentUser ?: return
        updateProfile()

        profileNameCard?.setOnClickListener {
            MaterialDialog(this).show {
                title(text = "Update Display Name")
                noAutoDismiss()
                input(prefill = user.displayName ?: "") { dialog, text ->
                    val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(text.toString())
                            .build()

                    dialog.setActionButtonEnabled(WhichButton.POSITIVE, false)
                    dialog.positiveButton(text = "SAVING")
                    user.updateProfile(profileUpdates).addOnSuccessListener {
                        dialog.dismiss()
                        updateProfile()
                    }.addOnFailureListener {
                        dialog.setActionButtonEnabled(WhichButton.POSITIVE, true)
                        dialog.positiveButton(text = "SAVE")
                        dialog.getInputField()?.error = "Error updating display name."
                    }
                }
                positiveButton(text = "SAVE")
            }
        }
    }

    private fun updateProfile() {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        profileDisplayName?.text = user.displayName ?: "BattleSTAT"
        profileEmail?.text = user.email ?: "No Email"
        profilePhone?.text = user.phoneNumber ?: "No Phone Number"
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.profile_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.logoutUser -> {
                MaterialDialog(this)
                        .title(text = getString(R.string.are_you_sure))
                        .message(text = getString(R.string.sign_out))
                        .positiveButton(text = getString(R.string.sign_out_text)) {
                            FirebaseAuth.getInstance().signOut()
                            finish()
                        }
                        .negativeButton(text = getString(R.string.nevermind)) {
                            it.dismiss()
                        }.show()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}