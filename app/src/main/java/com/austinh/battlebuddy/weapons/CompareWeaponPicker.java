package com.austinh.battlebuddy.weapons;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.austinh.battlebuddy.R;
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

    private List<ListenerRegistration> list = new ArrayList<>();

    private SharedPreferences mSharedPreferences;

    private FirebaseStorage storage = FirebaseStorage.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compare_weapon_picker);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        title.setText("Compare With " + getIntent().getStringExtra("weapon_name"));

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
    @Override
    protected void onStop() {
        super.onStop();
        for (ListenerRegistration registration : list) {
            registration.remove();
        }
    }

    private void loadWeapons() {
        String[] classes = {"assault_rifles", "sniper_rifles", "smgs", "shotguns", "pistols", "lmgs", "throwables",
                "melee", "misc"};
        for (String name : classes) {
            list.add(db.collection("weapons").document(name).collection("weapons").orderBy("weapon_name")
                    .addSnapshotListener((documentSnapshots, e) -> {
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
                    }));
        }
    }

    private void setupAdapter() {
        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, 1);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        LinearLayoutManager linearLayout = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(gridLayoutManager);
        adapter = SlimAdapter.create()
                .register(R.layout.weapon_compare_picker_item, new SlimInjector<DocumentSnapshot>() {
                    @Override
                    public void onInject(final DocumentSnapshot data, final IViewInjector injector) {

                        ImageView icon = (ImageView) injector
                                .findViewById(R.id.pickerIcon);

                        injector.text(R.id.pickerText,
                                data.getString("weapon_name"));

                        if (data.get("icon") != null) {
                            StorageReference gsReference = storage
                                    .getReferenceFromUrl(data.getString("icon"));

                            Glide.with(CompareWeaponPicker.this)
                                    .load(gsReference)
                                    .into(icon);
                        }

                        injector.clicked(R.id.topLayout, new OnClickListener() {
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
