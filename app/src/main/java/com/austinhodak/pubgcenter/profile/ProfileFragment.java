package com.austinhodak.pubgcenter.profile;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.afollestad.materialdialogs.MaterialDialog;
import com.austinhodak.pubgcenter.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthEmailException;
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.FirebaseDatabase;
import de.mateware.snacky.Snacky;
import java.util.HashMap;
import java.util.Map;

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

    public ProfileFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about, container, false);
        ButterKnife.bind(this, view);

        loadProfile();

        return view;
    }

    private void setupListeners(final FirebaseUser firebaseUser) {
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
                new MaterialDialog.Builder(getActivity())
                        .title("Pick Your Game Versions")
                        .items(items)
                        .itemsCallbackMultiChoice(null, new MaterialDialog.ListCallbackMultiChoice() {
                            @Override
                            public boolean onSelection(MaterialDialog dialog, Integer[] which, CharSequence[] text) {

                                return true;
                            }
                        })
                        .positiveText("DONE")
                        .show();
            }
        });
    }

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

        setupListeners(user);
    }
}
