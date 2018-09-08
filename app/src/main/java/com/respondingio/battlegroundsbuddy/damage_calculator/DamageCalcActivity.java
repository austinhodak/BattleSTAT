package com.respondingio.battlegroundsbuddy.damage_calculator;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.respondingio.battlegroundsbuddy.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;

public class DamageCalcActivity extends AppCompatActivity {

    class ViewPagerAdapter extends FragmentPagerAdapter {

        //"Teaser \uD83D\uDE08"

        private String title[] = {"YOU", "ENEMY"};

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
            currentFragment = new DamageCalcFragment();

            bundle = new Bundle();
            bundle.putInt("youEnemy", position);

            currentFragment.setArguments(bundle);
            return currentFragment;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return title[position];
        }
    }

    @BindView(R.id.bottom_sheet)
    NestedScrollView bottomSheet;

    @BindView(R.id.bottom_main_title)
    TextView bottomTitle;

    @BindView(R.id.enemyBodyTV)
    TextView enemyBodyTV;

    @BindView(R.id.enemyHeadTV)
    TextView enemyHeadTV;

    @BindView(R.id.enemyLimbTV)
    TextView enemyLimbTV;

    DamageCalcPlayer mPlayer, mEnemy;

    @BindView(R.id.main_toolbar)
    Toolbar mToolbar;

    @BindView(R.id.toolbar_title)
    TextView title;

    @BindView(R.id.youBodyTV)
    TextView youBodyTV;

    @BindView(R.id.youHeadTV)
    TextView youHeadTV;

    @BindView(R.id.youLimbTV)
    TextView youLimbTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_damage_calc);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);

        title.setText("Damage Calculator");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        setTitle("Damage Calculator");

        final ViewPager viewPager = findViewById(R.id.viewpager);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);

        final TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        final BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setHideable(false);
        try {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    public void updateEnemyStats(DamageCalcPlayer enemy) {
        mEnemy = enemy;
        updateStats();
    }

    public void updatePlayerStats(DamageCalcPlayer player) {
        mPlayer = player;
        updateStats();
    }

    private void updateStats() {
        if (mPlayer != null && mPlayer.getWeapon() != null) {
            //Update YOU -> ENEMY
            mPlayer.getWeapon().getReference().collection("stats").document("damage").addSnapshotListener(
                    new EventListener<DocumentSnapshot>() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onEvent(final DocumentSnapshot documentSnapshot, final FirebaseFirestoreException e) {
                            if (documentSnapshot == null || !documentSnapshot.exists()) {
                                return;
                            }

                            if (mEnemy == null || mEnemy.getHelmet() == null) {
                                if (documentSnapshot.contains("head0HTK")) {
                                    if (Integer.valueOf(documentSnapshot.getString("head0HTK")) == 1) {
                                        youHeadTV.setText("Head: " + documentSnapshot.getString("head0HTK") + " Shot");
                                    } else {
                                        youHeadTV.setText("Head: " + documentSnapshot.getString("head0HTK") + " Shots");
                                    }
                                }
                            } else {
                                if (mEnemy.getHelmet().getString("name").contains("Level 1")) {
                                    if (documentSnapshot.contains("head1HTK")) {
                                        if (Integer.valueOf(documentSnapshot.getString("head1HTK")) == 1) {
                                            youHeadTV.setText("Head: " + documentSnapshot.getString("head1HTK") + " Shot");
                                        } else {
                                            youHeadTV.setText("Head: " + documentSnapshot.getString("head1HTK") + " Shots");
                                        }
                                    }
                                } else if (mEnemy.getHelmet().getString("name").contains("Level 2")) {
                                    if (documentSnapshot.contains("head2HTK")) {
                                        if (Integer.valueOf(documentSnapshot.getString("head2HTK")) == 1) {
                                            youHeadTV.setText("Head: " + documentSnapshot.getString("head2HTK") + " Shot");
                                        } else {
                                            youHeadTV.setText("Head: " + documentSnapshot.getString("head2HTK") + " Shots");
                                        }
                                    }
                                } else if (mEnemy.getHelmet().getString("name").contains("Level 3")) {
                                    if (documentSnapshot.contains("head3HTK")) {
                                        if (Integer.valueOf(documentSnapshot.getString("head3HTK")) == 1) {
                                            youHeadTV.setText("Head: " + documentSnapshot.getString("head3HTK") + " Shot");
                                        } else {
                                            youHeadTV.setText("Head: " + documentSnapshot.getString("head3HTK") + " Shots");
                                        }
                                    }
                                }
                            }

                            if (mEnemy == null || mEnemy.getVest() == null) {
                                if (documentSnapshot.contains("body0HTK")) {
                                    if (Integer.valueOf(documentSnapshot.getString("body0HTK")) == 1) {
                                        youBodyTV.setText("Body: " + documentSnapshot.getString("body0HTK") + " Shot");
                                        youLimbTV.setText("Limb: " + (Integer.valueOf(documentSnapshot.getString("body0HTK")) + 1) + " Shots");
                                    } else {
                                        youBodyTV.setText("Body: " + documentSnapshot.getString("body0HTK") + " Shots");
                                        youLimbTV.setText("Limb: " + (Integer.valueOf(documentSnapshot.getString("body0HTK")) + 1) + " Shots");
                                    }
                                }
                            } else {
                                if (mEnemy.getVest().getString("name").contains("Level 1")) {
                                    if (documentSnapshot.contains("body1HTK")) {
                                        if (Integer.valueOf(documentSnapshot.getString("body1HTK")) == 1) {
                                            youBodyTV.setText("Body: " + documentSnapshot.getString("body1HTK") + " Shot");
                                        } else {
                                            youBodyTV.setText("Body: " + documentSnapshot.getString("body1HTK") + " Shots");
                                            youLimbTV.setText("Limb: " + (Integer.valueOf(documentSnapshot.getString("body1HTK")) + 1) + " Shots");
                                        }
                                    }
                                } else if (mEnemy.getVest().getString("name").contains("Level 2")) {
                                    if (documentSnapshot.contains("body2HTK")) {
                                        if (Integer.valueOf(documentSnapshot.getString("body2HTK")) == 1) {
                                            youBodyTV.setText("Body: " + documentSnapshot.getString("body2HTK") + " Shot");
                                        } else {
                                            youBodyTV.setText("Body: " + documentSnapshot.getString("body2HTK") + " Shots");
                                            youLimbTV.setText("Limb: " + (Integer.valueOf(documentSnapshot.getString("body2HTK")) + 1) + " Shots");
                                        }
                                    }
                                } else if (mEnemy.getVest().getString("name").contains("Level 3")) {
                                    if (documentSnapshot.contains("body3HTK")) {
                                        if (Integer.valueOf(documentSnapshot.getString("body3HTK")) == 1) {
                                            youBodyTV.setText("Body: " + documentSnapshot.getString("body3HTK") + " Shot");
                                        } else {
                                            youBodyTV.setText("Body: " + documentSnapshot.getString("body3HTK") + " Shots");
                                            youLimbTV.setText("Limb: " + (Integer.valueOf(documentSnapshot.getString("body3HTK")) + 1) + " Shots");
                                        }
                                    }
                                }
                            }
                        }
                    });
        }

        if (mEnemy != null && mEnemy.getWeapon() != null) {
            mEnemy.getWeapon().getReference().collection("stats").document("damage").addSnapshotListener(
                    new EventListener<DocumentSnapshot>() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onEvent(final DocumentSnapshot documentSnapshot, final FirebaseFirestoreException e) {
                            if (documentSnapshot == null || !documentSnapshot.exists()) {
                                return;
                            }

                            if (mPlayer == null || mPlayer.getHelmet() == null) {
                                if (documentSnapshot.contains("head0HTK")) {
                                    if (Integer.valueOf(documentSnapshot.getString("head0HTK")) == 1) {
                                        enemyHeadTV.setText("Head: " + documentSnapshot.getString("head0HTK") + " Shot");
                                    } else {
                                        enemyHeadTV.setText("Head: " + documentSnapshot.getString("head0HTK") + " Shots");
                                    }
                                }
                            } else {
                                if (mPlayer.getHelmet().getString("name").contains("Level 1")) {
                                    if (documentSnapshot.contains("head1HTK")) {
                                        if (Integer.valueOf(documentSnapshot.getString("head1HTK")) == 1) {
                                            enemyHeadTV.setText("Head: " + documentSnapshot.getString("head1HTK") + " Shot");
                                        } else {
                                            enemyHeadTV.setText("Head: " + documentSnapshot.getString("head1HTK") + " Shots");
                                        }
                                    }
                                } else if (mPlayer.getHelmet().getString("name").contains("Level 2")) {
                                    if (documentSnapshot.contains("head2HTK")) {
                                        if (Integer.valueOf(documentSnapshot.getString("head2HTK")) == 1) {
                                            enemyHeadTV.setText("Head: " + documentSnapshot.getString("head2HTK") + " Shot");
                                        } else {
                                            enemyHeadTV.setText("Head: " + documentSnapshot.getString("head2HTK") + " Shots");
                                        }
                                    }
                                } else if (mPlayer.getHelmet().getString("name").contains("Level 3")) {
                                    if (documentSnapshot.contains("head3HTK")) {
                                        if (Integer.valueOf(documentSnapshot.getString("head3HTK")) == 1) {
                                            enemyHeadTV.setText("Head: " + documentSnapshot.getString("head3HTK") + " Shot");
                                        } else {
                                            enemyHeadTV.setText("Head: " + documentSnapshot.getString("head3HTK") + " Shots");
                                        }
                                    }
                                }
                            }

                            if (mPlayer == null || mPlayer.getVest() == null) {
                                if (documentSnapshot.contains("body0HTK")) {
                                    if (Integer.valueOf(documentSnapshot.getString("body0HTK")) == 1) {
                                        enemyBodyTV.setText("Body: " + documentSnapshot.getString("body0HTK") + " Shot");
                                        enemyLimbTV.setText("Limb: " + (Integer.valueOf(documentSnapshot.getString("body0HTK")) + 1) + " Shots");
                                    } else {
                                        enemyBodyTV.setText("Body: " + documentSnapshot.getString("body0HTK") + " Shots");
                                        enemyLimbTV.setText("Limb: " + (Integer.valueOf(documentSnapshot.getString("body0HTK")) + 1) + " Shots");
                                    }
                                }
                            } else {
                                if (mPlayer.getVest().getString("name").contains("Level 1")) {
                                    if (documentSnapshot.contains("body1HTK")) {
                                        if (Integer.valueOf(documentSnapshot.getString("body1HTK")) == 1) {
                                            enemyBodyTV.setText("Body: " + documentSnapshot.getString("body1HTK") + " Shot");
                                        } else {
                                            enemyBodyTV.setText("Body: " + documentSnapshot.getString("body1HTK") + " Shots");
                                            enemyLimbTV.setText("Limb: " + (Integer.valueOf(documentSnapshot.getString("body1HTK")) + 1) + " Shots");
                                        }
                                    }
                                } else if (mPlayer.getVest().getString("name").contains("Level 2")) {
                                    if (documentSnapshot.contains("body2HTK")) {
                                        if (Integer.valueOf(documentSnapshot.getString("body2HTK")) == 1) {
                                            enemyBodyTV.setText("Body: " + documentSnapshot.getString("body2HTK") + " Shot");
                                        } else {
                                            enemyBodyTV.setText("Body: " + documentSnapshot.getString("body2HTK") + " Shots");
                                            enemyLimbTV.setText("Limb: " + (Integer.valueOf(documentSnapshot.getString("body2HTK")) + 1) + " Shots");
                                        }
                                    }
                                } else if (mPlayer.getVest().getString("name").contains("Level 3")) {
                                    if (documentSnapshot.contains("body3HTK")) {
                                        if (Integer.valueOf(documentSnapshot.getString("body3HTK")) == 1) {
                                            enemyBodyTV.setText("Body: " + documentSnapshot.getString("body3HTK") + " Shot");
                                        } else {
                                            enemyBodyTV.setText("Body: " + documentSnapshot.getString("body3HTK") + " Shots");
                                            enemyLimbTV.setText("Limb: " + (Integer.valueOf(documentSnapshot.getString("body3HTK")) + 1) + " Shots");
                                        }
                                    }
                                }
                            }
                        }
                    });
        }
    }
}
