package com.respondingio.battlegroundsbuddy.weapons;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.google.firebase.firestore.ListenerRegistration;
import com.respondingio.battlegroundsbuddy.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;

/**
 * A simple {@link Fragment} subclass.
 */
public class WeaponDetailCamera extends Fragment {

    @BindView(R.id.weaponDOFNearRange)
    TextView nearRangeTV;

    @BindView(R.id.weaponDOFPower)
    TextView powerTV;

    @BindView(R.id.weaponDOFRange)
    TextView rangeTV;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private FirebaseStorage storage = FirebaseStorage.getInstance();

    private ListenerRegistration weaponListener;

    public WeaponDetailCamera() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_weapon_detail_dof, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getArguments() != null) {
            loadWeapon(getArguments().getString("weaponPath"), getArguments().getString("weaponClass"));
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (weaponListener != null) weaponListener.remove();
    }

    private void loadWeapon(final String weaponID, final String weaponClass) {
        weaponListener = db.document(weaponID).collection("stats")
                .document("camera_dof")
                .addSnapshotListener(
                        new EventListener<DocumentSnapshot>() {
                            @Override
                            public void onEvent(final DocumentSnapshot data,
                                    final FirebaseFirestoreException e) {
                                if (e == null && data != null && data.exists()) {
                                    if (data.getString("range") != null) {
                                        rangeTV.setText(data.getString("range"));
                                    }

                                    if (data.getString("nearRange") != null) {
                                        nearRangeTV.setText(data.getString("nearRange"));
                                    }

                                    if (data.getString("power") != null) {
                                        powerTV.setText(data.getString("power"));
                                    }

                                } else {

                                }
                            }
                        });
    }

}
