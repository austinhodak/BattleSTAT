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
public class WeaponDetailDeviation extends Fragment {

    @BindView(R.id.weaponDevAnimKick)
    TextView devAnimKickTV;

    @BindView(R.id.weaponDevBaseAds)
    TextView devBaseADSTV;

    @BindView(R.id.weaponDevBaseAIm)
    TextView devBaseAimTV;

    @BindView(R.id.weaponDevBase)
    TextView devBaseTV;

    @BindView(R.id.weaponDevCrouch)
    TextView devCrouchTV;

    @BindView(R.id.weaponDevMax)
    TextView devMaxTV;

    @BindView(R.id.weaponDevMoveMod)
    TextView devMoveModTV;

    @BindView(R.id.weaponDevMoveRef)
    TextView devMoveRefTV;

    @BindView(R.id.weaponDevProne)
    TextView devProneTV;

    @BindView(R.id.weaponDevRecGainAds)
    TextView devRecGainADSTV;

    @BindView(R.id.weaponDevRecAim)
    TextView devRecGainAimTV;

    @BindView(R.id.weaponDevRecGain)
    TextView devRecGainTV;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private FirebaseStorage storage = FirebaseStorage.getInstance();

    private ListenerRegistration weaponListener;

    public WeaponDetailDeviation() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_weapon_detail_deviation, container, false);
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
                .document("deviation")
                .addSnapshotListener(
                        new EventListener<DocumentSnapshot>() {
                            @Override
                            public void onEvent(final DocumentSnapshot data,
                                    final FirebaseFirestoreException e) {
                                if (e == null && data != null && data.exists()) {
                                    if (data.getString("base") != null) {
                                        devBaseTV.setText(data.getString("base"));
                                    }

                                    if (data.getString("baseAim") != null) {
                                        devBaseAimTV.setText(data.getString("baseAim"));
                                    }

                                    if (data.getString("baseADS") != null) {
                                        devBaseADSTV.setText(data.getString("baseADS"));
                                    }

                                    if (data.getString("recoilGain") != null) {
                                        devRecGainTV.setText(data.getString("recoilGain"));
                                    }

                                    if (data.getString("recoilGainAim") != null) {
                                        devRecGainAimTV.setText(data.getString("recoilGainAim"));
                                    }

                                    if (data.getString("recoilGainADS") != null) {
                                        devRecGainADSTV.setText(data.getString("recoilGainADS"));
                                    }

                                    if (data.getString("animKickADS") != null) {
                                        devAnimKickTV.setText(data.getString("animKickADS"));
                                    }

                                    if (data.getString("maxLimit") != null) {
                                        devMaxTV.setText(data.getString("maxLimit"));
                                    }

                                    if (data.getString("moveMod") != null) {
                                        devMoveModTV.setText(data.getString("moveMod"));
                                    }

                                    if (data.getString("moveRef") != null) {
                                        devMoveRefTV.setText(data.getString("moveRef"));
                                    }

                                    if (data.getString("crouchMod") != null) {
                                        devCrouchTV.setText(data.getString("crouchMod"));
                                    }

                                    if (data.getString("proneMod") != null) {
                                        devProneTV.setText(data.getString("proneMod"));
                                    }

                                } else {

                                }
                            }
                        });
    }

}
