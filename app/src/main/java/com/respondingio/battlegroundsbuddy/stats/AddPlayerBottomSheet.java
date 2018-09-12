package com.respondingio.battlegroundsbuddy.stats;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.FirebaseFunctionsException;
import com.google.firebase.functions.FirebaseFunctionsException.Code;
import com.google.firebase.functions.HttpsCallableResult;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.respondingio.battlegroundsbuddy.R;
import de.mateware.snacky.Snacky;
import java.util.HashMap;
import java.util.Map;

public class AddPlayerBottomSheet extends BottomSheetDialogFragment {

    private Button continueButton;

    private FirebaseFunctions mFunctions;

    private ProgressBar mProgressBar;

    private String region = "XBOX-AS";

    private MaterialSpinner spinner;

    private EditText usernameText;

    @BindView(R.id.divider2) View divider;

    @BindView(R.id.top_ll) LinearLayout topView;

    public static String[] regionList = {"XBOX-AS", "XBOX-EU", "XBOX-NA", "XBOX-OC", "PC-KRJP", "PC-JP", "PC-NA", "PC-EU", "PC-RU", "PC-OC", "PC-KAKAO", "PC-SEA",
            "PC-SA", "PC-AS"};

    public static String[] modesList = {"solo", "solo-fpp", "duo", "duo-fpp", "squad", "squad-fpp"};

    public AddPlayerBottomSheet() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.stats_addplayer_bottom, container, false);
        ButterKnife.bind(this, view);

        mFunctions = FirebaseFunctions.getInstance();

        usernameText = view.findViewById(R.id.add_username);
        continueButton = view.findViewById(R.id.add_button);
        mProgressBar = view.findViewById(R.id.add_progress);

        continueButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                startAdd();
            }
        });

        spinner = view.findViewById(R.id.region_spinner);
        spinner.setItems("Xbox Asia", "Xbox Europe", "Xbox North America", "Xbox Oceania", "PC Korea", "PC Japan", "PC North America", "PC Europe", "PC Russia", "PC Oceania", "PC Kakao", "PC South East Asia",
                "PC South and Central America", "PC Asia");
        spinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, String item) {
                region = regionList[position];
                Log.d("ReGION", region);
            }
        });

        return view;
    }

    @Override
    public int getTheme() {
        return R.style.BottomSheetDialogTheme;
    }

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        return new BottomSheetDialog(requireContext(), getTheme());
    }

    private void startAdd() {
        if (usernameText.getText().toString().isEmpty()) {
            usernameText.setError("Cannot be empty.");
            return;
        }

        RequestQueue queue = Volley.newRequestQueue(getActivity());

        continueButton.setEnabled(false);
        mProgressBar.setVisibility(View.VISIBLE);
        usernameText.setEnabled(false);
        String userName = usernameText.getText().toString();

        loadPlayerStats(userName, regionList[spinner.getSelectedIndex()].toLowerCase()).addOnCompleteListener(
                new OnCompleteListener<Map<String, Object>>() {
                    @Override
                    public void onComplete(@NonNull final Task<Map<String, Object>> task) {
                        if (!task.isSuccessful()) {
                            Exception e = task.getException();
                            if (e instanceof FirebaseFunctionsException) {
                                FirebaseFunctionsException ffe = (FirebaseFunctionsException) e;
                                FirebaseFunctionsException.Code code = ffe.getCode();

                                Log.e("AddPlayer", "onComplete: " + code.toString());

                                if (code == Code.NOT_FOUND) {
                                    Toast.makeText(getActivity(), "Player not found, try again.", Toast.LENGTH_LONG).show();
//                                    Snacky.builder().setView(getView()).info().setText("Player not found, try again.").setDuration(
//                                            Snacky.LENGTH_LONG).show();
                                } else if (code == Code.RESOURCE_EXHAUSTED) {
                                    Toast.makeText(getActivity(), "API limit reached, try again in a minute.", Toast.LENGTH_LONG).show();
//                                    Snacky.builder().setView(getView()).error().setText("API limit reached, try again in a minute.").setDuration(
//                                            Snacky.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(getActivity(), "Unknown error.", Toast.LENGTH_SHORT).show();
//                                    Snacky.builder().setView(getView()).error().setText("Unknown error.").setDuration(
//                                            Snacky.LENGTH_LONG).show();
                                }
                            }

                            divider.setBackgroundColor(getResources().getColor(R.color.md_red_A400));

                            continueButton.setEnabled(true);
                            mProgressBar.setVisibility(View.GONE);
                            usernameText.setEnabled(true);
                            return;
                        }

                        int statusCode = (int) task.getResult().get("statusCode");
                        if (statusCode == 200) {
                            if (getActivity() instanceof  MainStatsActivity) {
                                MainStatsActivity activity = (MainStatsActivity) getActivity();
                                if (activity.getPlayersMap().containsKey(userName)) {
                                    //Player is in list. Switch to them.
                                    activity.setPlayerSelected(activity.getPlayersMap().get(userName));
                                }
                            }
                            Snacky.builder().setActivity(getActivity()).success().setText("Player found and added!").setDuration(
                                    Snacky.LENGTH_SHORT).show();
                            dismiss();
                        }
                    }
                });
    }


    private Task<Map<String, Object>> loadPlayerStats(String playerName, String shardID) {
        Map<String, Object> data = new HashMap<>();
        data.put("playerName", playerName);
        data.put("shardID", shardID);

        return mFunctions.getHttpsCallable("addPlayerByName").call(data).continueWith(new Continuation<HttpsCallableResult, Map<String, Object>>() {
            @Override
            public Map<String, Object> then(@NonNull final Task<HttpsCallableResult> task) throws Exception {
                Map<String, Object> result = (Map<String, Object>) task.getResult().getData();
                Log.d("REQUEST", String.valueOf(result));
                return result;
            }
        });
    }
}
