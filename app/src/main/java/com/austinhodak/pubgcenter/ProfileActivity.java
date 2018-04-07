package com.austinhodak.pubgcenter;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import butterknife.ButterKnife;

public class ProfileActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ButterKnife.bind(this);

        //        Glide.with(this)
        //                .load(R.drawable.header2)
        //                .asBitmap()
        //                .into(new SimpleTarget<Bitmap>(SimpleTarget.SIZE_ORIGINAL, SimpleTarget.SIZE_ORIGINAL) {
        //                    @Override
        //                    public void onResourceReady(Bitmap bitmap, GlideAnimation anim) {
        //                        mFrissonView.setBitmap(bitmap);
        //                    }
        //                });
    }
}
