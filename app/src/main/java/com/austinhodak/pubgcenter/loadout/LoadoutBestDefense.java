package com.austinhodak.pubgcenter.loadout;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.austinhodak.pubgcenter.R;

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
