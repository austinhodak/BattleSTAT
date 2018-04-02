package com.austinhodak.pubgcenter.weapons;

import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.appodeal.ads.Appodeal;
import com.austinhodak.pubgcenter.R;
import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import de.mateware.snacky.Snacky;
import java.util.ArrayList;
import java.util.List;
import net.idik.lib.slimadapter.SlimAdapter;
import net.idik.lib.slimadapter.SlimInjector;
import net.idik.lib.slimadapter.viewinjector.IViewInjector;

public class CompareWeaponActivity extends AppCompatActivity {

    private class Weapons {

        private Damage mDamage;

        private Overview mOverview;

        public Damage getDamage() {
            return mDamage;
        }

        public void setDamage(final Damage damage) {
            mDamage = damage;
        }

        public Overview getOverview() {
            return mOverview;
        }

        public void setOverview(final Overview overview) {
            mOverview = overview;
        }
    }

    private class Overview {

        private DocumentSnapshot weapon1;

        private DocumentSnapshot weapon2;

        public DocumentSnapshot getWeapon1() {
            return weapon1;
        }

        public void setWeapon1(final DocumentSnapshot weapon1) {
            this.weapon1 = weapon1;
        }

        public DocumentSnapshot getWeapon2() {
            return weapon2;
        }

        public void setWeapon2(final DocumentSnapshot weapon2) {
            this.weapon2 = weapon2;
        }
    }

    private class Damage {

        private DocumentSnapshot weapon1;

        private DocumentSnapshot weapon2;

        public DocumentSnapshot getWeapon1() {
            return weapon1;
        }

        public void setWeapon1(final DocumentSnapshot weapon1) {
            this.weapon1 = weapon1;
        }

        public DocumentSnapshot getWeapon2() {
            return weapon2;
        }

        public void setWeapon2(final DocumentSnapshot weapon2) {
            this.weapon2 = weapon2;
        }
    }

    List<Object> dataList = new ArrayList<>();

    @BindView(R.id.adView)
    AdView mAdView;

    @BindView(R.id.rv)
    RecyclerView mRecyclerView;

    @BindView(R.id.main_toolbar)
    Toolbar mToolbar;

    @BindView(R.id.toolbar_title)
    TextView title;

    @BindView(R.id.weapon1Image)
    ImageView weapon1Image;

    @BindView(R.id.weapon1Name)
    TextView weapon1Name;

    @BindView(R.id.weapon2Image)
    ImageView weapon2Image;

    @BindView(R.id.weapon2Name)
    TextView weapon2Name;

    private SlimAdapter adapter;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private FirebaseAnalytics mFirebaseAnalytics;

    private SharedPreferences mSharedPreferences;

    private FirebaseStorage storage = FirebaseStorage.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compare_weapon);
        ButterKnife.bind(this);
        Appodeal.setBannerViewId(R.id.appodealBannerView);

        Typeface phosphate = Typeface.createFromAsset(getAssets(), "fonts/Phosphate-Solid.ttf");
        title.setTypeface(phosphate);
        title.setText("Comparing Weapons");

        mSharedPreferences = this.getSharedPreferences("com.austinhodak.pubgcenter", MODE_PRIVATE);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //setTitle("Comparing Weapons");

        setupAdapter();
        mRecyclerView.setNestedScrollingEnabled(false);

        if (getIntent() != null && getIntent().hasExtra("firstWeapon") && getIntent().hasExtra("secondWeapon")) {
            String firstWeapon = getIntent().getStringExtra("firstWeapon");
            String secondWeapon = getIntent().getStringExtra("secondWeapon");

            loadWeapons(firstWeapon, secondWeapon);
        } else {
            Snacky.builder().setActivity(this).setDuration(Snacky.LENGTH_INDEFINITE).error()
                    .setText("Didn't receive data to load, try again.").setAction("CLOSE", new OnClickListener() {
                @Override
                public void onClick(final View view) {
                    onBackPressed();
                }
            }).show();
        }

        if (!mSharedPreferences.getBoolean("removeAds", false)) {
            //loadAds();
            Appodeal.show(this, Appodeal.BANNER_VIEW);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Glide.with(getApplicationContext()).pauseRequests();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }


    private void loadAds() {
        mAdView.setVisibility(View.GONE);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(final int i) {
                super.onAdFailedToLoad(i);
                mAdView.setVisibility(View.GONE);
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                mAdView.setVisibility(View.VISIBLE);
            }
        });
    }

    private void loadTopCard1(final DocumentSnapshot documentSnapshot) {
        if (documentSnapshot.contains("icon")) {
            StorageReference gsReference = storage
                    .getReferenceFromUrl(documentSnapshot.getString("icon"));
            Glide.with(getApplicationContext()).using(new FirebaseImageLoader()).load(gsReference).into(weapon1Image);
        }

        if (documentSnapshot.contains("weapon_name")) {
            weapon1Name.setText(documentSnapshot.getString("weapon_name").toUpperCase());
        }
    }

    private void loadTopCard2(final DocumentSnapshot documentSnapshot) {
        if (documentSnapshot.contains("icon")) {
            StorageReference gsReference = storage
                    .getReferenceFromUrl(documentSnapshot.getString("icon"));
            Glide.with(getApplicationContext()).using(new FirebaseImageLoader()).load(gsReference).into(weapon2Image);
        }

        if (documentSnapshot.contains("weapon_name")) {
            weapon2Name.setText(documentSnapshot.getString("weapon_name").toUpperCase());
        }
    }

    private void loadWeapons(final String firstWeapon, final String secondWeapon) {
        final Weapons weapons = new Weapons();
        final Overview overview = new Overview();
        final Damage damage = new Damage();

        db.document(firstWeapon).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(final DocumentSnapshot documentSnapshot, final FirebaseFirestoreException e) {
                if (!documentSnapshot.exists()) {
                    Snacky.builder().setActivity(CompareWeaponActivity.this).setText("Error Loading").error().show();
                    return;
                }

                loadTopCard1(documentSnapshot);

                overview.setWeapon1(documentSnapshot);
                weapons.setOverview(overview);

                adapter.updateData(dataList);
                adapter.notifyDataSetChanged();
            }
        });

        db.document(firstWeapon).collection("stats").document("damage")
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(final DocumentSnapshot documentSnapshot, final FirebaseFirestoreException e) {
                        if (!documentSnapshot.exists()) {
                            //Snacky.builder().setActivity(CompareWeaponActivity.this).setText("Error Loading").error()
                            //        .show();
                            return;
                        }

                        damage.setWeapon1(documentSnapshot);
                        weapons.setDamage(damage);

                        adapter.notifyDataSetChanged();
                    }
                });

        db.document(secondWeapon).collection("stats").document("damage")
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(final DocumentSnapshot documentSnapshot, final FirebaseFirestoreException e) {
                        if (!documentSnapshot.exists()) {
                            //Snacky.builder().setActivity(CompareWeaponActivity.this).setText("Error Loading").error()
                            //        .show();
                            return;
                        }

                        damage.setWeapon2(documentSnapshot);
                        weapons.setDamage(damage);

                        adapter.notifyDataSetChanged();
                    }
                });

        db.document(secondWeapon).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(final DocumentSnapshot documentSnapshot, final FirebaseFirestoreException e) {
                if (!documentSnapshot.exists()) {
                    Snacky.builder().setActivity(CompareWeaponActivity.this).setText("Error Loading").error().show();
                    return;
                }

                loadTopCard2(documentSnapshot);

                overview.setWeapon2(documentSnapshot);
                weapons.setOverview(overview);

                adapter.notifyDataSetChanged();
            }
        });

        dataList.add(overview);
        dataList.add(damage);

    }

    private void setupAdapter() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        adapter = SlimAdapter.create().register(R.layout.compare_card_overview, new SlimInjector<Overview>() {
            @Override
            public void onInject(final Overview data, final IViewInjector injector) {
                try {
                    DocumentSnapshot weapon1 = data.getWeapon1();
                    DocumentSnapshot weapon2 = data.getWeapon2();

                    if (weapon1 != null && weapon1.exists()) {

                        if (weapon1.contains("ammo")) {
                            injector.text(R.id.ammo1, weapon1.getString("ammo"));
                        }

                        if (weapon1.contains("ammoPerMag")) {
                            injector.text(R.id.ammoMag1, weapon1.getString("ammoPerMag"));
                        }

                        if (weapon1.contains("damageBody0")) {
                            injector.text(R.id.damage1, weapon1.getString("damageBody0"));
                        }

                        if (weapon1.contains("speed")) {
                            injector.text(R.id.speed1, weapon1.getString("speed"));
                        }

                        if (weapon1.contains("power")) {
                            injector.text(R.id.power1, weapon1.getString("power"));
                        }

                        if (weapon1.contains("range")) {
                            injector.text(R.id.range1, weapon1.getString("range"));
                        }

                        if (weapon1.contains("TBS")) {
                            injector.text(R.id.tbs1, weapon1.getString("TBS"));
                        }

                        if (weapon1.contains("burstShots")) {
                            injector.text(R.id.burst1, weapon1.getString("burstShots"));
                        }

                        if (weapon1.contains("burstDelay")) {
                            injector.text(R.id.burstD1, weapon1.getString("burstDelay"));
                        }

                        if (weapon1.contains("pickupDelay")) {
                            injector.text(R.id.pickup1, weapon1.getString("pickupDelay"));
                        }

                        if (weapon1.contains("readyDelay")) {
                            injector.text(R.id.ready1, weapon1.getString("readyDelay"));
                        }

                        if (weapon1.contains("burstDelay")) {
                            injector.text(R.id.burstD1, weapon1.getString("burstDelay"));
                        }

                        if (weapon1.contains("firingModes")) {
                            injector.text(R.id.firing1, weapon1.getString("firingModes"));
                        }

                        if (weapon1.contains("reloadDurationFull")) {
                            injector.text(R.id.reload1, weapon1.getString("reloadDurationFull"));
                        }

                        if (weapon1.contains("reloadDurationTac")) {
                            injector.text(R.id.reloadTac1, weapon1.getString("reloadDurationTac"));
                        }

                        if (weapon1.contains("reloadMethod")) {
                            injector.text(R.id.reloadM1, weapon1.getString("reloadMethod"));
                        }
                    }

                    ////

                    if (weapon2 != null && weapon2.exists()) {

                        if (weapon2.contains("ammo")) {
                            injector.text(R.id.ammo2, weapon2.getString("ammo"));
                        }

                        if (weapon2.contains("ammoPerMag")) {
                            injector.text(R.id.ammoMag2, weapon2.getString("ammoPerMag"));
                        }

                        if (weapon2.contains("damageBody0")) {
                            injector.text(R.id.damage2, weapon2.getString("damageBody0"));
                        }

                        if (weapon2.contains("speed")) {
                            injector.text(R.id.speed2, weapon2.getString("speed"));
                        }

                        if (weapon2.contains("power")) {
                            injector.text(R.id.power2, weapon2.getString("power"));
                        }

                        if (weapon2.contains("range")) {
                            injector.text(R.id.range2, weapon2.getString("range"));
                        }

                        if (weapon2.contains("TBS")) {
                            injector.text(R.id.tbs2, weapon2.getString("TBS"));
                        }

                        if (weapon2.contains("burstShots")) {
                            injector.text(R.id.burst2, weapon2.getString("burstShots"));
                        }

                        if (weapon2.contains("burstDelay")) {
                            injector.text(R.id.burstD2, weapon2.getString("burstDelay"));
                        }

                        if (weapon2.contains("pickupDelay")) {
                            injector.text(R.id.pickup2, weapon2.getString("pickupDelay"));
                        }

                        if (weapon2.contains("readyDelay")) {
                            injector.text(R.id.ready2, weapon2.getString("readyDelay"));
                        }

                        if (weapon2.contains("burstDelay")) {
                            injector.text(R.id.burstD2, weapon2.getString("burstDelay"));
                        }

                        if (weapon2.contains("firingModes")) {
                            injector.text(R.id.firing2, weapon2.getString("firingModes"));
                        }

                        if (weapon2.contains("reloadDurationFull")) {
                            injector.text(R.id.reload2, weapon2.getString("reloadDurationFull"));
                        }

                        if (weapon2.contains("reloadDurationTac")) {
                            injector.text(R.id.reloadTac2, weapon2.getString("reloadDurationTac"));
                        }

                        if (weapon2.contains("reloadMethod")) {
                            injector.text(R.id.reloadM2, weapon2.getString("reloadMethod"));
                        }

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Snacky.builder().error().setText("Whoops, something went wrong.").show();
                }
            }
        }).register(R.layout.compare_card_damage, new SlimInjector<Damage>() {
            @Override
            public void onInject(final Damage data, final IViewInjector injector) {
                try {
                    DocumentSnapshot weapon1 = data.getWeapon1();
                    DocumentSnapshot weapon2 = data.getWeapon2();

                    if (weapon1 != null && weapon1.exists()) {
                        if (weapon1.contains("body0")) {
                            injector.text(R.id.vest01, weapon1.getString("body0"));
                        }

                        if (weapon1.contains("body1")) {
                            injector.text(R.id.vest11, weapon1.getString("body1"));
                        }

                        if (weapon1.contains("body2")) {
                            injector.text(R.id.vest21, weapon1.getString("body2"));
                        }

                        if (weapon1.contains("body3")) {
                            injector.text(R.id.vest31, weapon1.getString("body3"));
                        }

                        if (weapon1.contains("head0")) {
                            injector.text(R.id.helmet01, weapon1.getString("head0"));
                        }

                        if (weapon1.contains("head1")) {
                            injector.text(R.id.helmet11, weapon1.getString("head1"));
                        }

                        if (weapon1.contains("head2")) {
                            injector.text(R.id.helmet21, weapon1.getString("head2"));
                        }

                        if (weapon1.contains("head3")) {
                            injector.text(R.id.helmet31, weapon1.getString("head3"));
                        }
                    }

                    if (weapon2 != null && weapon2.exists()) {
                        if (weapon1.contains("body0")) {
                            injector.text(R.id.vest02, weapon2.getString("body0"));
                        }

                        if (weapon1.contains("body1")) {
                            injector.text(R.id.vest12, weapon2.getString("body1"));
                        }

                        if (weapon1.contains("body2")) {
                            injector.text(R.id.vest22, weapon2.getString("body2"));
                        }

                        if (weapon1.contains("body3")) {
                            injector.text(R.id.vest32, weapon2.getString("body3"));
                        }

                        if (weapon1.contains("head0")) {
                            injector.text(R.id.helmet02, weapon2.getString("head0"));
                        }

                        if (weapon1.contains("head1")) {
                            injector.text(R.id.helmet12, weapon2.getString("head1"));
                        }

                        if (weapon1.contains("head2")) {
                            injector.text(R.id.helmet22, weapon2.getString("head2"));
                        }

                        if (weapon1.contains("head3")) {
                            injector.text(R.id.helmet32, weapon2.getString("head3"));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Snacky.builder().error().setText("Whoops, something went wrong.").show();
                }
            }
        }).attachTo(mRecyclerView);
    }


}
