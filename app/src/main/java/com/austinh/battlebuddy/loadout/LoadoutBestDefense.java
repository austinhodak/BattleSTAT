package com.austinh.battlebuddy.loadout;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import com.austinh.battlebuddy.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class LoadoutBestDefense extends Fragment {


    public LoadoutBestDefense() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_loadout_best_defense, container, false);
    }

}
