package com.respondingio.battlegroundsbuddy.weapons;


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
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.respondingio.battlegroundsbuddy.GlideApp;
import com.respondingio.battlegroundsbuddy.R;
import com.respondingio.battlegroundsbuddy.WeaponDetailActivity;

import net.idik.lib.slimadapter.SlimAdapter;
import net.idik.lib.slimadapter.SlimInjector;
import net.idik.lib.slimadapter.viewinjector.IViewInjector;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.content.Context.MODE_PRIVATE;
import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeWeaponsList extends Fragment {

    List<Object> data = new ArrayList<>();

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @BindView(R.id.pg)
    ProgressBar mProgressBar;

    RecyclerView mRecyclerView;

    private String doc;

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

        if (getActivity() != null) {
            mSharedPreferences = getActivity().getSharedPreferences("com.austinhodak.pubgcenter", MODE_PRIVATE);
        }

        mRecyclerView = view.findViewById(R.id.weapon_list_rv);

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
            setupAdapter(position);
        }
    }

    private void loadWeapons(final int position) {

        doc = "";

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
                                    if (document.contains("live")) {
                                        if (!document.getBoolean("live"))
                                            // Weapon is not ready to be live yet.
                                        continue;
                                    }
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

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(linearLayoutManager);
        slimAdapter = SlimAdapter.create()
                .register(R.layout.testing,
                        new SlimInjector<DocumentSnapshot>() {
                            @Override
                            public void onInject(@NonNull final DocumentSnapshot data, @NonNull final IViewInjector injector) {

                                TextView subtitle = (TextView) injector
                                        .findViewById(R.id.weapon_subtitle);

                                ImageView icon = (ImageView) injector
                                        .findViewById(R.id.weapon_icon);

                                injector.text(R.id.weapon_title,
                                        data.getString("weapon_name"));

                                if (data.get("icon") != null) {
                                    StorageReference gsReference = storage
                                            .getReferenceFromUrl(data.getString("icon"));

                                    if (getActivity() != null) {
                                        DrawableCrossFadeFactory factory =
                                                new DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build();


                                        GlideApp.with(getActivity())
                                                .load(gsReference)
                                                .apply(new RequestOptions().placeholder(R.drawable.icons8_rifle))
                                                .transition(withCrossFade(factory))
                                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                                .into(icon);
                                    }
                                }

                                injector.text(R.id.weapon_subtitle, "");

                                if (data.getString("ammo") != null) {
                                    injector.text(R.id.weapon_subtitle,
                                            data.getString("ammo"));
                                }

                                if (data.getString("damageBody0") != null) {
                                    injector.text(R.id.weapon_body_dmg,
                                            data
                                                    .getString("damageBody0"));
                                    injector.visible(R.id.divider);
                                } else {
                                    injector.text(R.id.weapon_body_dmg, "N/A");

                                    LinearLayout linearLayout = (LinearLayout) injector.findViewById(R.id.weapon_body_dmg).getParent();
                                    linearLayout.setVisibility(View.GONE);
                                }

                                if (data.getString("damageHead0") != null) {
                                    injector.text(R.id.weapon_head_dmg,
                                            data
                                                    .getString("damageHead0"));
                                    injector.visible(R.id.divider);
                                } else {
                                    injector.text(R.id.weapon_head_dmg, "N/A");

                                    LinearLayout linearLayout = (LinearLayout) injector.findViewById(R.id.weapon_head_dmg).getParent();
                                    linearLayout.setVisibility(View.GONE);
                                }

                                if (data.getString("range") != null) {
                                    String range = data.getString("range");
                                    String[] split = range.split("-");
                                    injector.text(R.id.weapon_range, split[1] + "M");
                                    injector.visible(R.id.divider);
                                } else {
                                    injector.text(R.id.weapon_range, "N/A");

                                    LinearLayout linearLayout = (LinearLayout) injector.findViewById(R.id.weapon_range).getParent();
                                    linearLayout.setVisibility(View.GONE);
                                }

                                if (subtitle.getText().length() == 0) {
                                    injector.gone(R.id.weapon_subtitle);
                                }

                                injector.clicked(R.id.card_top, new OnClickListener() {
                                    @Override
                                    public void onClick(final View view) {
                                        Intent intent = new Intent(getActivity(),
                                                WeaponDetailActivity.class);
                                        intent.putExtra("weaponPath", data.getReference().getPath());
                                        intent.putExtra("weaponName",
                                                data.getString("weapon_name"));
                                        intent.putExtra("weaponKey", data.getId());
                                        intent.putExtra("weaponClass", doc);
                                        startActivity(intent);
                                    }
                                });

                                injector.gone(R.id.weapon_fav);
                                if (favs != null && favs.contains(data.getReference().getPath())) {
                                    injector.visible(R.id.weapon_fav);
                                }

                                injector.gone(R.id.weapon_like);
                                if (mSharedPreferences.contains(data.getReference().getPath() + "-like") && mSharedPreferences
                                        .getBoolean(data.getReference().getPath() + "-like", false)) {
                                    injector.visible(R.id.weapon_like);
                                }

                                injector.gone(R.id.weapon_parachute);
                                injector.gone(R.id.weapon_trophy);

                                if (data.contains("airDropOnly") && data.get("airDropOnly") != null) {
                                    if (data.getBoolean("airDropOnly")) {
                                        if (getActivity() != null) {
                                            GlideApp.with(getActivity()).load(R.drawable.ic_parachute)
                                                    .into((ImageView) injector.findViewById(R.id.weapon_parachute));
                                        }
                                        injector.visible(R.id.weapon_parachute);
                                    } else {
                                        injector.gone(R.id.weapon_parachute);
                                    }
                                }

                                if (data.contains("bestInClass") && data.get("bestInClass") != null) {
                                    if (data.getBoolean("bestInClass")) {
                                        if (getActivity() != null) {
                                            GlideApp.with(getActivity()).load(R.drawable.icons8_trophy)
                                                    .into((ImageView) injector.findViewById(R.id.weapon_trophy));
                                        }
                                        injector.visible(R.id.weapon_trophy);
                                    } else {
                                        injector.gone(R.id.weapon_trophy);
                                    }
                                }

                                if (data.contains("miramar_only") && data.get("miramar_only") != null) {
                                    if (data.getBoolean("miramar_only")) {
                                        if (getActivity() != null) {
                                            GlideApp.with(getActivity()).load(R.drawable.cactu)
                                                    .into((ImageView) injector.findViewById(R.id.weapon_miramar));
                                        }
                                        injector.visible(R.id.weapon_miramar);
                                    } else {
                                        injector.gone(R.id.weapon_miramar);
                                    }
                                }

                                if (data.contains("sanhok_only") && data.get("sanhok_only") != null) {
                                    if (data.getBoolean("sanhok_only")) {
                                        injector.visible(R.id.weapon_sanhok);
                                    } else {
                                        injector.gone(R.id.weapon_sanhok);
                                    }
                                }
                            }
                        })
                .updateData(data).attachTo(mRecyclerView);

        loadWeapons(position);
    }
}