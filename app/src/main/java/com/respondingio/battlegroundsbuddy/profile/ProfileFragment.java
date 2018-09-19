package com.respondingio.battlegroundsbuddy.profile;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.respondingio.battlegroundsbuddy.R;
import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {

    FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();

    @BindView(R.id.profile_name_tv)
    TextView nameTV;

    @BindView(R.id.profile_email_tv)
    TextView emailTV;

    @BindView(R.id.profile_phone_tv)
    TextView phoneTV;

    @BindView(R.id.profile_game_versions)
    TextView gameTV;

    private Integer[] selected = null;

    public ProfileFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about, container, false);
        ButterKnife.bind(this, view);

        loadProfile();

        return view;
    }

    /*private void setupListeners(final FirebaseUser firebaseUser) {
        ((LinearLayout)nameTV.getParent().getParent()).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                new MaterialDialog.Builder(getActivity())
                        .title("Enter Display Name")
                        .inputRangeRes(2, 30, R.color.md_red_500)
                        .inputType(InputType.TYPE_CLASS_TEXT)
                        .input(null, firebaseUser.getDisplayName(), new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(final MaterialDialog dialog, final CharSequence input) {
                                if (input.toString().equals(firebaseUser.getDisplayName())) {
                                    return;
                                }

                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(input.toString())
                                        .build();

                                final MaterialDialog progress = new MaterialDialog.Builder(getActivity())
                                        .content("Please wait")
                                        .progress(true, 0)
                                        .show();

                                firebaseUser.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull final Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Snacky.builder().setActivity(getActivity()).success().setText("Display Name Updated!").show();
                                            loadProfile();
                                            progress.dismiss();

                                            Map<String, Object> childUpdates = new HashMap<>();
                                            childUpdates.put("users/" + firebaseUser.getUid() + "/display_name", input.toString());
                                            FirebaseDatabase.getInstance().getReference().updateChildren(childUpdates);
                                        } else {
                                            progress.dismiss();
                                            Snacky.builder().setActivity(getActivity()).error().setText("An error occurred.").show();
                                        }
                                    }
                                });
                            }
                        }).show();
            }
        });

        ((LinearLayout)emailTV.getParent().getParent()).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                new MaterialDialog.Builder(getActivity())
                        .title("Enter Email")
                        .inputRangeRes(2, 30, R.color.md_red_500)
                        .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS)
                        .input(null, firebaseUser.getEmail(), new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(final MaterialDialog dialog, final CharSequence input) {
                                if (input.toString().equals(firebaseUser.getEmail())) {
                                    return;
                                }

                                final MaterialDialog progress = new MaterialDialog.Builder(getActivity())
                                        .content("Please wait")
                                        .progress(true, 0)
                                        .show();

                                firebaseUser.updateEmail(input.toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull final Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Snacky.builder().setActivity(getActivity()).success().setText("Email Updated!").show();
                                            loadProfile();
                                            progress.dismiss();

                                            Map<String, Object> childUpdates = new HashMap<>();
                                            childUpdates.put("users/" + firebaseUser.getUid() + "/email", input.toString());
                                            FirebaseDatabase.getInstance().getReference().updateChildren(childUpdates);
                                        } else {
                                            if (task.getException() instanceof FirebaseAuthRecentLoginRequiredException) {
                                                progress.dismiss();
                                                Snacky.builder().setActivity(getActivity()).warning().setText("Please logout and back in and try again.").show();
                                            } else if (task.getException() instanceof FirebaseAuthEmailException) {
                                                progress.dismiss();
                                                Snacky.builder().setActivity(getActivity()).error().setText("Please enter a valid email.").show();
                                            } else if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                                progress.dismiss();
                                                Snacky.builder().setActivity(getActivity()).error().setText("Email already in use.").show();
                                            } else {
                                                progress.dismiss();
                                                Snacky.builder().setActivity(getActivity()).error().setText("Unknown Error Occurred.").show();
                                            }
                                        }
                                    }
                                });
                            }
                        }).show();
            }
        });

        ((LinearLayout)gameTV.getParent().getParent()).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                final String[] items = {"PC", "Xbox", "Mobile"};
                new Builder(getActivity())
                        .title("Pick Your Game Versions")
                        .items(items)
                        .itemsColor(Color.WHITE)
                        .itemsCallbackMultiChoice(selected, new ListCallbackMultiChoice() {
                            @Override
                            public boolean onSelection(MaterialDialog dialog, Integer[] which, CharSequence[] text) {
                                Map<String, Object> childUpdates = new HashMap<>();

                                List<Integer> integers = new ArrayList<>(Arrays.asList(which));

                                if (integers.contains(0)) {
                                    childUpdates.put("/users/" + firebaseUser.getUid() + "/game_versions/pc", true);
                                } else {
                                    childUpdates.put("/users/" + firebaseUser.getUid() + "/game_versions/pc", null);
                                }
                                if (integers.contains(1)) {
                                    childUpdates.put("/users/" + firebaseUser.getUid() + "/game_versions/xbox", true);
                                } else {
                                    childUpdates.put("/users/" + firebaseUser.getUid() + "/game_versions/xbox", null);
                                }
                                if (integers.contains(2)) {
                                    childUpdates.put("/users/" + firebaseUser.getUid() + "/game_versions/mobile", true);
                                } else {
                                    childUpdates.put("/users/" + firebaseUser.getUid() + "/game_versions/mobile", null);
                                }

                                FirebaseDatabase.getInstance().getReference().updateChildren(childUpdates);
                                return true;
                            }
                        })
                        .positiveText("DONE")
                        .show();
            }
        });
    }*/

    private void loadProfile() {
        FirebaseUser user = mFirebaseAuth.getCurrentUser();

        if (user == null) {
            return;
        }

        String displayName = user.getDisplayName();
        String email = user.getEmail();
        String phone = user.getPhoneNumber();

        if (displayName != null && !displayName.isEmpty()) {
            getActivity().setTitle(user.getDisplayName());
            nameTV.setText(user.getDisplayName());
        } else {
            getActivity().setTitle("My Profile");
        }

        if (email != null && !email.isEmpty()) {
            emailTV.setText(user.getEmail());
        }

        if (phone != null && !phone.isEmpty()) {
            phoneTV.setText(user.getPhoneNumber());
        }

        //setupListeners(user);

        FirebaseDatabase.getInstance().getReference("/users/" + user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    if (dataSnapshot.hasChild("game_versions")) {
                        List<Integer> myList = new ArrayList<Integer>();

                        if (dataSnapshot.hasChild("game_versions/pc")) {
                            myList.add(0);
                        }
                        if (dataSnapshot.hasChild("game_versions/xbox")) {
                            myList.add(1);
                        }
                        if (dataSnapshot.hasChild("game_versions/mobile")) {
                            myList.add(2);
                        }

                        selected = new Integer[myList.size()];
                        myList.toArray(selected);

                        Log.d("GAME", myList.toString() + " : " + myList.size() + " " + selected.length);

                        String game_versions = "";

                        if (dataSnapshot.hasChild("game_versions/pc")) {
                            game_versions = "PC";
                        }
                        if (dataSnapshot.hasChild("game_versions/xbox")) {
                            if (game_versions.isEmpty()) {
                                game_versions = "Xbox";
                            } else {
                                game_versions = game_versions + ", Xbox";
                            }
                        }
                        if (dataSnapshot.hasChild("game_versions/mobile")) {
                            if (game_versions.isEmpty()) {
                                game_versions = "Mobile";
                            } else {
                                game_versions = game_versions + ", Mobile";
                            }
                        }

                        gameTV.setText(game_versions);
                    } else {
                        gameTV.setText("None");
                    }
                }
            }

            @Override
            public void onCancelled(final DatabaseError databaseError) {

            }
        });
    }
}
