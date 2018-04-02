package com.austinhodak.pubgcenter.weapons;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.afollestad.materialdialogs.MaterialDialog;
import com.austinhodak.pubgcenter.R;
import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.ArrayList;
import java.util.List;
import net.idik.lib.slimadapter.SlimAdapter;
import net.idik.lib.slimadapter.SlimInjector;
import net.idik.lib.slimadapter.viewinjector.IViewInjector;

public class WeaponDetailOverview extends Fragment {

    @BindView(R.id.weaponAmmoTV)
    TextView ammoTV;

    @BindView(R.id.weaponDetailAttachmentRV)
    RecyclerView attachmentsRV;

    @BindView(R.id.weaponBurstDelayTV)
    TextView burstDelayTV;

    @BindView(R.id.weaponBurstShotTV)
    TextView burstShotTV;

    @BindView(R.id.weaponBaseDamageTV)
    TextView damageBaseTV;

    @BindView(R.id.weaponBody0Card)
    CardView damageBody0Card;

    @BindView(R.id.weaponBody0TV)
    TextView damageBody0TV;

    @BindView(R.id.weaponBody1Card)
    CardView damageBody1Card;

    @BindView(R.id.weaponBody1TV)
    TextView damageBody1TV;

    @BindView(R.id.weaponBody2Card)
    CardView damageBody2Card;

    @BindView(R.id.weaponBody2TV)
    TextView damageBody2TV;

    @BindView(R.id.weaponBody3Card)
    CardView damageBody3Card;

    @BindView(R.id.weaponBody3TV)
    TextView damageBody3TV;

    @BindView(R.id.weaponHead0Card)
    CardView damageHead0Card;

    @BindView(R.id.weaponHead0TV)
    TextView damageHead0TV;

    @BindView(R.id.weaponHead1Card)
    CardView damageHead1Card;

    @BindView(R.id.weaponHead1TV)
    TextView damageHead1TV;

    @BindView(R.id.weaponHead2Card)
    CardView damageHead2Card;

    @BindView(R.id.weaponHead2TV)
    TextView damageHead2TV;

    @BindView(R.id.weaponHead3Card)
    CardView damageHead3Card;

    @BindView(R.id.weaponHead3TV)
    TextView damageHead3TV;

    @BindView(R.id.weaponFiringModeTV)
    TextView firingModeTV;

    @BindView(R.id.adView)
    AdView mAdView;

    FirebaseAnalytics mFirebaseAnalytics;

    @BindView(R.id.scrollView)
    NestedScrollView mNestedScrollView;

    @BindView(R.id.weaponMagSize)
    TextView magSizeTV;

    @BindView(R.id.weaponMiscPickup)
    TextView pickupTV;

    @BindView(R.id.weaponPowerTV)
    TextView powerTV;

    @BindView(R.id.weaponRangeTV)
    TextView rangeTV;

    @BindView(R.id.weaponMiscReady)
    TextView readyDelayTV;

    @BindView(R.id.weaponReloadDurFullTV)
    TextView reloadFullTV;

    @BindView(R.id.weaponReloadMethodTV)
    TextView reloadMethodTV;

    @BindView(R.id.weaponReloadDurTacTV)
    TextView reloadTacTV;

    @BindView(R.id.weaponSpeedTV)
    TextView speedTV;

    @BindView(R.id.suggest_button)
    Button suggestButton;

    @BindView(R.id.weaponTBSTV)
    TextView tbsTV;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private FirebaseStorage storage = FirebaseStorage.getInstance();

    public WeaponDetailOverview() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_weapons_tab, container, false);
        ButterKnife.bind(this, view);
        // Inflate the layout for this fragment

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());

        if (getArguments() != null) {
            loadWeapon(getArguments().getString("weaponPath"), getArguments().getString("weaponClass"));
        }

        CardView cardView = (CardView) ((ViewGroup) tbsTV.getParent()).getParent();
        cardView.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(final View view) {
                new MaterialDialog.Builder(getActivity())
                        .title("Time Between Shots")
                        .positiveColor(getResources().getColor(R.color.md_white_1000))
                        .content(
                                "When firing at maximum speed, the time between shots.")
                        .positiveText("CLOSE")
                        .show();
                return false;
            }
        });

        attachmentsRV.setNestedScrollingEnabled(false);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AdRequest adRequest = new AdRequest.Builder().build();
        //mAdView.loadAd(adRequest);

        suggestButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View view) {
                String mailto = "mailto:fireappsdev@gmail.com" +
                        "?subject=" + Uri.encode("PUBG BattleGuide Suggestion");

                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setData(Uri.parse(mailto));
                startActivity(emailIntent);
            }
        });
    }

    private void loadAttachments(final DocumentSnapshot data) {
        if (!data.contains("attachments")) {
            return;
        }

        final List<Object> attachmentsData = new ArrayList<>();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        attachmentsRV.setLayoutManager(linearLayoutManager);
        final SlimAdapter adapter = SlimAdapter.create()
                .register(R.layout.weapon_list_item, new SlimInjector<DocumentSnapshot>() {
                    @Override
                    public void onInject(final DocumentSnapshot data, final IViewInjector injector) {

                        if (!data.exists()) {
                            return;
                        }

                        if (data.get("icon") != null) {
                            StorageReference gsReference = storage
                                    .getReferenceFromUrl(data.getString("icon"));

                            Glide.with(getActivity())
                                    .using(new FirebaseImageLoader())
                                    .load(gsReference)
                                    .into((ImageView) injector.findViewById(R.id.helmetItem64));

                        }

                        if (data.contains("name")) {
                            injector.text(R.id.weaponItemName, data.getString("name"));
                        }

                        if (data.contains("location")) {
                            injector.text(R.id.weaponItemSubtitle, data.getString("location"));
                        }
                    }
                }).attachTo(attachmentsRV);

        List<DocumentReference> list = (List<DocumentReference>) data.get("attachments");
        Log.d("LIST", list.get(0).getId());

        for (DocumentReference documentReference : list) {
            documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(final DocumentSnapshot attachment, final FirebaseFirestoreException e) {
                    attachmentsData.add(attachment);
                    adapter.updateData(attachmentsData);
                }
            });
        }
    }

    private void loadDamageStats(final DocumentSnapshot data) {
        data.getReference().collection("stats").document("damage").addSnapshotListener(
                new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(final DocumentSnapshot damageData,
                            final FirebaseFirestoreException e) {

                        List<CardView> cardViewList = new ArrayList<>();
                        cardViewList.add(damageBody0Card);
                        cardViewList.add(damageBody1Card);
                        cardViewList.add(damageBody2Card);
                        cardViewList.add(damageBody3Card);
                        cardViewList.add(damageHead0Card);
                        cardViewList.add(damageHead1Card);
                        cardViewList.add(damageHead2Card);
                        cardViewList.add(damageHead3Card);

                        for (final CardView cardView : cardViewList) {
                            cardView.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(final View view) {
                                    String title = "";
                                    String desc = "";
                                    int color;

                                    if (cardView.getCardBackgroundColor().getDefaultColor() == getResources()
                                            .getColor(R.color.md_red_500)) {
                                        title = "1 Hit to Kill";
                                        color = getResources().getColor(R.color.md_red_500);
                                    } else if (cardView.getCardBackgroundColor().getDefaultColor() == getResources()
                                            .getColor(R.color.md_deep_orange_500)) {
                                        title = "2 Hits to Kill";
                                        color = getResources().getColor(R.color.md_deep_orange_500);
                                    } else if (cardView.getCardBackgroundColor().getDefaultColor() == getResources()
                                            .getColor(R.color.md_amber_500)) {
                                        title = "3 Hits to Kill";
                                        color = getResources().getColor(R.color.md_amber_500);
                                    } else if (cardView.getCardBackgroundColor().getDefaultColor() == getResources()
                                            .getColor(R.color.md_green_500)) {
                                        title = "4 Hits to Kill";
                                        color = getResources().getColor(R.color.md_green_500);
                                    } else {
                                        title = "5+ Hits to Kill";
                                        color = getResources().getColor(R.color.md_grey_800);
                                    }

                                    new MaterialDialog.Builder(getActivity())
                                            .title(title)
                                            .backgroundColor(color)
                                            .positiveColor(getResources().getColor(R.color.md_white_1000))
                                            .contentColor(getResources().getColor(R.color.md_white_1000))
                                            .content(
                                                    "This is assuming the shooter is within normal range of the gun used.")
                                            .positiveText("CLOSE")
                                            .show();
                                }
                            });
                        }

                        if (getActivity() == null) {
                            return;
                        }

                        if (damageData != null && damageData.exists()) {
                            if (damageData.getString("body0") != null) {
                                damageBody0TV.setText(damageData.getString("body0"));
                                if (damageData.getString("body0HTK") != null) {
                                    switch (damageData.getString("body0HTK")) {
                                        case "1":
                                            damageBody0Card.setCardBackgroundColor(
                                                    getResources().getColor(R.color.md_red_500));
                                            break;
                                        case "2":
                                            damageBody0Card.setCardBackgroundColor(
                                                    getResources().getColor(R.color.md_deep_orange_500));
                                            break;
                                        case "3":
                                            damageBody0Card.setCardBackgroundColor(
                                                    getResources().getColor(R.color.md_amber_500));
                                            break;
                                        case "4":
                                            damageBody0Card.setCardBackgroundColor(
                                                    getResources().getColor(R.color.md_green_500));
                                            break;
                                    }
                                }
                            }

                            if (damageData.getString("body1") != null) {
                                TextView tv = damageBody1TV;
                                CardView cardView = damageBody1Card;
                                tv.setText(damageData.getString("body1"));
                                if (damageData.getString("body1HTK") != null) {
                                    switch (damageData.getString("body1HTK")) {
                                        case "1":
                                            cardView.setCardBackgroundColor(
                                                    getResources().getColor(R.color.md_red_500));
                                            break;
                                        case "2":
                                            cardView.setCardBackgroundColor(
                                                    getResources().getColor(R.color.md_deep_orange_500));
                                            break;
                                        case "3":
                                            cardView.setCardBackgroundColor(
                                                    getResources().getColor(R.color.md_amber_500));
                                            break;
                                        case "4":
                                            cardView.setCardBackgroundColor(
                                                    getResources().getColor(R.color.md_green_500));
                                            break;
                                    }
                                }
                            }

                            if (damageData.getString("body2") != null) {
                                TextView tv = damageBody2TV;
                                CardView cardView = damageBody2Card;
                                tv.setText(damageData.getString("body2"));
                                if (damageData.getString("body2HTK") != null) {
                                    switch (damageData.getString("body2HTK")) {
                                        case "1":
                                            cardView.setCardBackgroundColor(
                                                    getResources().getColor(R.color.md_red_500));
                                            break;
                                        case "2":
                                            cardView.setCardBackgroundColor(
                                                    getResources().getColor(R.color.md_deep_orange_500));
                                            break;
                                        case "3":
                                            cardView.setCardBackgroundColor(
                                                    getResources().getColor(R.color.md_amber_500));
                                            break;
                                        case "4":
                                            cardView.setCardBackgroundColor(
                                                    getResources().getColor(R.color.md_green_500));
                                            break;
                                    }
                                }
                            }

                            if (damageData.getString("body3") != null) {
                                TextView tv = damageBody3TV;
                                CardView cardView = damageBody3Card;
                                tv.setText(damageData.getString("body3"));
                                if (damageData.getString("body3HTK") != null) {
                                    switch (damageData.getString("body3HTK")) {
                                        case "1":
                                            cardView.setCardBackgroundColor(
                                                    getResources().getColor(R.color.md_red_500));
                                            break;
                                        case "2":
                                            cardView.setCardBackgroundColor(
                                                    getResources().getColor(R.color.md_deep_orange_500));
                                            break;
                                        case "3":
                                            cardView.setCardBackgroundColor(
                                                    getResources().getColor(R.color.md_amber_500));
                                            break;
                                        case "4":
                                            cardView.setCardBackgroundColor(
                                                    getResources().getColor(R.color.md_green_500));
                                            break;
                                    }
                                }

                            }

                            if (damageData.getString("head0") != null) {
                                TextView tv = damageHead0TV;
                                CardView cardView = damageHead0Card;
                                tv.setText(damageData.getString("head0"));
                                if (damageData.getString("head0HTK") != null) {
                                    switch (damageData.getString("head0HTK")) {
                                        case "1":
                                            cardView.setCardBackgroundColor(
                                                    getResources().getColor(R.color.md_red_500));
                                            break;
                                        case "2":
                                            cardView.setCardBackgroundColor(
                                                    getResources().getColor(R.color.md_deep_orange_500));
                                            break;
                                        case "3":
                                            cardView.setCardBackgroundColor(
                                                    getResources().getColor(R.color.md_amber_500));
                                            break;
                                        case "4":
                                            cardView.setCardBackgroundColor(
                                                    getResources().getColor(R.color.md_green_500));
                                            break;
                                    }
                                }

                            }

                            if (damageData.getString("head1") != null) {
                                TextView tv = damageHead1TV;
                                CardView cardView = damageHead1Card;
                                tv.setText(damageData.getString("head1"));
                                if (damageData.getString("head1HTK") != null) {
                                    switch (damageData.getString("head1HTK")) {
                                        case "1":
                                            cardView.setCardBackgroundColor(
                                                    getResources().getColor(R.color.md_red_500));
                                            break;
                                        case "2":
                                            cardView.setCardBackgroundColor(
                                                    getResources().getColor(R.color.md_deep_orange_500));
                                            break;
                                        case "3":
                                            cardView.setCardBackgroundColor(
                                                    getResources().getColor(R.color.md_amber_500));
                                            break;
                                        case "4":
                                            cardView.setCardBackgroundColor(
                                                    getResources().getColor(R.color.md_green_500));
                                            break;
                                    }
                                }
                            }

                            if (damageData.getString("head2") != null) {
                                TextView tv = damageHead2TV;
                                CardView cardView = damageHead2Card;
                                tv.setText(damageData.getString("head2"));
                                if (damageData.getString("head2HTK") != null) {
                                    switch (damageData.getString("head2HTK")) {
                                        case "1":
                                            cardView.setCardBackgroundColor(
                                                    getResources().getColor(R.color.md_red_500));
                                            break;
                                        case "2":
                                            cardView.setCardBackgroundColor(
                                                    getResources().getColor(R.color.md_deep_orange_500));
                                            break;
                                        case "3":
                                            cardView.setCardBackgroundColor(
                                                    getResources().getColor(R.color.md_amber_500));
                                            break;
                                        case "4":
                                            cardView.setCardBackgroundColor(
                                                    getResources().getColor(R.color.md_green_500));
                                            break;
                                    }
                                }
                            }

                            if (damageData.getString("head3") != null) {
                                TextView tv = damageHead3TV;
                                CardView cardView = damageHead3Card;
                                tv.setText(damageData.getString("head3"));
                                if (damageData.getString("head3HTK") != null) {
                                    switch (damageData.getString("head3HTK")) {
                                        case "1":
                                            cardView.setCardBackgroundColor(
                                                    getResources().getColor(R.color.md_red_500));
                                            break;
                                        case "2":
                                            cardView.setCardBackgroundColor(
                                                    getResources().getColor(R.color.md_deep_orange_500));
                                            break;
                                        case "3":
                                            cardView.setCardBackgroundColor(
                                                    getResources().getColor(R.color.md_amber_500));
                                            break;
                                        case "4":
                                            cardView.setCardBackgroundColor(
                                                    getResources().getColor(R.color.md_green_500));
                                            break;
                                    }
                                }
                            }
                        }
                    }
                });
    }

    private void loadWeapon(final String weaponID, final String weaponClass) {
        db.document(weaponID)
                .addSnapshotListener(
                        new EventListener<DocumentSnapshot>() {
                            @Override
                            public void onEvent(final DocumentSnapshot data,
                                    final FirebaseFirestoreException e) {
                                if (e == null && data != null && data.exists()) {

                                    Bundle bundle = new Bundle();
                                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, data.getId());
                                    bundle.putString(FirebaseAnalytics.Param.ITEM_NAME,
                                            data.getString("weapon_name"));
                                    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "weapon_view");
                                    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                                    if (data.getString("ammo") != null) {
                                        ammoTV.setText(data.getString("ammo"));
                                    }

                                    if (data.getString("ammoPerMag") != null) {
                                        magSizeTV.setText(data.getString("ammoPerMag"));
                                    }

                                    if (data.getString("damageBody0") != null) {
                                        damageBaseTV.setText(data.getString("damageBody0"));
                                    }

                                    if (data.getString("speed") != null) {
                                        speedTV.setText(data.getString("speed"));
                                    }

                                    if (data.getString("power") != null) {
                                        powerTV.setText(data.getString("power"));
                                    }

                                    if (data.getString("range") != null) {
                                        rangeTV.setText(data.getString("range"));
                                    }

                                    if (data.getString("TBS") != null) {
                                        tbsTV.setText(data.getString("TBS"));
                                    }

                                    if (data.getString("burstShots") != null) {
                                        burstShotTV.setText(data.getString("burstShots"));
                                    }

                                    if (data.getString("burstDelay") != null) {
                                        burstDelayTV.setText(data.getString("burstDelay"));
                                    }

                                    if (data.getString("firingModes") != null) {
                                        firingModeTV.setText(data.getString("firingModes").toUpperCase());
                                    }

                                    if (data.getString("reloadDurationFull") != null) {
                                        reloadFullTV.setText(data.getString("reloadDurationFull"));
                                    }

                                    if (data.getString("reloadDurationTac") != null) {
                                        reloadTacTV.setText(data.getString("reloadDurationTac"));
                                    }

                                    if (data.getString("reloadMethod") != null) {
                                        reloadMethodTV.setText(data.getString("reloadMethod").toUpperCase());
                                    }

                                    if (data.getString("pickupDelay") != null) {
                                        pickupTV.setText(data.getString("pickupDelay"));
                                    }

                                    if (data.getString("readyDelay") != null) {
                                        readyDelayTV.setText(data.getString("readyDelay").toUpperCase());
                                    }

                                    loadDamageStats(data);
                                    loadAttachments(data);

                                } else {

                                }
                            }
                        });
    }

}
