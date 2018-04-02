package com.austinhodak.pubgcenter.weapons;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.austinhodak.pubgcenter.R;
import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.idik.lib.slimadapter.SlimAdapter;
import net.idik.lib.slimadapter.SlimInjector;
import net.idik.lib.slimadapter.viewinjector.IViewInjector;

public class CompareWeaponPicker extends AppCompatActivity {

    List<Object> data = new ArrayList<>();

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    String firstWeapon;

    @BindView(R.id.rv)
    RecyclerView mRecyclerView;

    @BindView(R.id.main_toolbar)
    Toolbar mToolbar;

    @BindView(R.id.toolbar_title)
    TextView title;

    private SlimAdapter adapter;

    private SharedPreferences mSharedPreferences;

    private FirebaseStorage storage = FirebaseStorage.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compare_weapon_picker);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Typeface phosphate = Typeface.createFromAsset(getAssets(), "fonts/Phosphate-Solid.ttf");
        title.setTypeface(phosphate);

        title.setText("Pick Second Weapon • " + getIntent().getStringExtra("weapon_name"));

        mSharedPreferences = this.getSharedPreferences("com.austinhodak.pubgcenter", MODE_PRIVATE);

        firstWeapon = getIntent().getStringExtra("firstWeapon");

        setupAdapter();
        loadWeapons();
    }

    public void compare() {

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void loadWeapons() {
        String[] classes = {"assault_rifles", "sniper_rifles", "smgs", "shotguns", "pistols", "lmgs", "throwables",
                "melee", "misc"};
        for (String name : classes) {
            db.collection("weapons").document(name).collection("weapons").orderBy("weapon_name")
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(final QuerySnapshot documentSnapshots,
                                final FirebaseFirestoreException e) {
                            Set<String> favs = mSharedPreferences.getStringSet("favoriteWeapons", null);
                            //data.clear();
                            for (DocumentSnapshot document : documentSnapshots) {

                                if (favs != null && favs.contains(document.getReference().getPath())) {
                                    data.add(0, document);
                                } else {
                                    data.add(document);
                                }

                                //mProgressBar.setVisibility(View.GONE);
                            }
                            adapter.updateData(data);
                        }
                    });
        }
    }

    private void setupAdapter() {
        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, 1);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        LinearLayoutManager linearLayout = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(gridLayoutManager);
        adapter = SlimAdapter.create()
                .register(R.layout.weapon_list_item_card_light, new SlimInjector<DocumentSnapshot>() {
                    @Override
                    public void onInject(final DocumentSnapshot data, final IViewInjector injector) {
                        Set<String> favs = mSharedPreferences.getStringSet("favoriteWeapons", null);
                        if (favs != null && favs.contains(data.getReference().getPath())) {
                            injector.visible(R.id.favorite_weapon_icon);
                        } else {
                            injector.gone(R.id.favorite_weapon_icon);
                        }

                        TextView subtitle = (TextView) injector
                                .findViewById(R.id.weaponItemSubtitle);

                        ImageView icon = (ImageView) injector
                                .findViewById(R.id.helmetItem64);

                        injector.text(R.id.weaponItemName,
                                data.getString("weapon_name"));

                        if (data.get("icon") != null) {
                            StorageReference gsReference = storage
                                    .getReferenceFromUrl(data.getString("icon"));

                            Glide.with(CompareWeaponPicker.this)
                                    .using(new FirebaseImageLoader())
                                    .load(gsReference)
                                    .placeholder(R.drawable.icons8_rifle)
                                    .into(icon);

                        }

                        injector.text(R.id.weaponItemSubtitle, "");

                        if (data.getString("ammo") != null) {
                            injector.text(R.id.weaponItemSubtitle,
                                    data.getString("ammo"));
                        }

                        if (data.getString("damageBody0") != null) {
                            if (subtitle.getText().length() != 0) {
                                injector.text(R.id.weaponItemSubtitle,
                                        subtitle.getText() + " • Body: " + data
                                                .getString("damageBody0"));
                            } else {
                                injector.text(R.id.weaponItemSubtitle,
                                        subtitle.getText() + "Body: " + data
                                                .getString("damageBody0"));
                            }
                        }

                        if (data.getString("damageHead0") != null) {
                            injector.text(R.id.weaponItemSubtitle,
                                    subtitle.getText() + " • Head: " + data
                                            .getString("damageHead0"));
                        }

                        if (subtitle.getText().length() == 0) {
                            injector.gone(R.id.weaponItemSubtitle);
                        }

                        injector.clicked(R.id.top_layout, new OnClickListener() {
                            @Override
                            public void onClick(final View view) {
                                Intent intent = new Intent(CompareWeaponPicker.this,
                                        CompareWeaponActivity.class);
                                intent.putExtra("firstWeapon", firstWeapon);
                                intent.putExtra("secondWeapon",
                                        data.getReference().getPath());
                                intent.putExtra("weaponName", data.getString("weapon_name"));
                                startActivity(intent);
                            }
                        });
                    }
                }).attachTo(mRecyclerView);
    }
}