package com.brokenstrawapps.battlebuddy.weapons;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.firestore.FirebaseFirestore;
import com.brokenstrawapps.battlebuddy.R;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.widget.NestedScrollView;
import butterknife.BindView;
import butterknife.ButterKnife;

public class WeaponDamageChart extends AppCompatActivity {

    @BindView(R.id.bottom_sheet)
    NestedScrollView bottomSheet;

    int buttonSelected = 0;

    @BindView(R.id.button_damage) Button damageSelector;

    @BindView(R.id.damage_helmet_radios) RadioGroup helmetRadios;

    @BindView(R.id.button_htk) Button hitsSelector;

    @BindView(R.id.main_toolbar)
    Toolbar mToolbar;

    @BindView(R.id.dmg_shotgun_disc) CardView shotgunDisclaimer;

    @BindView(R.id.toolbar_title) TextView title;

    @BindView(R.id.damage_vest_radios) RadioGroup vestRadios;

    @BindView(R.id.dmg_head) LinearLayout layoutHead;

    @BindView(R.id.dmg_neck) LinearLayout layoutNeck;

    @BindView(R.id.dmg_upshld) LinearLayout layoutShoulder;

    @BindView(R.id.dmg_upperchest) LinearLayout layoutUpChest;

    @BindView(R.id.dmg_midchest) LinearLayout layoutMidChest;

    @BindView(R.id.dmg_upperstomach) LinearLayout layoutUpStomach;

    @BindView(R.id.dmg_lowerstomach) LinearLayout layoutLowStomach;

    @BindView(R.id.dmg_upperarm) LinearLayout layoutUpArm;

    @BindView(R.id.dmg_lowerarm) LinearLayout layoutLowArm;

    @BindView(R.id.dmg_hand) LinearLayout layoutHand;

    @BindView(R.id.dmg_upleg) LinearLayout layoutUpLeg;

    @BindView(R.id.dmg_lowleg) LinearLayout layoutLowLeg;

    @BindView(R.id.dmg_foot) LinearLayout layoutFoot;

    @BindView(R.id.dmg_base) TextView baseDMGText;

    private double baseDamage;

    private double baseHeadDamage;

    private double classModBody = 1;

    private double classModHead = 1;

    private double classModLegs = 1;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private double helmetModifier = 0;

    private BottomSheetBehavior mBottomSheetBehavior;

    private double vestModifier = 0;

    private String weaponClass;

    private String weaponID;

    private String weaponKey;

    private static double MOD_HEAD = 1;
    private static double MOD_NECK = 0.75;
    private static double MOD_UPSHLD = 1;
    private static double MOD_HEART = 1.1;
    private static double MOD_MIDCHST = 1;
    private static double MOD_UPSTMCH = 0.9;
    private static double MOD_LWRSTMCH = 1;
    private static double MOD_UPLIMB = 0.6;
    private static double MOD_LOWLIMB = 0.5;
    private static double MOD_HAND = 0.3;
    private static double MOD_UPLEG = 0.5;
    private static double MOD_LOWLEG = 0.5;
    private static double MOD_FOOT = 0.3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weapon_damage_chart);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        setTitle(null);

        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        mBottomSheetBehavior.setHideable(false);
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (getIntent() != null) {
            weaponID = getIntent().getStringExtra("weaponPath");
            weaponClass = getIntent().getStringExtra("weaponClass");
            weaponKey = getIntent().getStringExtra("weaponKey");


            setClassStats(weaponClass, weaponKey);
            loadWeapon(weaponID);

            String weaponName = getIntent().getStringExtra("weaponName");
            title.setText(weaponName.toUpperCase());
        }

        damageSelector.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (buttonSelected == 1) {
                    damageSelector.setTextColor(getResources().getColor(R.color.md_deep_orange_500));
                    hitsSelector.setTextColor(getResources().getColor(R.color.md_black_1000));
                }

                buttonSelected = 0;

            }
        });
        hitsSelector.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (buttonSelected == 0) {
                    hitsSelector.setTextColor(getResources().getColor(R.color.md_deep_orange_500));
                    damageSelector.setTextColor(getResources().getColor(R.color.md_black_1000));
                }

                buttonSelected = 1;

            }
        });

        helmetRadios.check(((RadioButton) helmetRadios.getChildAt(0)).getId());
        vestRadios.check(((RadioButton) vestRadios.getChildAt(0)).getId());

        helmetRadios.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(final RadioGroup group, final int checkedId) {
                switch (checkedId) {
                    case R.id.radio_helmet_none:
                        helmetModifier = 0;
                        break;
                    case R.id.radio_helmet_1:
                        helmetModifier = 0.30;
                        break;
                    case R.id.radio_helmet_2:
                        helmetModifier = 0.40;
                        break;
                    case R.id.radio_helmet_3:
                        helmetModifier = 0.55;
                        break;
                }

                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                updateStats();
            }
        });

        vestRadios.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(final RadioGroup group, final int checkedId) {
                switch (checkedId) {
                    case R.id.radio_vest_none:
                        vestModifier = 0;
                        break;
                    case R.id.radio_vest_1:
                        vestModifier = 0.30;
                        break;
                    case R.id.radio_vest_2:
                        vestModifier = 0.40;
                        break;
                    case R.id.radio_vest_3:
                        vestModifier = 0.55;
                        break;
                }

                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                updateStats();
            }
        });
    }

    private void loadWeapon(final String weaponID) {

        baseDamage = getIntent().getDoubleExtra("damageBody", 0.0);
        baseHeadDamage = getIntent().getDoubleExtra("damageHead", 0.0);
        baseDMGText.setText(String.valueOf(getIntent().getDoubleExtra("damageBody", 0.0)));
        updateStats();

//        db.document(weaponID).addSnapshotListener(new EventListener<DocumentSnapshot>() {
//            @Override
//            public void onEvent(@Nullable final DocumentSnapshot documentSnapshot, @Nullable final FirebaseFirestoreException e) {
//                if (documentSnapshot == null || !documentSnapshot.exists()) {
//                    return;
//                }
//
//                if (documentSnapshot.contains("damageBody0")) {
//
//                }
//            }
//        });
    }

    private void setClassStats(String weaponClass, String weaponKey) {
        if (weaponKey == "sks" || weaponKey == "mk14" || weaponKey == "YTmmvZ6zaOq94Qz0HXKq" || weaponKey == "" || weaponKey == "slr") {
            classModHead = 2.3;
            classModBody = 1;
            classModLegs = 0.95;
            return;
        }

        switch (weaponClass) {
            case "lmgs":
            case "assault_rifles":
                classModHead = 2.3;
                classModBody = 1;
                classModLegs = 0.90;
                break;
            case "melee":
                classModHead = 1.5;
                classModBody = 1;
                classModLegs = 1.2;
                break;
            case "pistols":
                classModHead = 2;
                classModBody = 1;
                classModLegs = 1;
                break;
            case "shotguns":
                classModHead = 1.5;
                classModBody = 1;
                classModLegs = 0.75;
                shotgunDisclaimer.setVisibility(View.VISIBLE);
                break;
            case "smgs":
                classModHead = 1.8;
                classModBody = 1;
                classModLegs = 1;
                break;
            case "sniper_rifles":
                classModHead = 2.5;
                classModBody = 1.1;
                classModLegs = 0.95;
                break;
            default:
                classModHead = 1;
                classModBody = 1;
                classModLegs = 1;
        }
    }

    private void updateStats() {
        //HEAD
        double headDMG = baseHeadDamage * MOD_HEAD * classModHead - (baseHeadDamage * MOD_HEAD * classModHead * helmetModifier);
        ((TextView) layoutHead.getChildAt(1)).setText(String.format("%.1f", headDMG));
        ((TextView) layoutHead.getChildAt(2)).setText(String.format("%.0f", Math.ceil(100/headDMG)));
        updateHTKColor(layoutHead, (int) Math.ceil(100/headDMG));

        //NECK
        double neckDMG = baseHeadDamage * MOD_NECK * classModHead - (baseHeadDamage * MOD_NECK * classModHead * helmetModifier);
        ((TextView) layoutNeck.getChildAt(1)).setText(String.format("%.1f", neckDMG));
        ((TextView) layoutNeck.getChildAt(2)).setText(String.format("%.0f", Math.ceil(100/neckDMG)));
        updateHTKColor(layoutNeck, (int) Math.ceil(100/neckDMG));

        //SHOUlDER
        double shoulderDMG = baseDamage * MOD_UPSHLD * classModBody - (baseDamage * MOD_UPSHLD * classModBody * vestModifier);
        ((TextView) layoutShoulder.getChildAt(1)).setText(String.format("%.1f", shoulderDMG));
        ((TextView) layoutShoulder.getChildAt(2)).setText(String.format("%.0f", Math.ceil(100/shoulderDMG)));
        updateHTKColor(layoutShoulder, (int) Math.ceil(100/shoulderDMG));

        //UPPER CHEST
        double upchestDMG = baseDamage * MOD_HEART * classModBody - (baseDamage * MOD_HEART * classModBody * vestModifier);
        ((TextView) layoutUpChest.getChildAt(1)).setText(String.format("%.1f", upchestDMG));
        ((TextView) layoutUpChest.getChildAt(2)).setText(String.format("%.0f", Math.ceil(100/upchestDMG)));
        updateHTKColor(layoutUpChest, (int) Math.ceil(100/upchestDMG));

        //LOWER CHEST
        double lowChestDMG = baseDamage * MOD_MIDCHST * classModBody - (baseDamage * MOD_MIDCHST * classModBody * vestModifier);
        ((TextView) layoutMidChest.getChildAt(1)).setText(String.format("%.1f", lowChestDMG));
        ((TextView) layoutMidChest.getChildAt(2)).setText(String.format("%.0f", Math.ceil(100/lowChestDMG)));
        updateHTKColor(layoutMidChest, (int) Math.ceil(100/lowChestDMG));

        //UPPER STOMACH
        double upStomachDMG = baseDamage * MOD_UPSTMCH * classModBody - (baseDamage * MOD_UPSTMCH * classModBody * vestModifier);
        ((TextView) layoutUpStomach.getChildAt(1)).setText(String.format("%.1f", upStomachDMG));
        ((TextView) layoutUpStomach.getChildAt(2)).setText(String.format("%.0f", Math.ceil(100/upStomachDMG)));
        updateHTKColor(layoutUpStomach, (int) Math.ceil(100/upStomachDMG));

        //LOWER STOMACH
        double lowStomachDMG = baseDamage * MOD_LWRSTMCH * classModBody - (baseDamage * MOD_LWRSTMCH * classModBody * vestModifier);
        ((TextView) layoutLowStomach.getChildAt(1)).setText(String.format("%.1f", lowStomachDMG));
        ((TextView) layoutLowStomach.getChildAt(2)).setText(String.format("%.0f", Math.ceil(100/lowStomachDMG)));
        updateHTKColor(layoutLowStomach, (int) Math.ceil(100/lowStomachDMG));

        //UPPER ARM
        double upArmDMG = baseDamage * MOD_UPLIMB * classModLegs;
        ((TextView) layoutUpArm.getChildAt(1)).setText(String.format("%.1f", upArmDMG));
        ((TextView) layoutUpArm.getChildAt(2)).setText(String.format("%.0f", Math.ceil(100/upArmDMG)));
        updateHTKColor(layoutUpArm, (int) Math.ceil(100/upArmDMG));

        //LOWER ARM
        double lowArmDMG = baseDamage * MOD_LOWLIMB * classModLegs;
        ((TextView) layoutLowArm.getChildAt(1)).setText(String.format("%.1f", lowArmDMG));
        ((TextView) layoutLowArm.getChildAt(2)).setText(String.format("%.0f", Math.ceil(100/lowArmDMG)));
        updateHTKColor(layoutLowArm, (int) Math.ceil(100/lowArmDMG));

        //HAND
        double handDMG = baseDamage * MOD_HAND * classModLegs;
        ((TextView) layoutHand.getChildAt(1)).setText(String.format("%.1f", handDMG));
        ((TextView) layoutHand.getChildAt(2)).setText(String.format("%.0f", Math.ceil(100/handDMG)));
        updateHTKColor(layoutHand, (int) Math.ceil(100/handDMG));

        //UPPER LEG
        double upLegDMG = baseDamage * MOD_UPLEG * classModLegs;
        ((TextView) layoutUpLeg.getChildAt(1)).setText(String.format("%.1f", upLegDMG));
        ((TextView) layoutUpLeg.getChildAt(2)).setText(String.format("%.0f", Math.ceil(100/upLegDMG)));
        updateHTKColor(layoutUpLeg, (int) Math.ceil(100/upLegDMG));

        //LOWER LEG
        double lowLegDMG = baseDamage * MOD_LOWLEG * classModLegs;
        ((TextView) layoutLowLeg.getChildAt(1)).setText(String.format("%.1f", lowLegDMG));
        ((TextView) layoutLowLeg.getChildAt(2)).setText(String.format("%.0f", Math.ceil(100/lowLegDMG)));
        updateHTKColor(layoutLowLeg, (int) Math.ceil(100/lowLegDMG));

        //FOOT
        double footDMG = baseDamage * MOD_FOOT * classModLegs;
        ((TextView) layoutFoot.getChildAt(1)).setText(String.format("%.1f", footDMG));
        ((TextView) layoutFoot.getChildAt(2)).setText(String.format("%.0f", Math.ceil(100/footDMG)));
        updateHTKColor(layoutFoot, (int) Math.ceil(100/footDMG));
    }

    private void updateHTKColor(LinearLayout linearLayout, int HTK) {
        //TextView textView = (TextView) linearLayout.getChildAt(2);
        LinearLayout textView = linearLayout;
        switch (HTK) {
            case 1:
                textView.setBackgroundColor(getResources().getColor(R.color.md_green_500));
                break;
            case 2:
                textView.setBackgroundColor(getResources().getColor(R.color.md_green_400));
                break;
            case 3:
                textView.setBackgroundColor(getResources().getColor(R.color.md_light_green_500));
                break;
            case 4:
                textView.setBackgroundColor(getResources().getColor(R.color.md_yellow_600));
                break;
            case 5:
                textView.setBackgroundColor(getResources().getColor(R.color.md_yellow_800));
                break;
            case 6:
                textView.setBackgroundColor(getResources().getColor(R.color.md_orange_500));
                break;
            case 7:
                textView.setBackgroundColor(getResources().getColor(R.color.md_orange_700));
                break;
            case 8:
                textView.setBackgroundColor(getResources().getColor(R.color.md_red_400));
                break;
            case 9:
                textView.setBackgroundColor(getResources().getColor(R.color.md_red_500));
                break;
                default:
                    textView.setBackgroundColor(getResources().getColor(R.color.md_red_700));
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
