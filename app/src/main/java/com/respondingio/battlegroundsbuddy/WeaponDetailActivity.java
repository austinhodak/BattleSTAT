package com.respondingio.battlegroundsbuddy;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
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
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.google.android.gms.ads.AdView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Transaction;
import com.respondingio.battlegroundsbuddy.weapons.CompareWeaponPicker;
import com.respondingio.battlegroundsbuddy.weapons.WeaponComments;
import com.respondingio.battlegroundsbuddy.weapons.WeaponDamageChart;
import com.respondingio.battlegroundsbuddy.weapons.WeaponDetailCamera;
import com.respondingio.battlegroundsbuddy.weapons.WeaponDetailDeviation;
import com.respondingio.battlegroundsbuddy.weapons.WeaponDetailOverview;
import com.respondingio.battlegroundsbuddy.weapons.WeaponDetailRecoil;
import com.respondingio.battlegroundsbuddy.weapons.WeaponDetailSpread;
import com.respondingio.battlegroundsbuddy.weapons.WeaponDetailSway;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.mateware.snacky.Snacky;

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
            bundle.putString("weaponName", weaponName);
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

    @BindView(R.id.top_bar_linear) LinearLayout topBar;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private ListenerRegistration registration;

    private String weaponClass;

    private String weaponID;

    private String weaponKey;

    private String weaponName;

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

            weaponName = getIntent().getStringExtra("weaponName");
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
            public void onPageScrollStateChanged(final int state) {
            }

            @Override
            public void onPageScrolled(final int position, final float positionOffset, final int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(final int position) {
                if (position == 0) {
                    hideFab();
                } else {
                    showFab();
                }
            }
        });
    }

    public int convertDpToPixel(float dp) {
        Resources resources = WeaponDetailActivity.this.getResources();
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                resources.getDisplayMetrics()
        );
    }

    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.weapon_detail, menu);

        MenuItem menuItem = menu.add(0, 10, 0, "Full Damage Chart");
        menuItem.setIcon(R.drawable.ic_local_hospital_24dp);
        menuItem.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);

        menuItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(final MenuItem item) {

                Intent intent = new Intent(WeaponDetailActivity.this,
                        WeaponDamageChart.class);
                intent.putExtra("weaponPath", weaponID);
                intent.putExtra("weaponKey",
                        weaponKey);
                intent.putExtra("weaponClass", weaponClass);
                intent.putExtra("weaponName", weaponName);
                startActivity(intent);
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
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
                    public void onAnimationCancel(final Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(final Animator animation) {
                        compareFAB.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationRepeat(final Animator animation) {

                    }

                    @Override
                    public void onAnimationStart(final Animator animation) {

                    }
                })
                .start();
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadWeapon(weaponID);
    }

    private void loadWeapon(final String weaponID) {
        topBar.removeViews(1, topBar.getChildCount() - 1);
        registration = db.document(weaponID).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable final DocumentSnapshot data, @Nullable final FirebaseFirestoreException e) {
                if (data != null && data.exists()) {

                    if (data.contains("airDropOnly") && data.get("airDropOnly") != null) {
                        if (data.getBoolean("airDropOnly")) {
                            ImageView imageView = new ImageView(WeaponDetailActivity.this);

                            LayoutParams params = new LayoutParams(convertDpToPixel(24), convertDpToPixel(24));

                            params.setMargins(convertDpToPixel(8), 0, 0, 0);

                            imageView.setLayoutParams(params);
                            //imageView.setBackground(getResources().getDrawable(R.drawable.chip_green_outline));
                            imageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_parachute));
                            //imageView.setPadding(convertDpToPixel(4), convertDpToPixel(4), convertDpToPixel(4), convertDpToPixel(4));

                            if (VERSION.SDK_INT >= VERSION_CODES.O) {
                                imageView.setTooltipText("Air Drop Only");
                            }

                            topBar.addView(imageView);
                        } else {
                        }
                    }

                    if (data.contains("miramar_only") && data.get("miramar_only") != null) {
                        if (data.getBoolean("miramar_only")) {
                            ImageView imageView = new ImageView(WeaponDetailActivity.this);

                            LayoutParams params = new LayoutParams(convertDpToPixel(24), convertDpToPixel(24));

                            params.setMargins(convertDpToPixel(8), 0, 0, 0);

                            imageView.setLayoutParams(params);
                            //imageView.setBackground(getResources().getDrawable(R.drawable.chip_tan_outline));
                            imageView.setImageDrawable(getResources().getDrawable(R.drawable.cactu));
                            //imageView.setPadding(convertDpToPixel(4), convertDpToPixel(4), convertDpToPixel(4), convertDpToPixel(4));

                            if (VERSION.SDK_INT >= VERSION_CODES.O) {
                                imageView.setTooltipText("Miramar Only");
                            }

                            topBar.addView(imageView);
                        }
                    }

                    if (data.contains("bestInClass") && data.get("bestInClass") != null) {
                        if (data.getBoolean("bestInClass")) {
                            ImageView imageView = new ImageView(WeaponDetailActivity.this);

                            LayoutParams params = new LayoutParams(convertDpToPixel(24), convertDpToPixel(24));

                            params.setMargins(convertDpToPixel(8), 0, 0, 0);

                            imageView.setLayoutParams(params);
                            //imageView.setBackground(getResources().getDrawable(R.drawable.chip_blue_outine));
                            imageView.setImageDrawable(getResources().getDrawable(R.drawable.icons8_trophy));
                            //imageView.setPadding(convertDpToPixel(6), convertDpToPixel(6), convertDpToPixel(6), convertDpToPixel(6));

                            if (VERSION.SDK_INT >= VERSION_CODES.O) {
                                imageView.setTooltipText("Best In Class");
                            }

                            topBar.addView(imageView);
                        }
                    }

                    if (data.contains("sanhok_only") && data.get("sanhok_only") != null) {
                        if (data.getBoolean("sanhok_only")) {
                            ImageView imageView = new ImageView(WeaponDetailActivity.this);

                            LayoutParams params = new LayoutParams(convertDpToPixel(24), convertDpToPixel(24));

                            params.setMargins(convertDpToPixel(8), 0, 0, 0);

                            imageView.setLayoutParams(params);
                            //imageView.setBackground(getResources().getDrawable(R.drawable.chip_red_outline));
                            imageView.setImageDrawable(getResources().getDrawable(R.drawable.sanhok_icon));
                            //imageView.setPadding(convertDpToPixel(6), convertDpToPixel(6), convertDpToPixel(6), convertDpToPixel(6));

                            if (VERSION.SDK_INT >= VERSION_CODES.O) {
                                imageView.setTooltipText("Sanhok Only");
                            }

                            topBar.addView(imageView);
                        }
                    }
                    stopListener();
                }
            }
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
                        public void onAnimationCancel(final Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(final Animator animation) {
                            compareFAB.setVisibility(View.VISIBLE);
                            compareFAB.setScaleX(1.f);
                            compareFAB.setScaleY(1.f);
                        }

                        @Override
                        public void onAnimationRepeat(final Animator animation) {

                        }

                        @Override
                        public void onAnimationStart(final Animator animation) {

                        }
                    })
                    .start();
        }
    }

    private void stopListener() {
        registration.remove();
    }
}
