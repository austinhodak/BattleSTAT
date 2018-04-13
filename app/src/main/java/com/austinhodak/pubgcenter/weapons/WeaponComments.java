package com.austinhodak.pubgcenter.weapons;


import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.MaterialDialog.SingleButtonCallback;
import com.austinhodak.pubgcenter.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import de.mateware.snacky.Snacky;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.idik.lib.slimadapter.SlimAdapter;
import net.idik.lib.slimadapter.SlimInjector;
import net.idik.lib.slimadapter.viewinjector.IViewInjector;

/**
 * A simple {@link Fragment} subclass.
 */
public class WeaponComments extends Fragment {

    @BindView(R.id.comment_edittext)
    EditText commentEditText;

    @BindView(R.id.comment_send)
    ImageView buttonSend;

    @BindView(R.id.comment_rv)
    RecyclerView mRecyclerView;

    @BindView(R.id.empty_tv)
    TextView emptyTV;

    @BindView(R.id.comments_pg)
    ProgressBar mProgressBar;

    private ChildEventListener childEventListener;

    public WeaponComments() {
        // Required empty public constructor
    }

    SlimAdapter mSlimAdapter;

    String weaponPath, weaponKey;

    List<Object> listData = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_weapon_comments, container, false);
        ButterKnife.bind(this, view);

        if (getArguments() != null) {
            weaponPath = getArguments().getString("weaponPath");
            weaponKey = getArguments().getString("weaponKey");
        }
        //Toast.makeText(getActivity(), weaponPath, Toast.LENGTH_SHORT).show();
        // Inflate the layout for this fragment

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            buttonSend.setEnabled(false);
            commentEditText.setText("You must be signed in.");
            commentEditText.setEnabled(false);
        }

        buttonSend.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                saveComment();
            }
        });

        setupAdapter();

        return view;
    }

    private void setupAdapter() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mSlimAdapter = SlimAdapter.create().register(R.layout.comments_weapons_item, new SlimInjector<DataSnapshot>() {
            @Override
            public void onInject(final DataSnapshot data, final IViewInjector injector) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                long time = 0;
                try {
                    time = sdf.parse(data.child("timestamp").getValue().toString()).getTime();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                long now = System.currentTimeMillis();

                CharSequence ago =
                        DateUtils.getRelativeTimeSpanString(time, now, DateUtils.MINUTE_IN_MILLIS);

                injector.text(R.id.comment_time, ago);
                injector.text(R.id.comment_user, data.child("user_name").getValue().toString());
                injector.text(R.id.comment_text, data.child("comment").getValue().toString());

                FirebaseDatabase.getInstance().getReference("/users/").child(data.child("user").getValue().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {
                        if (dataSnapshot == null || !dataSnapshot.exists()) {
                            return;
                        }

                        if (!dataSnapshot.child("display_name").getValue().toString().equals(data.child("user_name").getValue().toString())) {
                            injector.text(R.id.comment_user, dataSnapshot.child("display_name").getValue().toString());
                        }

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

                        Log.d("VERSIONS", game_versions);

                        injector.text(R.id.comment_gameV, game_versions);

                        if (game_versions.isEmpty()) {
                            injector.gone(R.id.comment_gameV);
                        }

                        if (dataSnapshot.hasChild("dev")) {
                            injector.textColor(R.id.comment_user, getResources().getColor(R.color.md_red_500));
                            injector.text(R.id.comment_user, dataSnapshot.child("display_name").getValue().toString() + " (DEV)");
                        }
                    }

                    @Override
                    public void onCancelled(final DatabaseError databaseError) {

                    }
                });

                injector.longClicked(R.id.top_layout, new OnLongClickListener() {
                    @Override
                    public boolean onLongClick(final View v) {
                        if (FirebaseAuth.getInstance().getCurrentUser() == null) return false;
                        FirebaseDatabase.getInstance().getReference("/users/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/weapon_comments/" + weaponKey + "/" + data.getKey()).addListenerForSingleValueEvent(
                                new ValueEventListener() {
                                    @Override
                                    public void onDataChange(final DataSnapshot dataSnapshot) {
                                        if (dataSnapshot != null && dataSnapshot.exists() && (boolean) dataSnapshot.getValue()) {
                                            new MaterialDialog.Builder(getActivity())
                                                    .title("Delete Post?")
                                                    .content("This cannot be undone.")
                                                    .positiveText("DELETE")
                                                    .negativeText("CANCEL")
                                                    .onPositive(new SingleButtonCallback() {
                                                        @Override
                                                        public void onClick(@NonNull final MaterialDialog dialog, @NonNull final DialogAction which) {
                                                            FirebaseDatabase.getInstance().getReference("/users/" + FirebaseAuth.getInstance().getCurrentUser().getUid()
                                                                    + "/weapon_comments/" + weaponKey + "/" + data.getKey()).removeValue();

                                                            FirebaseDatabase.getInstance().getReference("/comments_weapons/" + weaponKey + "/" + data.getKey()).removeValue();

                                                            listData.remove(data);
                                                            mSlimAdapter.updateData(listData);

                                                            FirebaseDatabase.getInstance().getReference("/comments_weapons/" + weaponKey).removeEventListener(childEventListener);
                                                            loadData();
                                                        }
                                                    })
                                                    .show();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(final DatabaseError databaseError) {

                                    }
                                });
                        return false;
                    }
                });
            }
        }).attachTo(mRecyclerView).updateData(listData);

        loadData();
    }

    private void loadData() {
        listData.clear();
        mProgressBar.setVisibility(View.VISIBLE);
        childEventListener = FirebaseDatabase.getInstance().getReference("/comments_weapons/" + weaponKey).orderByChild("timestamp").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(final DataSnapshot dataSnapshot, final String s) {
                listData.add(0, dataSnapshot);
                mSlimAdapter.updateData(listData);
                emptyTV.setVisibility(View.GONE);
            }

            @Override
            public void onChildChanged(final DataSnapshot dataSnapshot, final String s) {

            }

            @Override
            public void onChildRemoved(final DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(final DataSnapshot dataSnapshot, final String s) {

            }

            @Override
            public void onCancelled(final DatabaseError databaseError) {

            }
        });
        FirebaseDatabase.getInstance().getReference("/comments_weapons/" + weaponKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                if (dataSnapshot == null || !dataSnapshot.exists()) {
                    emptyTV.setVisibility(View.VISIBLE);
                    mProgressBar.setVisibility(View.GONE);

                    mSlimAdapter.updateData(listData);
                } else {
                    mProgressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(final DatabaseError databaseError) {

            }
        });
    }

    private void saveComment() {
        buttonSend.setEnabled(false);
        commentEditText.setEnabled(false);
        String comment = commentEditText.getText().toString();
        if (!comment.isEmpty()) {
            String UID = FirebaseAuth.getInstance().getCurrentUser().getUid();
            final String key = FirebaseDatabase.getInstance().getReference("/comments_weapons/" + weaponKey).push().getKey();
            String path = "/comments_weapons/" + weaponKey + "/" + key;

            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            Date date = new Date();

            Map<String, Object> childUpdates = new HashMap<>();
            childUpdates.put(path + "/comment", comment);
            childUpdates.put(path + "/user", UID);
            childUpdates.put(path + "/user_name", FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
            childUpdates.put(path + "/timestamp", dateFormat.format(date));
            childUpdates.put(path + "/weapon_path", weaponPath);

            childUpdates.put("/users/" + UID + "/weapon_comments/" + weaponKey + "/" + key, true);

            FirebaseDatabase.getInstance().getReference().updateChildren(childUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull final Task<Void> task) {
                    buttonSend.setEnabled(true);
                    commentEditText.setEnabled(true);
                    if (!task.isSuccessful()) {
                        Snacky.builder().setActivity(getActivity()).error().setText("Error posting.").show();
                        return;
                    }

                    commentEditText.getText().clear();
                    Snacky.builder().setActivity(getActivity()).setActionTextColor(Color.WHITE).setActionText("UNDO").setActionClickListener(new OnClickListener() {
                        @Override
                        public void onClick(final View v) {
                            FirebaseDatabase.getInstance().getReference("/users/" + FirebaseAuth.getInstance().getCurrentUser().getUid()
                                    + "/weapon_comments/" + weaponKey + "/" + key).removeValue();

                            FirebaseDatabase.getInstance().getReference("/comments_weapons/" + weaponKey + "/" + key).removeValue();

                            FirebaseDatabase.getInstance().getReference("/comments_weapons/" + weaponKey).removeEventListener(childEventListener);


                            mSlimAdapter.updateData(listData);

                            loadData();
                        }
                    }).success().setText("Posted!").show();
                }
            });
        } else {
            buttonSend.setEnabled(true);
            commentEditText.setEnabled(true);
            commentEditText.setError("Cannot be empty.");
        }
    }

}
