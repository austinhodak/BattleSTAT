package com.respondingio.battlegroundsbuddy.profile;


import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.respondingio.battlegroundsbuddy.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileSettingsFragment extends Fragment {

    @BindView(R.id.profile_notify_master)
    TextView masterNotifyTV;

    @BindView(R.id.master_notify_reveal)
    CardView mCardView;

    boolean notifyMaster;

    private SharedPreferences mSharedPreferences;

    public ProfileSettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_settings, container, false);
        ButterKnife.bind(this, view);
        // Inflate the layout for this fragment

        mSharedPreferences = getActivity().getSharedPreferences("com.austinhodak.pubgcenter", Context.MODE_PRIVATE);

        if (mSharedPreferences.getBoolean("masterNotify", true)) {
            masterNotifyTV.setText("On");
            notifyMaster = true;
            mCardView.setVisibility(View.VISIBLE);
        } else {
            masterNotifyTV.setText("Off");
            notifyMaster = false;
            mCardView.setVisibility(View.INVISIBLE);
        }

        ((LinearLayout) masterNotifyTV.getParent().getParent()).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (masterNotifyTV.getText().toString().equals("On") && notifyMaster) {
                    masterNotifyTV.setText("Off");

                    int centerX = (mCardView.getLeft() + mCardView.getRight()) / 2;
                    int centerY = (mCardView.getTop() + mCardView.getBottom()) / 2;

                    int startRadius = 0;
                    // get the final radius for the clipping circle
                    int endRadius = Math.max(mCardView.getWidth(), mCardView.getHeight());

                    Animator anim =
                            ViewAnimationUtils.createCircularReveal(mCardView, centerX, centerY, endRadius, startRadius);

                    anim.addListener(new AnimatorListener() {
                        @Override
                        public void onAnimationStart(final Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(final Animator animation) {
                            mCardView.setVisibility(View.INVISIBLE);
                            notifyMaster = false;
                        }

                        @Override
                        public void onAnimationCancel(final Animator animation) {

                        }

                        @Override
                        public void onAnimationRepeat(final Animator animation) {

                        }
                    });
                    anim.start();

                    mSharedPreferences.edit().putBoolean("masterNotify", false).apply();
                } else if (masterNotifyTV.getText().toString().equals("Off") && !notifyMaster) {
                    masterNotifyTV.setText("On");
                    int centerX = (mCardView.getLeft() + mCardView.getRight()) / 2;
                    int centerY = (mCardView.getTop() + mCardView.getBottom()) / 2;

                    int startRadius = 0;
                    // get the final radius for the clipping circle
                    int endRadius = Math.max(mCardView.getWidth(), mCardView.getHeight());

                    Animator anim =
                            ViewAnimationUtils.createCircularReveal(mCardView, centerX, centerY, startRadius, endRadius);

                    // make the view visible and start the animation
                    mCardView.setVisibility(View.VISIBLE);
                    anim.addListener(new AnimatorListener() {
                        @Override
                        public void onAnimationStart(final Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(final Animator animation) {

                            notifyMaster = true;

                        }

                        @Override
                        public void onAnimationCancel(final Animator animation) {

                        }

                        @Override
                        public void onAnimationRepeat(final Animator animation) {

                        }
                    });
                    anim.start();


                    mSharedPreferences.edit().putBoolean("masterNotify", true).apply();
                }
            }
        });

        return view;
    }

}
