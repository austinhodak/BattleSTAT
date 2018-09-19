package com.respondingio.battlegroundsbuddy.weapons;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.storage.FirebaseStorage;
import com.respondingio.battlegroundsbuddy.R;

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

    private ListenerRegistration weaponListener;

    public WeaponDetailSway() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_weapon_detail_sway, container, false);
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
