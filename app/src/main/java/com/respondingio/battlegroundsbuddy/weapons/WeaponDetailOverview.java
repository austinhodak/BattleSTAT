package com.respondingio.battlegroundsbuddy.weapons;

import static android.content.Context.MODE_PRIVATE;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.bumptech.glide.Glide;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.analytics.FirebaseAnalytics.Event;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FileDownloadTask.TaskSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.respondingio.battlegroundsbuddy.R;
import com.respondingio.battlegroundsbuddy.models.WeaponSound;
import com.respondingio.battlegroundsbuddy.snacky.Snacky;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
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

    @BindView(R.id.weapon_desc_arrow)
    ImageView descArrow;

    @BindView(R.id.weapon_desc_card_content)
    LinearLayout descContentLayout;

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

    @BindView(R.id.weapon_desc_card)
    CardView weaponDescCard;

    @BindView(R.id.weapon_desc_layout)
    LinearLayout weaponDescLayout;

    @BindView(R.id.desc_text)
    TextView weaponDescText;

    @BindView(R.id.wiki_button)
    Button wikiButton;

    @BindView(R.id.buggy_shots)
    TextView buggyShots;

    @BindView(R.id.dacia_shots)
    TextView daciaShots;

    @BindView(R.id.mc_shots)
    TextView mcShots;

    @BindView(R.id.boat_shots)
    TextView boatShots;

    @BindView(R.id.uaz_shots)
    TextView uazShots;

    @BindView(R.id.burst_layout) LinearLayout burstLayout;

    @BindView(R.id.weaponDetailSoundsHeader) TextView soundsHeader;
    @BindView(R.id.weaponDetailSoundsCard) CardView soundsCard;
    @BindView(R.id.weaponDetailSoundsList) RecyclerView soundsList;

    @BindView(R.id.overview_full_stats) CardView fullDamageCard;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private SharedPreferences mSharedPreferences;

    private boolean snackbarShown = false;

    private SlimAdapter soundsAdapter;

    private FirebaseStorage storage = FirebaseStorage.getInstance();

    private List<Object> soundsData = new ArrayList<>();

    public WeaponDetailOverview() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_weapons_tab, container, false);
        ButterKnife.bind(this, view);
        // Inflate the layout for this fragment

        setupSoundsList();

        if (getActivity() != null)
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());

        if (getArguments() != null) {
            loadWeapon(getArguments().getString("weaponPath"));
        }

        mSharedPreferences = getActivity().getSharedPreferences("com.austinhodak.pubgcenter", MODE_PRIVATE);


        weaponDescCard.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (descContentLayout.getVisibility() == View.GONE) {
                    descContentLayout.setVisibility(View.VISIBLE);

                    descArrow.setImageDrawable(getResources().getDrawable(R.drawable.ic_arrow_drop_up_24dp));
                } else {
                    descContentLayout.setVisibility(View.GONE);

                    descArrow.setImageDrawable(getResources().getDrawable(R.drawable.ic_arrow_drop_down_24dp));
                }
            }
        });

        attachmentsRV.setNestedScrollingEnabled(false);

        if (mSharedPreferences != null && !mSharedPreferences.getBoolean("removeAds", false)) {
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    loadAds();
                }
            }, 2000);
        }

        fullDamageCard.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View view) {
                Intent intent = new Intent(requireActivity(),
                        WeaponDamageChart.class);
                intent.putExtra("weaponPath", getArguments().getString("weaponPath"));
                intent.putExtra("weaponKey",
                        getArguments().getString("weaponKey"));
                intent.putExtra("weaponClass", getArguments().getString("weaponClass"));
                intent.putExtra("weaponName", getArguments().getString("weaponName"));
                startActivity(intent);
            }
        });

        return view;
    }

    private void loadAds() {
        Log.d("ADS", "loadAds: WeaponDetailOverview");
        mAdView.setVisibility(View.GONE);

        AdRequest adRequest;

        if (!mSharedPreferences.getBoolean("personalized_ads", false)) {
            Bundle extra = new Bundle();
            extra.putString("npa", "1");
            adRequest = new AdRequest.Builder().addNetworkExtrasBundle(AdMobAdapter.class, extra).build();
        } else {
            adRequest = new AdRequest.Builder().build();
        }

        mAdView.loadAd(adRequest);
        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                mAdView.setVisibility(View.VISIBLE);
                // Code to be executed when an ad finishes
                // loading.
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                // Code to be executed when an ad request fails.
                mAdView.setVisibility(View.GONE);
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
            }

            @Override
            public void onAdLeftApplication() {
                // Code to be executed when the user has left the app.
            }

            @Override
            public void onAdClosed() {
                // Code to be executed when when the user is about to return
                // to the app after tapping on an ad.
            }
        });
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        suggestButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View view) {
                try {
                    String mailto = "mailto:pubgbattlebuddy@gmail.com" +
                            "?subject=" + Uri.encode("Battlegrounds Battle Buddy Suggestion");

                    Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                    emailIntent.setData(Uri.parse(mailto));
                    startActivity(emailIntent);
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                    Snacky.builder().setView(getActivity().findViewById(R.id.rl)).error().setDuration(4000)
                            .setText("Email app not found.")
                            .show();
                }
            }
        });
    }

    private void loadAttachments(final DocumentSnapshot data) {
        if (!data.contains("attachments")) {
            return;
        }

        final List<DocumentSnapshot> attachmentsData = new ArrayList<>();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        attachmentsRV.setLayoutManager(linearLayoutManager);
        final SlimAdapter adapter = SlimAdapter.create()
                .register(R.layout.weapon_list_item, new SlimInjector<DocumentSnapshot>() {
                    @Override
                    public void onInject(@NonNull final DocumentSnapshot data, @NonNull final IViewInjector injector) {

                        if (!data.exists()) {
                            return;
                        }

                        if (data.get("icon") != null) {
                            StorageReference gsReference = storage
                                    .getReferenceFromUrl(data.getString("icon"));

                            if (getActivity() != null)
                            Glide.with(getActivity())
                                    .load(gsReference)
                                    .into((ImageView) injector.findViewById(R.id.helmetItem64));

                        }

                        if (data.contains("name")) {
                            injector.text(R.id.weaponItemName, data.getString("name"));
                        }

                        if (data.contains("location")) {
                            injector.text(R.id.weaponItemSubtitle, data.getString("location"));
                        }

                        injector.clicked(R.id.top_layout, new OnClickListener() {
                            @Override
                            public void onClick(final View v) {
                                if (data.contains("stats")) {
                                    String stats = "";
                                    stats = data.getString("stats").replaceAll("<br>", "");
                                    stats = stats.replace(" +", "\n+");
                                    stats = stats.replace(" -", "\n-");
//                                    MaterialDialog materialDialog = new MaterialDialog(getActivity())
//                                            .title(null, data.getString("name"))
//                                            .message(null, stats)
//                                            .positiveButton(null, "OK", null)
//                                            .show();
                                }
                            }
                        });

                    }
                }).attachTo(attachmentsRV);


        @SuppressWarnings("unchecked")
        List<DocumentReference> list = (List<DocumentReference>) data.get("attachments");
        for (DocumentReference documentReference : list) {
            documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(final DocumentSnapshot attachment, final FirebaseFirestoreException e) {
                    attachmentsData.add(attachment);

                    try {
                        Collections.sort(attachmentsData, new Comparator<DocumentSnapshot>() {
                            @Override
                            public int compare(DocumentSnapshot s1, DocumentSnapshot s2) {
                                return s1.getString("name").compareToIgnoreCase(s2.getString("name"));
                            }
                        });
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }

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
                                    String title;
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

                                    //if (getActivity() != null)
//                                    new MaterialDialog.Builder(getActivity())
//                                            .title(title)
//                                            .backgroundColor(color)
//                                            .positiveColor(getResources().getColor(R.color.md_white_1000))
//                                            .contentColor(getResources().getColor(R.color.md_white_1000))
//                                            .content(
//                                                    "This is assuming the shooter is within normal range of the gun used.")
//                                            .positiveText("CLOSE")
//                                            .show();
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

    private void loadWeapon(final String weaponID) {
        db.document(weaponID)
                .addSnapshotListener(
                        new EventListener<DocumentSnapshot>() {
                            @Override
                            public void onEvent(final DocumentSnapshot data,
                                    final FirebaseFirestoreException e) {
                                if (e == null && data != null && data.exists()) {

                                    if (data.contains("desc") && data.get("desc") != null) {
                                        if (data.contains("wiki") && data.get("wiki") != null) {
                                            wikiButton.setOnClickListener(new OnClickListener() {
                                                @Override
                                                public void onClick(final View v) {
                                                    String url = data.getString("wiki");
                                                    Intent i = new Intent(Intent.ACTION_VIEW);
                                                    i.setData(Uri.parse(url));
                                                    startActivity(i);
                                                }
                                            });
                                        } else {
                                            wikiButton.setVisibility(View.INVISIBLE);
                                        }

                                        String descText = data.getString("desc").replace("<br>", "\n").replace("  ", "\n\n");
//                                        descText = data.getString("desc");
//                                        descText = descText.replace("<br>", "\n");
//                                        descText = descText.replace("  ", "\n\n");

                                        weaponDescText.setText(descText);
                                    } else {
                                        weaponDescLayout.setVisibility(View.GONE);
                                    }

                                    Bundle bundle = new Bundle();
                                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, data.getId());
                                    bundle.putString(FirebaseAnalytics.Param.ITEM_NAME,
                                            data.getString("weapon_name"));
                                    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "weapon_view");
                                    mFirebaseAnalytics.logEvent(Event.VIEW_ITEM, bundle);

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
                                        speedTV.setText(data.getString("speed") + " m/s");
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

                                        if (data.getString("firingModes").contains("BURST")) {
                                            burstLayout.setVisibility(View.VISIBLE);
                                        }
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

                                    if (data.contains("incomplete") && (data.getBoolean("incomplete"))) {
                                        if (getActivity() != null && !snackbarShown) {
                                            Snacky.builder().setView(getActivity().findViewById(R.id.rl)).info().setDuration(2000)
                                                    .setText("Complete stats not available for this weapon.")
                                                    .show();
                                            snackbarShown = true;
                                        }
                                    }

                                    loadDamageStats(data);
                                    loadAttachments(data);
                                    loadVehicleDamage(data);

                                    soundsData.clear();

                                    //SOUNDS
                                    if (data.contains("audio")) {
                                        updateAudioVisibility(View.VISIBLE);
                                        HashMap<String, String> audioObject = (HashMap<String, String>) data.get("audio");
                                        Log.d("WEAPON", audioObject.toString());

                                        for (String s : audioObject.keySet()) {
                                            if (audioObject.get(s).isEmpty()) {
                                                continue;
                                            }
                                            switch (s) {
                                                case "normal-single":
                                                    soundsData.add(new WeaponSound(s, "Normal Single", audioObject.get(s)));
                                                    break;
                                                case "normal-burst":
                                                    soundsData.add(new WeaponSound(s, "Normal Burst", audioObject.get(s)));
                                                    break;
                                                case "normal-auto":
                                                    soundsData.add(new WeaponSound(s, "Normal Auto", audioObject.get(s)));
                                                    break;
                                                case "suppressed-single":
                                                    soundsData.add(new WeaponSound(s, "Suppressed Single", audioObject.get(s)));
                                                    break;
                                                case "suppressed-auto":
                                                    soundsData.add(new WeaponSound(s, "Suppressed Auto", audioObject.get(s)));
                                                    break;
                                                case "suppressed-burst":
                                                    soundsData.add(new WeaponSound(s, "Suppressed Burst", audioObject.get(s)));
                                                    break;
                                                case "reloading":
                                                    soundsData.add(new WeaponSound(s, "Reloading", audioObject.get(s)));
                                                    break;
                                            }
                                        }

                                        Collections.sort(soundsData, new Comparator<Object>() {
                                            @Override
                                            public int compare(final Object o, final Object t1) {
                                                if (o instanceof WeaponSound && t1 instanceof WeaponSound) {
                                                    return ((WeaponSound) o).getTitle().compareToIgnoreCase(((WeaponSound) t1).getTitle());
                                                } else {
                                                    return 0;
                                                }
                                            }
                                        });

                                        soundsAdapter.updateData(soundsData);
                                    } else {
                                        updateAudioVisibility(View.GONE);
                                    }

                                }
                            }
                        });
    }

//    private void setupStatDescListeners() {
//        CardView tbs = (CardView) tbsTV.getParent().getParent();
//        CardView damage = (CardView) damageBaseTV.getParent().getParent();
//        CardView speed = (CardView) speedTV.getParent().getParent();
//        CardView power = (CardView) powerTV.getParent().getParent();
//        CardView range = (CardView) rangeTV.getParent().getParent();
//        CardView burstShots = (CardView) burstShotTV.getParent().getParent();
//        CardView burstDelay = (CardView) burstDelayTV.getParent().getParent();
//        CardView ammoPerMag = (CardView) magSizeTV.getParent().getParent();
//        CardView firingModes = (CardView) firingModeTV.getParent().getParent();
//
//        if (getActivity() == null) return;
//
//        tbs.setOnLongClickListener(new OnLongClickListener() {
//            @Override
//            public boolean onLongClick(final View view) {
//                new MaterialDialog.Builder(getActivity())
//                        .title("Time Between Shots")
//                        .positiveColor(getResources().getColor(R.color.md_white_1000))
//                        .content(
//                                "When firing at maximum speed, the time between shots.")
//                        .positiveText("CLOSE")
//                        .show();
//                return false;
//            }
//        });
//
//        damage.setOnLongClickListener(new OnLongClickListener() {
//            @Override
//            public boolean onLongClick(final View view) {
//                new MaterialDialog.Builder(getActivity())
//                        .title("Hit Damage")
//                        .positiveColor(getResources().getColor(R.color.md_white_1000))
//                        .content(
//                                "Direct damage applied before armor and other mitigating factors.")
//                        .positiveText("CLOSE")
//                        .show();
//                return false;
//            }
//        });
//
//        speed.setOnLongClickListener(new OnLongClickListener() {
//            @Override
//            public boolean onLongClick(final View view) {
//                new MaterialDialog.Builder(getActivity())
//                        .title("Initial Bullet Speed")
//                        .positiveColor(getResources().getColor(R.color.md_white_1000))
//                        .content(
//                                "The speed at which the bullet leaves the muzzle, affecting range and damage.")
//                        .positiveText("CLOSE")
//                        .show();
//                return false;
//            }
//        });
//
//        power.setOnLongClickListener(new OnLongClickListener() {
//            @Override
//            public boolean onLongClick(final View view) {
//                new MaterialDialog.Builder(getActivity())
//                        .title("Body Hit Impact Power")
//                        .positiveColor(getResources().getColor(R.color.md_white_1000))
//                        .content(
//                                "Damage taken to an unshielded, unarmored body.")
//                        .positiveText("CLOSE")
//                        .show();
//                return false;
//            }
//        });
//
//        range.setOnLongClickListener(new OnLongClickListener() {
//            @Override
//            public boolean onLongClick(final View view) {
//                new MaterialDialog.Builder(getActivity())
//                        .title("Zero Range")
//                        .positiveColor(getResources().getColor(R.color.md_white_1000))
//                        .content(
//                                "The range of effectiveness.")
//                        .positiveText("CLOSE")
//                        .show();
//                return false;
//            }
//        });
//
//        burstShots.setOnLongClickListener(new OnLongClickListener() {
//            @Override
//            public boolean onLongClick(final View view) {
//                new MaterialDialog.Builder(getActivity())
//                        .title("Burst Shots")
//                        .positiveColor(getResources().getColor(R.color.md_white_1000))
//                        .content(
//                                "If allowed, how many shots are fired during burst.")
//                        .positiveText("CLOSE")
//                        .show();
//                return false;
//            }
//        });
//
//        burstDelay.setOnLongClickListener(new OnLongClickListener() {
//            @Override
//            public boolean onLongClick(final View view) {
//                new MaterialDialog.Builder(getActivity())
//                        .title("Burst Delay")
//                        .positiveColor(getResources().getColor(R.color.md_white_1000))
//                        .content(
//                                "The delay of these shots.")
//                        .positiveText("CLOSE")
//                        .show();
//                return false;
//            }
//        });
//
//        ammoPerMag.setOnLongClickListener(new OnLongClickListener() {
//            @Override
//            public boolean onLongClick(final View view) {
//                new MaterialDialog.Builder(getActivity())
//                        .title("Ammo Per Mag")
//                        .positiveColor(getResources().getColor(R.color.md_white_1000))
//                        .content(
//                                "The maximum amount of ammunition that can be held in a magazine.")
//                        .positiveText("CLOSE")
//                        .show();
//                return false;
//            }
//        });
//
//        firingModes.setOnLongClickListener(new OnLongClickListener() {
//            @Override
//            public boolean onLongClick(final View view) {
//                new MaterialDialog.Builder(getActivity())
//                        .title("Firing Modes")
//                        .positiveColor(getResources().getColor(R.color.md_white_1000))
//                        .content(
//                                "The modes this weapon can select.")
//                        .positiveText("CLOSE")
//                        .show();
//                return false;
//            }
//        });
//    }

    private void loadVehicleDamage(DocumentSnapshot data) {
        if (data.getString("damageBody0") == null) {
            return;
        }
        try {
            float damage = Float.parseFloat(data.getString("damageBody0"));

            String buggy = String.format("%.0f", Math.ceil(1540/damage)) + " Shots";
            String dacia = String.format("%.0f", Math.ceil(1820/damage)) + " Shots";
            String mc = String.format("%.0f", Math.ceil(1025/damage)) + " Shots";
            String boat = String.format("%.0f", Math.ceil(1520/damage)) + " Shots";
            String uaz = String.format("%.0f", Math.ceil(1820/damage)) + " Shots";

            buggyShots.setText(buggy);
            daciaShots.setText(dacia);
            mcShots.setText(mc);
            boatShots.setText(boat);
            uazShots.setText(uaz);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    private void updateAudioVisibility(int vis) {
        soundsCard.setVisibility(vis);
        soundsHeader.setVisibility(vis);
        soundsList.setVisibility(vis);
    }

    private void setupSoundsList() {
        soundsList.setLayoutManager(new LinearLayoutManager(requireActivity()));
        soundsAdapter = SlimAdapter.create().attachTo(soundsList).register(R.layout.weapon_audio_list_item, new SlimInjector<WeaponSound>() {
            @Override
            public void onInject(final WeaponSound data, final IViewInjector injector) {
                injector.text(R.id.weaponAudioText, data.getTitle());

                final StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(data.getUrl());

                final boolean[] isPlaying = {false};
                final boolean[] isLoaded = {false};

                final MediaPlayer mediaPlayer = new MediaPlayer();
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

                File localFile = null;
                try {
                    localFile = File.createTempFile(data.getValue(), "ogg");
                } catch (IOException e) {
                    e.printStackTrace();
                }

                final File finalLocalFile = localFile;
                injector.clicked(R.id.weaponAudioPlay, new OnClickListener() {
                    @Override
                    public void onClick(final View view) {
                        if (!isPlaying[0]) {
                            isPlaying[0] = true;
                            injector.image(R.id.weaponAudioPlay, R.drawable.ic_pause_circle_filled_white_24dp);
                            try {
                                startPlaying();
                            } catch (IllegalStateException e) {
                                e.printStackTrace();
                            }
                        } else {
                            mediaPlayer.stop();
                            isPlaying[0] = false;
                            injector.image(R.id.weaponAudioPlay, R.drawable.ic_play_circle_filled_white_24dp);
                        }
                    }

                    private void startPlaying() {
                        injector.visible(R.id.audioPg);
                        if (finalLocalFile != null && !isLoaded[0]) {
                            storageReference.getFile(finalLocalFile).addOnSuccessListener(new OnSuccessListener<TaskSnapshot>() {
                                @Override
                                public void onSuccess(final TaskSnapshot taskSnapshot) {
                                    isLoaded[0] = true;
                                    injector.gone(R.id.audioPg);
                                    try {
                                        mediaPlayer.reset();
                                        mediaPlayer.setDataSource(requireActivity(), Uri.parse(finalLocalFile.getAbsolutePath()));
                                        mediaPlayer.prepare();
                                        mediaPlayer.start();
                                    } catch (IOException | IllegalStateException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        } else {
                            injector.gone(R.id.audioPg);
                            try {
                                mediaPlayer.reset();
                                mediaPlayer.setDataSource(requireActivity(), Uri.parse(finalLocalFile.getAbsolutePath()));
                                mediaPlayer.prepare();
                                mediaPlayer.start();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });

                mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
                    @Override
                    public void onCompletion(final MediaPlayer mediaPlayer) {
                        mediaPlayer.stop();
                        if (isPlaying[0]) {
                            isPlaying[0] = false;
                            injector.image(R.id.weaponAudioPlay, R.drawable.ic_play_circle_filled_white_24dp);
                        }
                    }
                });
            }
        });
    }

}
