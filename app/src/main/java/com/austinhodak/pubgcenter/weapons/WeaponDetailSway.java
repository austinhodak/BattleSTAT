package com.austinhodak.pubgcenter.weapons;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.austinhodak.pubgcenter.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;

/**
 * A simple {@link Fragment} subclass.
 */
public class WeaponDetailSway extends Fragment {

    @BindView(R.id.weaponSprayCrouchMod)
    TextView crouchTV;

    @BindView(R.id.weaponSprayMoveMod)
    TextView moveModTV;

    @BindView(R.id.weaponSprayPitch)
    TextView pitchTV;

    @BindView(R.id.weaponSprayProneMod)
    TextView proneTV;

    @BindView(R.id.weaponSprayYaw)
    TextView yawTV;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private FirebaseStorage storage = FirebaseStorage.getInstance();

    public WeaponDetailSway() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_weapon_detail_sway, container, false);
        ButterKnife.bind(this, view);
        // Inflate the layout for this fragment
        if (getArguments() != null) {
            loadWeapon(getArguments().getString("weaponPath"), getArguments().getString("weaponClass"));
        }

        //AdRequest adRequest = new AdRequest.Builder().build();
        //mAdView.loadAd(adRequest);

        return view;
    }

    private void loadWeapon(final String weaponID, final String weaponClass) {
        db.document(weaponID).collection("stats")
                .document("sway")
                .addSnapshotListener(
                        new EventListener<DocumentSnapshot>() {
                            @Override
                            public void onEvent(final DocumentSnapshot data,
                                    final FirebaseFirestoreException e) {
                                if (e == null && data != null && data.exists()) {
                                    if (data.getString("pitch") != null) {
                                        pitchTV.setText(data.getString("pitch"));
                                    }

                                    if (data.getString("yaw") != null) {
                                        yawTV.setText(data.getString("yaw"));
                                    }

                                    if (data.getString("moveMod") != null) {
                                        moveModTV.setText(data.getString("moveMod"));
                                    }

                                    if (data.getString("crouchMod") != null) {
                                        crouchTV.setText(data.getString("crouchMod"));
                                    }

                                    if (data.getString("proneMod") != null) {
                                        proneTV.setText(data.getString("proneMod"));
                                    }

                                } else {

                                }
                            }
                        });
    }

}
