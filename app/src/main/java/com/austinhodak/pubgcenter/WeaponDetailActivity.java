package com.austinhodak.pubgcenter;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.austinhodak.pubgcenter.weapons.CompareWeaponPicker;
import com.austinhodak.pubgcenter.weapons.WeaponComments;
import com.austinhodak.pubgcenter.weapons.WeaponDetailCamera;
import com.austinhodak.pubgcenter.weapons.WeaponDetailDeviation;
import com.austinhodak.pubgcenter.weapons.WeaponDetailOverview;
import com.austinhodak.pubgcenter.weapons.WeaponDetailRecoil;
import com.austinhodak.pubgcenter.weapons.WeaponDetailSpread;
import com.austinhodak.pubgcenter.weapons.WeaponDetailSway;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;
import de.mateware.snacky.Snacky;
import java.util.HashSet;
import java.util.Set;

public class WeaponDetailActivity extends AppCompatActivity {

    class ViewPagerAdapter extends FragmentPagerAdapter {

        private String title[] = {"Comments", "Overview", "Spread", "Deviation", "Recoil", "Sway", "Camera DOF"};

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
            switch (position) {
                case 0:
                    currentFragment = new WeaponComments();
                    break;
                case 1:
                    currentFragment = new WeaponDetailOverview();
                    break;
                case 2:
                    currentFragment = new WeaponDetailSpread();
                    break;
                case 3:
                    currentFragment = new WeaponDetailDeviation();
                    break;
                case 4:
                    currentFragment = new WeaponDetailRecoil();
                    break;
                case 5:
                    currentFragment = new WeaponDetailSway();
                    break;
                case 6:
                    currentFragment = new WeaponDetailCamera();
                    break;
                default:
                    currentFragment = new WeaponDetailOverview();
                    break;
            }

            Bundle bundle = new Bundle();
            bundle.putString("weaponClass", weaponClass);
            bundle.putString("weaponPath", weaponID);
            bundle.putString("weaponKey", weaponKey);
            currentFragment.setArguments(bundle);
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
    CoordinatorLayout mRelativeLayout;

    SharedPreferences mSharedPreferences;

    @BindView(R.id.weapon_detail_toolbar)
    Toolbar mToolbar;

    @BindView(R.id.toolbar_title)
    TextView title;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private String weaponClass;

    private String weaponID;

    private String weaponKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weapon_detail_activity);
        ButterKnife.bind(this);

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
            weaponKey = getIntent().getStringExtra("weaponKey");

            String weaponName = getIntent().getStringExtra("weaponName");
            title.setText(weaponName.toUpperCase());
        }

        final ViewPager viewPager = findViewById(R.id.viewpager);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);

        final TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        viewPager.setCurrentItem(1);
        tabLayout.setScrollPosition(1, 0f, true);

        compareFAB.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View view) {
                Intent intent = new Intent(WeaponDetailActivity.this, CompareWeaponPicker.class);
                intent.putExtra("firstWeapon", weaponID);
                intent.putExtra("weapon_name", getIntent().getStringExtra("weaponName"));
                startActivity(intent);
            }
        });

        viewPager.addOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageScrolled(final int position, final float positionOffset, final int positionOffsetPixels) { }

            @Override
            public void onPageSelected(final int position) {
                if (position == 0) {
                    hideFab();
                } else {
                    showFab();
                }
            }

            @Override
            public void onPageScrollStateChanged(final int state) { }
        });
    }

    private void showFab() {
        if (compareFAB.getVisibility() == View.GONE) {
            compareFAB.setVisibility(View.VISIBLE);
            compareFAB.setScaleX(0.f);
            compareFAB.setScaleY(0.f);
            compareFAB.animate()
                    .scaleX(1.f).scaleY(1.f)
                    .setDuration(200)
                    .setListener(new AnimatorListener() {
                        @Override
                        public void onAnimationStart(final Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(final Animator animation) {
                            compareFAB.setVisibility(View.VISIBLE);
                            compareFAB.setScaleX(1.f);
                            compareFAB.setScaleY(1.f);
                        }

                        @Override
                        public void onAnimationCancel(final Animator animation) {

                        }

                        @Override
                        public void onAnimationRepeat(final Animator animation) {

                        }
                    })
                    .start();
        }
    }

    private void hideFab() {
        if (compareFAB.getVisibility() == View.GONE) {
            return;
        }
        compareFAB.setScaleX(1.f);
        compareFAB.setScaleY(1.f);
        compareFAB.animate()
                .scaleX(0.f).scaleY(0.f)
                .setDuration(200)
                .setListener(new AnimatorListener() {
                    @Override
                    public void onAnimationStart(final Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(final Animator animation) {
                        compareFAB.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationCancel(final Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(final Animator animation) {

                    }
                })
                .start();
    }

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

                    Snacky.builder().setView(mRelativeLayout).setText("Added to favorites").build().show();

                } else {
                    item.setIcon(R.drawable.ic_star_border_24dp);

                    Set<String> favs = mSharedPreferences.getStringSet("favoriteWeapons", null);
                    if (favs != null && favs.contains(weaponID)) {
                        favs.remove(weaponID);

                        mSharedPreferences.edit().remove("favoriteWeapons").apply();
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
                        public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
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

                            Snacky.builder().setView(mRelativeLayout).setText("Error Liking Weapon").error();
                            item.setIcon(R.drawable.ic_favorite_border_24dp);
                        }
                    });

                } else {
                    item.setIcon(R.drawable.ic_favorite_border_24dp);

                    db.runTransaction(new Transaction.Function<Void>() {
                        @Override
                        public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
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
            case R.id.compare_menu:
                Intent intent = new Intent(WeaponDetailActivity.this, CompareWeaponPicker.class);
                intent.putExtra("firstWeapon", weaponID);
                intent.putExtra("weapon_name", getIntent().getStringExtra("weaponName"));
                startActivity(intent);
                break;
        }
        return false;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        final MenuItem alertMenuItem = menu.findItem(R.id.favorite_weapon);

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

        if (VERSION.SDK_INT < VERSION_CODES.LOLLIPOP) {
            MenuItem compare = menu.findItem(R.id.compare_menu);
            compare.setVisible(true);

            compareFAB.setVisibility(View.GONE);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
