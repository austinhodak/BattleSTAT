package com.austinhodak.pubgcenter;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.appodeal.ads.Appodeal;
import com.appodeal.ads.InterstitialCallbacks;
import com.austinhodak.pubgcenter.weapons.CompareWeaponPicker;
import com.austinhodak.pubgcenter.weapons.WeaponDetailCamera;
import com.austinhodak.pubgcenter.weapons.WeaponDetailDeviation;
import com.austinhodak.pubgcenter.weapons.WeaponDetailOverview;
import com.austinhodak.pubgcenter.weapons.WeaponDetailRecoil;
import com.austinhodak.pubgcenter.weapons.WeaponDetailSpread;
import com.austinhodak.pubgcenter.weapons.WeaponDetailSway;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.storage.FirebaseStorage;
import de.mateware.snacky.Snacky;
import java.util.HashSet;
import java.util.Set;

public class WeaponDetailActivity extends AppCompatActivity {

    class ViewPagerAdapter extends FragmentPagerAdapter {

        private String title[] = {"Overview", "Spread", "Deviation", "Recoil", "Sway", "Camera DOF"};

        ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public int getCount() {
            return title.length;
        }

        @Override
        public Fragment getItem(int position) {
            Fragment currentFragment;
            Bundle bundle;
            Log.d("POS", position + "");
            switch (position) {
                case 0:
                    currentFragment = new WeaponDetailOverview();
                    bundle = new Bundle();
                    bundle.putString("weaponClass", weaponClass);
                    bundle.putString("weaponPath", weaponID);
                    currentFragment.setArguments(bundle);
                    break;
                case 1:
                    currentFragment = new WeaponDetailSpread();
                    bundle = new Bundle();
                    bundle.putString("weaponClass", weaponClass);
                    bundle.putString("weaponPath", weaponID);
                    currentFragment.setArguments(bundle);
                    break;
                case 2:
                    currentFragment = new WeaponDetailDeviation();
                    bundle = new Bundle();
                    bundle.putString("weaponClass", weaponClass);
                    bundle.putString("weaponPath", weaponID);
                    currentFragment.setArguments(bundle);
                    break;
                case 3:
                    currentFragment = new WeaponDetailRecoil();
                    bundle = new Bundle();
                    bundle.putString("weaponClass", weaponClass);
                    bundle.putString("weaponPath", weaponID);
                    currentFragment.setArguments(bundle);
                    break;
                case 4:
                    currentFragment = new WeaponDetailSway();
                    bundle = new Bundle();
                    bundle.putString("weaponClass", weaponClass);
                    bundle.putString("weaponPath", weaponID);
                    currentFragment.setArguments(bundle);
                    break;
                case 5:
                    currentFragment = new WeaponDetailCamera();
                    bundle = new Bundle();
                    bundle.putString("weaponClass", weaponClass);
                    bundle.putString("weaponPath", weaponID);
                    currentFragment.setArguments(bundle);
                    break;
                default:
                    currentFragment = new WeaponDetailOverview();
                    bundle = new Bundle();
                    bundle.putString("weaponClass", weaponClass);
                    bundle.putString("weaponPath", weaponID);
                    currentFragment.setArguments(bundle);
                    break;
            }
            return currentFragment;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return title[position];
        }
    }

    @BindView(R.id.compare_fab)
    FloatingActionButton compareFAB;

    @BindView(R.id.adView)
    AdView mAdView;

    FirebaseAnalytics mFirebaseAnalytics;

    @BindView(R.id.rl)
    FrameLayout mRelativeLayout;

    SharedPreferences mSharedPreferences;

    @BindView(R.id.weapon_detail_toolbar)
    Toolbar mToolbar;

    @BindView(R.id.toolbar_title)
    TextView title;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private FirebaseStorage storage = FirebaseStorage.getInstance();

    private String weaponClass;

    private String weaponID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weapon_detail_activity);
        ButterKnife.bind(this);
        Appodeal.setBannerViewId(R.id.appodealBannerView);

        Typeface phosphate = Typeface.createFromAsset(getAssets(), "fonts/Phosphate-Solid.ttf");
        title.setTypeface(phosphate);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mSharedPreferences = this.getSharedPreferences("com.austinhodak.pubgcenter", MODE_PRIVATE);

        setSupportActionBar(mToolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        if (getIntent() != null) {
            weaponID = getIntent().getStringExtra("weaponPath");
            weaponClass = getIntent().getStringExtra("weaponClass");

            String weaponName = getIntent().getStringExtra("weaponName");
            title.setText(weaponName.toUpperCase());
        }

        final ViewPager viewPager = findViewById(R.id.viewpager);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);

        final TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        if (!mSharedPreferences.getBoolean("removeAds", false)) {
            //loadAds();
            Appodeal.show(this, Appodeal.BANNER_VIEW);
        }

        compareFAB.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View view) {
                Intent intent = new Intent(WeaponDetailActivity.this, CompareWeaponPicker.class);
                intent.putExtra("firstWeapon", weaponID);
                intent.putExtra("weapon_name", getIntent().getStringExtra("weaponName"));
                startActivity(intent);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        Appodeal.onResume(this, Appodeal.BANNER);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (!mSharedPreferences.getBoolean("removeAds", false)) {
            int viewCount = mSharedPreferences.getInt("weaponDetailAdCount", 1);
            if (viewCount >= 5) {
                //Show interstitial
                if (Appodeal.isLoaded(Appodeal.INTERSTITIAL)) {
                    Appodeal.show(this, Appodeal.INTERSTITIAL);
                    mSharedPreferences.edit().putInt("weaponDetailAdCount", 1).apply();
                }

                Appodeal.setInterstitialCallbacks(new InterstitialCallbacks() {
                    @Override
                    public void onInterstitialClicked() {
                        Log.d("Appodeal", "onInterstitialClicked");
                    }

                    @Override
                    public void onInterstitialClosed() {
                        Log.d("Appodeal", "onInterstitialClosed");
                    }

                    @Override
                    public void onInterstitialFailedToLoad() {
                        Log.d("Appodeal", "onInterstitialFailedToLoad");
                    }

                    @Override
                    public void onInterstitialLoaded(boolean isPrecache) {
                        Log.d("Appodeal", "onInterstitialLoaded");
                    }

                    @Override
                    public void onInterstitialShown() {
                        Log.d("Appodeal", "onInterstitialShown");
                    }
                });

            } else {
                mSharedPreferences.edit().putInt("weaponDetailAdCount", viewCount + 1).apply();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.weapon_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        final DocumentReference sfDocRef = db.document(weaponID);
        switch (item.getItemId()) {
            case R.id.favorite_weapon:
                if (item.getIcon().getConstantState()
                        .equals(getResources().getDrawable(R.drawable.ic_star_border_24dp).getConstantState())) {
                    item.setIcon(R.drawable.ic_star_gold_24dp);

                    Set<String> favs = mSharedPreferences.getStringSet("favoriteWeapons", null);
                    if (favs != null) {
                        Set<String> newSet = new HashSet<>(favs);
                        newSet.add(weaponID);
                        mSharedPreferences.edit().putStringSet("favoriteWeapons", newSet).apply();
                    } else {
                        Set<String> favsNew = new HashSet<>();
                        favsNew.add(weaponID);
                        mSharedPreferences.edit().putStringSet("favoriteWeapons", favsNew).apply();
                    }

                    Snacky.builder().setActivity(this).setText("Added to favorites").build().show();

                } else {
                    item.setIcon(R.drawable.ic_star_border_24dp);

                    Set<String> favs = mSharedPreferences.getStringSet("favoriteWeapons", null);
                    if (favs != null && favs.contains(weaponID)) {
                        favs.remove(weaponID);
                        mSharedPreferences.edit().putStringSet("favoriteWeapons", favs).apply();
                    }
                }
                break;
            case R.id.heart_weapon:
                if (item.getIcon().getConstantState()
                        .equals(getResources().getDrawable(R.drawable.ic_favorite_border_24dp).getConstantState())) {
                    item.setIcon(R.drawable.ic_favorite_24dp);

                    db.runTransaction(new Transaction.Function<Void>() {
                        @Override
                        public Void apply(Transaction transaction) throws FirebaseFirestoreException {
                            DocumentSnapshot snapshot = transaction.get(sfDocRef);
                            if (snapshot.contains("likes")) {
                                double newPopulation = snapshot.getDouble("likes") + 1;
                                transaction.update(sfDocRef, "likes", newPopulation);
                            } else {
                                transaction.update(sfDocRef, "likes", 1);
                            }

                            mSharedPreferences.edit().putBoolean(weaponID + "-like", true).apply();
                            // Success
                            return null;
                        }
                    }).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("LIKE", "SUCCESS");
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("LIKE", e.getMessage());
                            Snacky.builder().setActivity(WeaponDetailActivity.this).setText("Error Liking Weapon").error();
                            item.setIcon(R.drawable.ic_favorite_border_24dp);
                        }
                    });

                } else {
                    item.setIcon(R.drawable.ic_favorite_border_24dp);

                    db.runTransaction(new Transaction.Function<Void>() {
                        @Override
                        public Void apply(Transaction transaction) throws FirebaseFirestoreException {
                            DocumentSnapshot snapshot = transaction.get(sfDocRef);
                            if (snapshot.contains("likes")) {
                                double newPopulation = snapshot.getDouble("likes") - 1;
                                transaction.update(sfDocRef, "likes", newPopulation);
                            } else {
                                transaction.update(sfDocRef, "likes", 0);
                            }

                            mSharedPreferences.edit().putBoolean(weaponID + "-like", false).apply();
                            // Success
                            return null;
                        }
                    }).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                        }
                    });
                }
                break;
        }
        return false;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        final MenuItem alertMenuItem = menu.findItem(R.id.favorite_weapon);
        FrameLayout rootView = (FrameLayout) alertMenuItem.getActionView();

        if (mSharedPreferences.getBoolean(weaponID + "-like", false)) {
            menu.findItem(R.id.heart_weapon).setIcon(R.drawable.ic_favorite_24dp);
        } else {
            menu.findItem(R.id.heart_weapon).setIcon(R.drawable.ic_favorite_border_24dp);
        }

        Set<String> favs = mSharedPreferences.getStringSet("favoriteWeapons", null);

        if (favs != null) {
            if (favs.contains(weaponID)) {
                alertMenuItem.setIcon(R.drawable.ic_star_gold_24dp);
            } else {
                alertMenuItem.setIcon(R.drawable.ic_star_border_24dp);
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
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

}
