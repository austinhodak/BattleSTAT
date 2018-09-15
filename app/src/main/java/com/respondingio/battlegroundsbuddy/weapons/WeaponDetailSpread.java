package com.respondingio.battlegroundsbuddy.weapons;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.google.android.gms.ads.AdView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.storage.FirebaseStorage;
import com.respondingio.battlegroundsbuddy.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class WeaponDetailSpread extends Fragment {

    @BindView(R.id.weaponweaponSpreadADSTV)
    TextView adsTV;

    @BindView(R.id.weaponSpreadAimingTV)
    TextView aimingTV;

    @BindView(R.id.weaponSpreadCrouchTV)
    TextView crouchTV;

    @BindView(R.id.weaponSpreadFiringTV)
    TextView firingTV;

    @BindView(R.id.weaponSpreadJumpTV)
    TextView jumpTV;

    @BindView(R.id.adView)
    AdView mAdView;

    @BindView(R.id.weaponSpreadProneTV)
    TextView proneTV;

    @BindView(R.id.weaponSpreadRunTV)
    TextView runTV;

    @BindView(R.id.weaponSpreadBaseTV)
    TextView spreadBaseTV;

    @BindView(R.id.weaponSpreadWalkTV)
    TextView walkTV;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private FirebaseStorage storage = FirebaseStorage.getInstance();

    private ListenerRegistration weaponListener;

    public WeaponDetailSpread() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_weapon_detail_spread, container, false);
        ButterKnife.bind(this, view);
        // Inflate the layout for this fragment
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
                .document("spread")
                .addSnapshotListener(
                        (data, e) -> {
                            if (e == null && data != null && data.exists()) {
                                if (data.getString("baseSpread") != null) {
                                    spreadBaseTV.setText(data.getString("baseSpread"));
                                }

                                if (data.getString("aimingMod") != null) {
                                    aimingTV.setText(data.getString("aimingMod"));
                                }

                                if (data.getString("adsMod") != null) {
                                    adsTV.setText(data.getString("adsMod"));
                                }

                                if (data.getString("firingBase") != null) {
                                    firingTV.setText(data.getString("firingBase"));
                                }

                                if (data.getString("crouchMod") != null) {
                                    crouchTV.setText(data.getString("crouchMod"));
                                }

                                if (data.getString("proneMod") != null) {
                                    proneTV.setText(data.getString("proneMod"));
                                }

                                if (data.getString("walkMod") != null) {
                                    walkTV.setText(data.getString("walkMod"));
                                }

                                if (data.getString("runMod") != null) {
                                    runTV.setText(data.getString("runMod"));
                                }

                                if (data.getString("jumpMod") != null) {
                                    jumpTV.setText(data.getString("jumpMod"));
                                }

                                if (data.getString("bestInClass") != null) {

                                }

                            } else {

                            }
                        });
    }

}
