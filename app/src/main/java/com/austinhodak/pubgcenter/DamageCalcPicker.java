package com.austinhodak.pubgcenter;

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

public class DamageCalcPicker extends AppCompatActivity {

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
        //title.setText("Damage Calculator");

        mSharedPreferences = this.getSharedPreferences("com.austinhodak.pubgcenter", MODE_PRIVATE);

        firstWeapon = getIntent().getStringExtra("firstWeapon");

        setupAdapter();

        if (getIntent() != null) {
            switch (getIntent().getIntExtra("itemType", 0)) {
                case 0:
                    loadWeapons();
                    title.setText("Pick a Weapon");
                    break;
                case 1:
                    loadHelmets();
                    title.setText("Pick a Helmet");
                    break;
                case 2:
                    loadVests();
                    title.setText("Pick a Vest");
                    break;
            }
        }
    }

    public void compare() {

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void loadHelmets() {
        db.collection("equipment").whereEqualTo("type", "helmet")
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

    private void loadVests() {
        db.collection("equipment").whereEqualTo("type", "vest")
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

    private void loadWeapons() {
        String[] classes = {"assault_rifles", "sniper_rifles", "smgs", "shotguns", "pistols", "lmgs",
                "melee", "misc"};
        for (String name : classes) {
            db.collection("weapons").document(name).collection("weapons").orderBy("weapon_name")
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(final QuerySnapshot documentSnapshots,
                                final FirebaseFirestoreException e) {
                            Set<String> favs = mSharedPreferences.getStringSet("favoriteWeapons", null);
                            //data.clear();
                            if (documentSnapshots == null) {
                                return;
                            }
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

                        if (data.contains("weapon_name")) {
                            injector.text(R.id.weaponItemName,
                                    data.getString("weapon_name"));

                            if (data.get("icon") != null) {
                                StorageReference gsReference = storage
                                        .getReferenceFromUrl(data.getString("icon"));

                                Glide.with(DamageCalcPicker.this)
                                        .using(new FirebaseImageLoader())
                                        .load(gsReference)
                                        .placeholder(R.drawable.icons8_rifle)
                                        .into(icon);

                            }
                        } else {
                            injector.text(R.id.weaponItemName,
                                    data.getString("name"));

                            // Toast.makeText(DamageCalcPicker.this, data.getString("type"), Toast.LENGTH_SHORT).show();

                            if (data.contains("type")) {
                                if (data.getString("type").contains("vest")) {
                                    if (data.get("icon") != null) {
                                        StorageReference gsReference = storage
                                                .getReferenceFromUrl(data.getString("icon"));

                                        Glide.with(DamageCalcPicker.this)
                                                .using(new FirebaseImageLoader())
                                                .load(gsReference)
                                                .placeholder(R.drawable.vest)
                                                .into(icon);
                                    }
                                } else {
                                    if (data.get("icon") != null) {
                                        StorageReference gsReference = storage
                                                .getReferenceFromUrl(data.getString("icon"));

                                        Glide.with(DamageCalcPicker.this)
                                                .using(new FirebaseImageLoader())
                                                .load(gsReference)
                                                .placeholder(R.drawable.icons8_helmet)
                                                .into(icon);
                                    }
                                }
                            } else {
                                if (data.get("icon") != null) {
                                    StorageReference gsReference = storage
                                            .getReferenceFromUrl(data.getString("icon"));

                                    Glide.with(DamageCalcPicker.this)
                                            .using(new FirebaseImageLoader())
                                            .load(gsReference)
                                            .into(icon);

                                }
                            }
                        }

                        if (subtitle.getText().length() == 0) {
                            injector.gone(R.id.weaponItemSubtitle);
                        }

                        injector.clicked(R.id.top_layout, new OnClickListener() {
                            @Override
                            public void onClick(final View view) {
                                Intent intent = new Intent(DamageCalcPicker.this,
                                        DamageCalcActivity.class);
                                intent.putExtra("pickedWeapon",
                                        data.getReference().getPath());
                                setResult(RESULT_OK, intent);
                                finish();
                            }
                        });
                    }
                }).attachTo(mRecyclerView);
    }
}
