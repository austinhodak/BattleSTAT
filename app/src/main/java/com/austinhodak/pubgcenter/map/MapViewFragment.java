package com.austinhodak.pubgcenter.map;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.austinhodak.pubgcenter.R;
import com.bogdwellers.pinchtozoom.ImageMatrixTouchHandler;
import com.bumptech.glide.Glide;
import de.mateware.snacky.Snacky;

/**
 * A simple {@link Fragment} subclass.
 */
public class MapViewFragment extends Fragment {

    @BindView(R.id.map_frame)
    ImageView mMapView;

    SharedPreferences mSharedPreferences;

    public MapViewFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map_view, container, false);
        ButterKnife.bind(this, view);
        // Inflate the layout for this fragment
        mMapView.setOnTouchListener(new ImageMatrixTouchHandler(getActivity()));

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if (mSharedPreferences != null) {
            int runs = mSharedPreferences.getInt("runTotal", 5);
            if (runs < 5) {
                //Increment
                mSharedPreferences.edit().putInt("runTotal", runs + 1).apply();
            } else {
                //Show toast & reset runs
                Snacky.builder().setActivity(getActivity())
                        .setText("Please remember, this page is under construction!").setDuration(Snacky.LENGTH_LONG)
                        .warning().show();
                mSharedPreferences.edit().putInt("runTotal", 0).apply();
            }
        }

        if (getArguments() != null) {
            int map = getArguments().getInt("map");
            loadMap(map);
        }

        return view;
    }

    private void loadMap(final int map) {
        switch (map) {
            case 0:
                Glide.with(getActivity()).load(R.drawable.pubg_erangel).into(mMapView);
                break;
            case 1:
                Glide.with(getActivity()).load(R.drawable.pubg_miramar).into(mMapView);
                break;
        }
    }

}
