package com.austinhodak.pubgcenter.attachments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.afollestad.materialdialogs.MaterialDialog;
import com.austinhodak.pubgcenter.GlideApp;
import com.austinhodak.pubgcenter.R;
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
public class HomeAttachmentsList extends Fragment {

    List<Object> data = new ArrayList<>();

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @BindView(R.id.pg)
    ProgressBar mProgressBar;

    RecyclerView mRecyclerView;

    private SlimAdapter slimAdapter;

    private FirebaseStorage storage = FirebaseStorage.getInstance();

    public HomeAttachmentsList() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home_weapons_list, container, false);
        ButterKnife.bind(this, view);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.weapon_list_rv);

        if (getArguments() != null) {
            int position = getArguments().getInt("pos");

            setupAdapter(position);
        }

        Log.d("test", "" + getArguments().getInt("pos"));

        return view;
    }

    private void loadWeapons(final int position) {

        String doc = "";

        switch (position) {
            case 0:
                doc = "Muzzle";
                break;
            case 1:
                doc = "Upper Rail";
                break;
            case 2:
                doc = "Lower Rail";
                break;
            case 3:
                doc = "Magazine";
                break;
            case 4:
                doc = "Stock";
                break;
        }

        db.collection("attachments").document("muzzle").collection("attachments")
                .whereEqualTo("location", doc)
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

    private void setupAdapter(final int position) {
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

                                subtitle.setMaxLines(10);

                                if (data.get("icon") != null) {
                                    try {
                                        StorageReference gsReference = storage
                                                .getReferenceFromUrl(data.getString("icon"));

                                        GlideApp.with(getActivity())
                                                .load(gsReference)
                                                .into(icon);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                }

                                injector.text(R.id.weaponItemSubtitle, data.getString("weapons"));

                                injector.clicked(R.id.top_layout, new OnClickListener() {
                                    @Override
                                    public void onClick(final View view) {
                                        if (data.contains("stats")) {
                                            String stats = "";
                                            stats = data.getString("stats").replaceAll("<br>", "");
                                            stats = stats.replace(" +", "\n+");
                                            stats = stats.replace(" -", "\n-");
                                            MaterialDialog materialDialog = new MaterialDialog.Builder(getActivity())
                                                    .title(data.getString("name"))
                                                    .content(stats)
                                                    .contentColorRes(R.color.md_white_1000)
                                                    .positiveText("OK")
                                                    .build();

                                            materialDialog.show();
                                        }

                                    }
                                });
                            }
                        })
                .register(R.layout.weapon_list_header, new SlimInjector<String>() {
                    @Override
                    public void onInject(final String data, final IViewInjector injector) {
                        Log.d("SECTION", data);

                        injector.text(R.id.weaponHeaderTitle, data);
                    }
                }).updateData(data).attachTo(mRecyclerView);

        loadWeapons(position);
    }

}
