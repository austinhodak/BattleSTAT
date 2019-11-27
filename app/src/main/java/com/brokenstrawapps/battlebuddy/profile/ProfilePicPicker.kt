package com.brokenstrawapps.battlebuddy.profile

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.brokenstrawapps.battlebuddy.R
import kotlinx.android.synthetic.main.activity_profile_pic_picker.*
import net.idik.lib.slimadapter.SlimAdapter
import org.jetbrains.anko.appcompat.v7.navigationIconResource
import android.graphics.drawable.Drawable



class ProfilePicPicker : AppCompatActivity() {

    var imageList: MutableList<Drawable> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.instabug_fadein, R.anim.instabug_fadeout)
        setContentView(R.layout.activity_profile_pic_picker)

        setSupportActionBar(toolbar)

        toolbar.navigationIconResource = R.drawable.instabug_ic_back
        toolbar.setNavigationOnClickListener { onBackPressed() }

        val drawablesFields = com.brokenstrawapps.battlebuddy.R.drawable::class.java.fields

        for (field in drawablesFields) {
            try {
                if (field.name.contains("ppic_")) {
                    imageList.add(resources.getDrawable(field.getInt(null)))
                    imageList.add(resources.getDrawable(field.getInt(null)))
                    imageList.add(resources.getDrawable(field.getInt(null)))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        profile_pic_rv?.layoutManager = GridLayoutManager(this, 5)
        var mAdapter = SlimAdapter.create().register<Drawable>(R.layout.icon_picker_item) { data, injector ->
            val iv = injector.findViewById<ImageView>(R.id.pic)
            Glide.with(this)
                    .load(data)
                    .apply(RequestOptions().override(128))
                    .into(iv)
        }.updateData(imageList).attachTo(profile_pic_rv)
    }
}