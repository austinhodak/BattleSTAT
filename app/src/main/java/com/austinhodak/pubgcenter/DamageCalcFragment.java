package com.austinhodak.pubgcenter;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


/**
 * A simple {@link Fragment} subclass.
 */
public class DamageCalcFragment extends Fragment {

    @BindView(R.id.damage_weapon_card)
    CardView damageWeaponCard;

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @BindView(R.id.damage_helmet_card)
    CardView helmetCard;

    @BindView(R.id.helmetItem64)
    ImageView helmetIV;

    @BindView(R.id.helmetItemName)
    TextView helmetName;

    DamageCalcPlayer mDamageCalcPlayer = new DamageCalcPlayer();

    @BindView(R.id.damage_vest_card)
    CardView vestCard;

    @BindView(R.id.vestItem64)
    ImageView vestIV;

    @BindView(R.id.vestItemName)
    TextView vestName;

    @BindView(R.id.weaponItem64)
    ImageView weaponIV;

    @BindView(R.id.weaponItemName)
    TextView weaponName;

    int youEnemy = -1;

    private FirebaseStorage storage = FirebaseStorage.getInstance();

    public DamageCalcFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_damage_calc, container, false);
        ButterKnife.bind(this, view);
        // Inflate the layout for this fragment

        Glide.with(getActivity().getApplicationContext()).load(R.drawable.icons8_rifle).into(weaponIV);
        Glide.with(getActivity().getApplicationContext()).load(R.drawable.icons8_helmet).into(helmetIV);
        Glide.with(getActivity().getApplicationContext()).load(R.drawable.vest1).into(vestIV);

        if (getArguments().containsKey("youEnemy")) {
            youEnemy = getArguments().getInt("youEnemy");
        }

        damageWeaponCard.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View view) {
                Intent intent = new Intent(getActivity(), DamageCalcPicker.class);
                intent.putExtra("itemType", 0);
                startActivityForResult(intent, 1);
            }
        });

        helmetCard.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View view) {
                Intent intent = new Intent(getActivity(), DamageCalcPicker.class);
                intent.putExtra("itemType", 1);
                startActivityForResult(intent, 2);
            }
        });

        vestCard.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View view) {
                Intent intent = new Intent(getActivity(), DamageCalcPicker.class);
                intent.putExtra("itemType", 2);
                startActivityForResult(intent, 3);
            }
        });

        helmetCard.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(final View view) {
                mDamageCalcPlayer.setHelmet(null);
                if (youEnemy == 0) {
                    ((DamageCalcActivity) getActivity()).updatePlayerStats(mDamageCalcPlayer);
                } else {
                    ((DamageCalcActivity) getActivity()).updateEnemyStats(mDamageCalcPlayer);
                }

                helmetName.setText("Click to Pick");
                helmetIV.setImageDrawable(null);
                return true;
            }
        });

        vestCard.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(final View view) {
                mDamageCalcPlayer.setVest(null);
                if (youEnemy == 0) {
                    ((DamageCalcActivity) getActivity()).updatePlayerStats(mDamageCalcPlayer);
                } else {
                    ((DamageCalcActivity) getActivity()).updateEnemyStats(mDamageCalcPlayer);
                }

                vestName.setText("Click to Pick");
                vestIV.setImageDrawable(null);
                return true;
            }
        });

        return view;
    }


    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //1 Weapon, 2 Helmet, 3 Vest
        if (requestCode == 1 && resultCode == getActivity().RESULT_OK) {
            db.document(data.getStringExtra("pickedWeapon"))
                    .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(final DocumentSnapshot documentSnapshot,
                                final FirebaseFirestoreException e) {
                            if (documentSnapshot != null && documentSnapshot.exists() && getActivity().getApplicationContext() != null) {
                                if (documentSnapshot.contains("icon")) {
                                    StorageReference gsReference = storage
                                            .getReferenceFromUrl(documentSnapshot.getString("icon"));
                                    Glide.with(getActivity().getApplicationContext()).using(new FirebaseImageLoader()).load(gsReference)
                                            .into(weaponIV);
                                }

                                if (documentSnapshot.contains("weapon_name")) {
                                    weaponName.setText(documentSnapshot.getString("weapon_name").toUpperCase());
                                }

                                mDamageCalcPlayer.setWeapon(documentSnapshot);
                                if (youEnemy == 0) {
                                    ((DamageCalcActivity) getActivity()).updatePlayerStats(mDamageCalcPlayer);
                                } else {
                                    ((DamageCalcActivity) getActivity()).updateEnemyStats(mDamageCalcPlayer);
                                }
                            }
                        }
                    });
        }

        if (requestCode == 2 && resultCode == getActivity().RESULT_OK) {
            db.document(data.getStringExtra("pickedWeapon"))
                    .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(final DocumentSnapshot documentSnapshot,
                                final FirebaseFirestoreException e) {
                            if (documentSnapshot != null && documentSnapshot.exists()) {
                                if (documentSnapshot.contains("icon")) {
                                    StorageReference gsReference = storage
                                            .getReferenceFromUrl(documentSnapshot.getString("icon"));
                                    Glide.with(getActivity().getApplicationContext()).using(new FirebaseImageLoader()).load(gsReference)
                                            .into(helmetIV);
                                }

                                if (documentSnapshot.contains("name")) {
                                    helmetName.setText(documentSnapshot.getString("name").toUpperCase());
                                }

                                mDamageCalcPlayer.setHelmet(documentSnapshot);
                                if (youEnemy == 0) {
                                    ((DamageCalcActivity) getActivity()).updatePlayerStats(mDamageCalcPlayer);
                                } else {
                                    ((DamageCalcActivity) getActivity()).updateEnemyStats(mDamageCalcPlayer);
                                }
                            }
                        }
                    });
        }

        if (requestCode == 3 && resultCode == getActivity().RESULT_OK) {
            db.document(data.getStringExtra("pickedWeapon"))
                    .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(final DocumentSnapshot documentSnapshot,
                                final FirebaseFirestoreException e) {
                            if (documentSnapshot != null && documentSnapshot.exists()) {
                                if (documentSnapshot.contains("icon")) {
                                    StorageReference gsReference = storage
                                            .getReferenceFromUrl(documentSnapshot.getString("icon"));
                                    Glide.with(getActivity().getApplicationContext()).using(new FirebaseImageLoader()).load(gsReference)
                                            .into(vestIV);
                                }

                                if (documentSnapshot.contains("name")) {
                                    vestName.setText(documentSnapshot.getString("name").toUpperCase());
                                }

                                mDamageCalcPlayer.setVest(documentSnapshot);
                                if (youEnemy == 0) {
                                    ((DamageCalcActivity) getActivity()).updatePlayerStats(mDamageCalcPlayer);
                                } else {
                                    ((DamageCalcActivity) getActivity()).updateEnemyStats(mDamageCalcPlayer);
                                }
                            }
                        }
                    });
        }
    }
}
