package com.respondingio.battlegroundsbuddy.loadout;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.respondingio.battlegroundsbuddy.R;

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
