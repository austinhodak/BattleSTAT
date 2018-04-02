package com.austinhodak.pubgcenter.loadout;

import android.os.Bundle;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.austinhodak.pubgcenter.R;

public class LoadoutCreateMain extends AppCompatActivity {

    @BindView(R.id.bottom_sheet)
    NestedScrollView bottomSheet;

    @BindView(R.id.main_toolbar)
    Toolbar mToolbar;

    private BottomSheetBehavior mBottomSheetBehavior;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loadout_create_main);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        setTitle("Loadout Creator");

        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        mBottomSheetBehavior.setHideable(true);


    }
}
