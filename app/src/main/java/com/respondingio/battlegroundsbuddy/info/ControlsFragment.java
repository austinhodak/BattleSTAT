package com.respondingio.battlegroundsbuddy.info;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.ButterKnife;
import com.respondingio.battlegroundsbuddy.R;

public class ControlsFragment extends Fragment {

    public ControlsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_controls, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

}
