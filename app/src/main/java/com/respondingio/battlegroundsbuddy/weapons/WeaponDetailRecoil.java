package com.respondingio.battlegroundsbuddy.weapons;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.respondingio.battlegroundsbuddy.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;

/**
 * A simple {@link Fragment} subclass.
 */
public class WeaponDetailRecoil extends Fragment {

    @BindView(R.id.weaponRecoilCrouchMod)
    TextView crouchTV;

    @BindView(R.id.weaponRecoilHorSpeed)
    TextView horSpeedTV;

    @BindView(R.id.weaponRecoilHorTend)
    TextView horTendTV;

    @BindView(R.id.weaponRecoilLeftMax)
    TextView leftMaxTV;

    @BindView(R.id.weaponRecoilPatScale)
    TextView patternTV;

    @BindView(R.id.weaponRecoilProneMod)
    TextView proneTV;

    @BindView(R.id.weaponRecoilRecSpeed)
    TextView recSpeedTV;

    @BindView(R.id.weaponRecoilRightMax)
    TextView rightMaxTV;

    @BindView(R.id.weaponRecoilSpeed)
    TextView speedTV;

    @BindView(R.id.weaponRecoilValClimb)
    TextView valueClimbTV;

    @BindView(R.id.weaponRecoilValFall)
    TextView valueFallTV;

    @BindView(R.id.weaponRecoilVertClamp)
    TextView vertClampTV;

    @BindView(R.id.weaponRecoilVerRecClamp)
    TextView vertRecClampTV;

    @BindView(R.id.weaponRecoilVertRecMax)
    TextView vertRecMaxTV;

    @BindView(R.id.weaponRecoilVertRecMod)
    TextView vertRecModTV;

    @BindView(R.id.weaponRecoilVertSpeed)
    TextView vertSpeedTV;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private FirebaseStorage storage = FirebaseStorage.getInstance();

    public WeaponDetailRecoil() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_weapon_detail_recoil, container, false);
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
                .document("recoil")
                .addSnapshotListener(
                        new EventListener<DocumentSnapshot>() {
                            @Override
                            public void onEvent(final DocumentSnapshot data,
                                    final FirebaseFirestoreException e) {
                                if (e == null && data != null && data.exists()) {
                                    if (data.getString("vertClamp") != null) {
                                        vertClampTV.setText(data.getString("vertClamp"));
                                    }

                                    if (data.getString("vertSpeed") != null) {
                                        vertSpeedTV.setText(data.getString("vertSpeed"));
                                    }

                                    if (data.getString("vertRecClamp") != null) {
                                        vertRecClampTV.setText(data.getString("vertRecClamp"));
                                    }

                                    if (data.getString("vertRecMax") != null) {
                                        vertRecMaxTV.setText(data.getString("vertRecMax"));
                                    }

                                    if (data.getString("vertRecMod") != null) {
                                        vertRecModTV.setText(data.getString("vertRecMod"));
                                    }

                                    if (data.getString("horSpeed") != null) {
                                        horSpeedTV.setText(data.getString("horSpeed"));
                                    }

                                    if (data.getString("horTend") != null) {
                                        horTendTV.setText(data.getString("horTend"));
                                    }

                                    if (data.getString("leftMax") != null) {
                                        leftMaxTV.setText(data.getString("leftMax"));
                                    }

                                    if (data.getString("rightMax") != null) {
                                        rightMaxTV.setText(data.getString("rightMax"));
                                    }

                                    if (data.getString("speed") != null) {
                                        speedTV.setText(data.getString("speed"));
                                    }

                                    if (data.getString("recSpeed") != null) {
                                        recSpeedTV.setText(data.getString("recSpeed"));
                                    }

                                    if (data.getString("pattern") != null) {
                                        patternTV.setText(data.getString("pattern"));
                                    }

                                    if (data.getString("valueClimb") != null) {
                                        valueClimbTV.setText(data.getString("valueClimb"));
                                    }

                                    if (data.getString("valueFall") != null) {
                                        valueFallTV.setText(data.getString("valueFall"));
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
