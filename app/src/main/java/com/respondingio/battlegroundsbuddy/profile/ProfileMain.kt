package com.respondingio.battlegroundsbuddy.profile

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.afollestad.materialdialogs.MaterialDialog
import com.google.firebase.auth.FirebaseAuth
import com.respondingio.battlegroundsbuddy.R
import kotlinx.android.synthetic.main.fragment_about.*
import org.jetbrains.anko.appcompat.v7.navigationIconResource

class ProfileMain : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.instabug_fadein, R.anim.instabug_fadeout)
        setContentView(R.layout.fragment_about)
        setSupportActionBar(toolbar)

        toolbar.navigationIconResource = R.drawable.instabug_ic_back
        toolbar.setNavigationOnClickListener { onBackPressed() }

        val user = FirebaseAuth.getInstance().currentUser ?: return
        profileDisplayName?.text = user.displayName ?: "Battle Buddy"
        profileEmail?.text = user.email ?: "No Email"
        profilePhone?.text = user.phoneNumber ?: "No Phone Number"

        profileDisplayName?.setOnClickListener {
            //startActivityForResult(Intent(this@ProfileMain, ProfilePicPicker::class.java), 1)
        }
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