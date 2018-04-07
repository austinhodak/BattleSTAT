package com.austinhodak.pubgcenter.weapons;


import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.austinhodak.pubgcenter.R;
import com.austinhodak.pubgcenter.WeaponDetailActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuth.AuthStateListener;
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

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeWeaponsList extends Fragment {

    List<Object> data = new ArrayList<>();

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    boolean isAdLoaded = false;

    @BindView(R.id.pg)
    ProgressBar mProgressBar;

    RecyclerView mRecyclerView;

    private boolean isFavoriteTab = false;

    private boolean lightTesting = false;

    private SharedPreferences mSharedPreferences;

    private SlimAdapter slimAdapter;

    private FirebaseStorage storage = FirebaseStorage.getInstance();

    public HomeWeaponsList() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home_weapons_list, container, false);

        //        lightTesting = FirebaseRemoteConfig.getInstance().getBoolean("light_version");
        //        if (lightTesting) {
        //            //User is in the light version test group, update UI.
        //            view = inflater.inflate(R.layout.home_weapons_list_light, container, false);
        //        } else {
        //            view = inflater.inflate(R.layout.home_weapons_list, container, false);
        //        }

        ButterKnife.bind(this, view);

        mSharedPreferences = getActivity().getSharedPreferences("com.austinhodak.pubgcenter", MODE_PRIVATE);

        mRecyclerView = view.findViewById(R.id.weapon_list_rv);

        if (!mSharedPreferences.getBoolean("removeAds", false)) {
            //loadAds();
        }

        loadRemoteConfig();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getArguments() != null) {
            final int position = getArguments().getInt("pos");
            if (getArguments().containsKey("isFavoriteTab")) {
                isFavoriteTab = getArguments().getBoolean("isFavoriteTab");
            }

            mProgressBar.setVisibility(View.VISIBLE);

            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                FirebaseAuth.getInstance().addAuthStateListener(new AuthStateListener() {
                    @Override
                    public void onAuthStateChanged(@NonNull final FirebaseAuth firebaseAuth) {
                        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                            setupAdapter(position);
                        }
                    }
                });
            } else {
                setupAdapter(position);
            }
        }
    }

    private void loadRemoteConfig() {
        //boolean

    }

    private void loadWeapons(final int position) {

        String doc = "";

        switch (position) {
            case 0:
                doc = "assault_rifles";
                break;
            case 1:
                doc = "sniper_rifles";
                break;
            case 2:
                doc = "smgs";
                break;
            case 3:
                doc = "shotguns";
                break;
            case 4:
                doc = "pistols";
                break;
            case 5:
                doc = "lmgs";
                break;
            case 6:
                doc = "throwables";
                break;
            case 7:
                doc = "melee";
                break;
            case 8:
                doc = "misc";
                break;
        }

        if (isFavoriteTab) {
            String[] classes = {"assault_rifles", "sniper_rifles", "smgs", "shotguns", "pistols", "lmgs", "throwables",
                    "melee", "misc"};

            data.clear();

            for (String name : classes) {
                db.collection("weapons").document(name).collection("weapons").orderBy("weapon_name")
                        .addSnapshotListener(new EventListener<QuerySnapshot>() {
                            @Override
                            public void onEvent(final QuerySnapshot documentSnapshots, final FirebaseFirestoreException e) {

                                Set<String> favs = mSharedPreferences.getStringSet("favoriteWeapons", null);

                                if (documentSnapshots != null) {
                                    for (DocumentSnapshot document : documentSnapshots) {

                                        if (favs != null && favs.contains(document.getReference().getPath())) {
                                            data.add(document);
                                        }

                                        mProgressBar.setVisibility(View.GONE);
                                    }
                                }
                                slimAdapter.updateData(data);

                            }
                        });
            }
        } else {
            db.collection("weapons").document(doc).collection("weapons").orderBy("weapon_name")
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(final QuerySnapshot documentSnapshots, final FirebaseFirestoreException e) {
                            data.clear();

                            if (documentSnapshots != null) {
                                for (DocumentSnapshot document : documentSnapshots) {

                                    data.add(document);

                                    mProgressBar.setVisibility(View.GONE);
                                }
                            }

                            slimAdapter.updateData(data);
                        }
                    });
        }

        mProgressBar.setVisibility(View.VISIBLE);
    }

    private void setupAdapter(final int position) {
        final Set<String> favs = mSharedPreferences.getStringSet("favoriteWeapons", null);

        //int layout = R.layout.weapon_list_item_card;

        if (lightTesting) {
            //    layout = R.layout.weapon_list_item_card_light1;
        }

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(linearLayoutManager);
        slimAdapter = SlimAdapter.create()
                .register(R.layout.weapon_list_item_card,
                        new SlimInjector<DocumentSnapshot>() {
                            @Override
                            public void onInject(@NonNull final DocumentSnapshot data, @NonNull final IViewInjector injector) {

                                TextView subtitle = (TextView) injector
                                        .findViewById(R.id.weaponItemSubtitle);

                                ImageView icon = (ImageView) injector
                                        .findViewById(R.id.helmetItem64);

                                injector.text(R.id.weaponItemName,
                                        data.getString("weapon_name"));

                                if (data.get("icon") != null) {
                                    StorageReference gsReference = storage
                                            .getReferenceFromUrl(data.getString("icon"));

                                    Glide.with(getActivity())
                                            .using(new FirebaseImageLoader())
                                            .load(gsReference)
                                            //.asBitmap()
                                            .placeholder(R.drawable.icons8_rifle)
                                            //.diskCacheStrategy(DiskCacheStrategy.RESULT)
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
                                        Intent intent = new Intent(getActivity(),
                                                WeaponDetailActivity.class);
                                        intent.putExtra("weaponPath", data.getReference().getPath());
                                        intent.putExtra("weaponName",
                                                data.getString("weapon_name"));
                                        startActivity(intent);
                                    }
                                });

                                if (data.contains("bestInClass")) {
                                    if (data.getBoolean("bestInClass")) {
                                        //CardView cardView = (CardView) injector.findViewById(R.id.card);
                                    }
                                }

                                injector.gone(R.id.fav_icon);
                                if (favs != null && favs.contains(data.getReference().getPath())) {
                                    injector.visible(R.id.fav_icon);
                                }

                                injector.gone(R.id.heart_icon);
                                if (mSharedPreferences.contains(data.getReference().getPath() + "-like") && mSharedPreferences
                                        .getBoolean(data.getReference().getPath() + "-like", false)) {
                                    injector.visible(R.id.heart_icon);
                                }

                                injector.gone(R.id.weapon_airdrop_icon);
                                injector.gone(R.id.weapon_best_icon);

                                if (data.contains("airDropOnly") && data.get("airDropOnly") != null) {
                                    if (data.getBoolean("airDropOnly")) {
                                        Glide.with(getActivity()).load(R.drawable.ic_parachute)
                                                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                                                .into((ImageView) injector.findViewById(R.id.weapon_airdrop_icon));
                                        injector.visible(R.id.weapon_airdrop_icon);
                                    } else {
                                        injector.gone(R.id.weapon_airdrop_icon);
                                    }
                                }

                                if (data.contains("bestInClass") && data.get("bestInClass") != null) {
                                    if (data.getBoolean("bestInClass")) {
                                        Glide.with(getActivity()).load(R.drawable.icons8_trophy)
                                                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                                                .into((ImageView) injector.findViewById(R.id.weapon_best_icon));
                                        injector.visible(R.id.weapon_best_icon);
                                    } else {
                                        injector.gone(R.id.weapon_best_icon);
                                    }
                                }
                            }
                        })
                .updateData(data).attachTo(mRecyclerView);

        loadWeapons(position);
    }
}