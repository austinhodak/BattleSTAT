package com.austinhodak.pubgcenter;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
import net.idik.lib.slimadapter.SlimAdapter;
import net.idik.lib.slimadapter.SlimInjector;
import net.idik.lib.slimadapter.viewinjector.IViewInjector;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeConsumablesList extends Fragment {

    List<Object> data = new ArrayList<>();

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @BindView(R.id.pg)
    ProgressBar mProgressBar;

    RecyclerView mRecyclerView;

    private SlimAdapter slimAdapter;

    private FirebaseStorage storage = FirebaseStorage.getInstance();

    public HomeConsumablesList() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home_weapons_list, container, false);
        ButterKnife.bind(this, view);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.weapon_list_rv);

        setupAdapter();

        return view;
    }

    private void loadWeapons() {

        db.collection("consumables").orderBy("name")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(final QuerySnapshot documentSnapshots, final FirebaseFirestoreException e) {
                        data.clear();
                        try {
                            for (DocumentSnapshot document : documentSnapshots) {
                                data.add(document);

                                mProgressBar.setVisibility(View.GONE);
                            }
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                        slimAdapter.updateData(data);

                    }
                });

        mProgressBar.setVisibility(View.VISIBLE);
    }

    private void setupAdapter() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(linearLayoutManager);
        slimAdapter = SlimAdapter.create()
                .register(R.layout.weapon_list_item_card,
                        new SlimInjector<DocumentSnapshot>() {
                            @Override
                            public void onInject(final DocumentSnapshot data, final IViewInjector injector) {

                                TextView subtitle = (TextView) injector
                                        .findViewById(R.id.weaponItemSubtitle);

                                ImageView icon = (ImageView) injector
                                        .findViewById(R.id.helmetItem64);

                                injector.text(R.id.weaponItemName,
                                        data.getString("name"));

                                if (data.get("icon") != null) {
                                    try {
                                        StorageReference gsReference = storage
                                                .getReferenceFromUrl(data.getString("icon"));

                                        Glide.with(getActivity())
                                                .using(new FirebaseImageLoader())
                                                .load(gsReference)
                                                .into(icon);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                }

                                injector.gone(R.id.weaponItemSubtitle);
                            }
                        })
                .register(R.layout.weapon_list_header, new SlimInjector<String>() {
                    @Override
                    public void onInject(final String data, final IViewInjector injector) {
                        Log.d("SECTION", data);

                        injector.text(R.id.weaponHeaderTitle, data);
                    }
                }).updateData(data).attachTo(mRecyclerView);

        loadWeapons();
    }

}
