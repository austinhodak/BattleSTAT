package com.austinhodak.pubgcenter.map;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.austinhodak.pubgcenter.GlideApp;
import com.austinhodak.pubgcenter.R;
import com.bogdwellers.pinchtozoom.ImageMatrixTouchHandler;
import de.mateware.snacky.Snacky;

/**
 * A simple {@link Fragment} subclass.
 */
public class MapViewFragment extends Fragment {

    @BindView(R.id.map_frame)
    ImageView mMapView;

    SharedPreferences mSharedPreferences;

    @BindView(R.id.webview)
    WebView mWebView;

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

            mWebView.loadUrl("https://pubgmap.io/");
            mWebView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    view.loadUrl(url);
                    return true;
                }
            });
            //loadMap(map);
        }

        return view;
    }



    private void loadMap(final int map) {
        switch (map) {
            case 0:
                GlideApp.with(getActivity()).load(R.drawable.pubg_erangel).into(mMapView);
                break;
            case 1:
                GlideApp.with(getActivity()).load(R.drawable.pubg_miramar).into(mMapView);
                break;
            case 2:
                GlideApp.with(getActivity()).load(R.drawable.savage_map).into(mMapView);
                break;
        }
    }

}
