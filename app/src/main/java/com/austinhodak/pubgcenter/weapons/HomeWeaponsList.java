package com.austinhodak.pubgcenter.weapons;


import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
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
import com.appodeal.ads.Appodeal;
import com.appodeal.ads.NativeAd;
import com.appodeal.ads.NativeCallbacks;
import com.appodeal.ads.native_ad.views.NativeAdViewAppWall;
import com.austinhodak.pubgcenter.R;
import com.austinhodak.pubgcenter.WeaponDetailActivity;
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

    private NativeAd currentAd;

    private boolean isFavoriteTab = false;

    private SharedPreferences mSharedPreferences;

    private SlimAdapter slimAdapter;

    private FirebaseStorage storage = FirebaseStorage.getInstance();

    public HomeWeaponsList() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home_weapons_list, container, false);
        ButterKnife.bind(this, view);

        mSharedPreferences = getActivity().getSharedPreferences("com.austinhodak.pubgcenter", MODE_PRIVATE);

        mRecyclerView = view.findViewById(R.id.weapon_list_rv);

        if (!mSharedPreferences.getBoolean("removeAds", false)) {
            //loadAds();
            Appodeal.cache(getActivity(), Appodeal.NATIVE, 1);
            Appodeal.setNativeCallbacks(new NativeCallbacks() {
                @Override
                public void onNativeClicked(final NativeAd nativeAd) {

                }

                @Override
                public void onNativeFailedToLoad() {

                }

                @Override
                public void onNativeLoaded() {
                    if (isAdLoaded) {
                        return;
                    }

                    try {
                        isAdLoaded = true;
                        currentAd = Appodeal.getNativeAds(1).get(0);
                        data.add(currentAd);
                        if (slimAdapter != null) {
                            slimAdapter.updateData(data);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        isAdLoaded = false;

                    }
                }

                @Override
                public void onNativeShown(final NativeAd nativeAd) {

                }
            });
        }

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

            setupAdapter(position);
        }
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

            if (currentAd != null) {
                data.add(currentAd);
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

                            if (currentAd != null) {
                                data.add(currentAd);
                            }

                            slimAdapter.updateData(data);
                        }
                    });
        }

        mProgressBar.setVisibility(View.VISIBLE);
    }

    private void setupAdapter(final int position) {
        final Set<String> favs = mSharedPreferences.getStringSet("favoriteWeapons", null);

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

                                if (favs != null && favs.contains(data.getReference().getPath())) {
                                    injector.visible(R.id.fav_icon);
                                }

                                if (mSharedPreferences.contains(data.getReference().getPath() + "-like") && mSharedPreferences
                                        .getBoolean(data.getReference().getPath() + "-like", false)) {
                                    injector.visible(R.id.heart_icon);
                                }
                            }
                        })
                .register(R.layout.ad_list_item_card, new SlimInjector<NativeAd>() {
                    @Override
                    public void onInject(@NonNull final NativeAd nativeAd, @NonNull final IViewInjector injector) {
                        CardView cardView = (CardView) injector.findViewById(R.id.card);
                        NativeAdViewAppWall nativeAdView = new NativeAdViewAppWall(getActivity(), nativeAd);
                        nativeAdView.showSponsored(true);
                        cardView.addView(nativeAdView);
                    }
                }).updateData(data).attachTo(mRecyclerView);

        loadWeapons(position);
    }
}
