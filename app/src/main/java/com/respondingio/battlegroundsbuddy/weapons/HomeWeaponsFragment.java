package com.respondingio.battlegroundsbuddy.weapons;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.respondingio.battlegroundsbuddy.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import butterknife.ButterKnife;

import static android.content.Context.MODE_PRIVATE;

public class HomeWeaponsFragment extends Fragment {

    class ViewPagerAdapter extends FragmentPagerAdapter {

        //"Teaser \uD83D\uDE08"

        ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public int getCount() {
            return mStringArray.size();
        }

        @Override
        public Fragment getItem(int position) {
            Fragment currentFragment;
            Bundle bundle;
            currentFragment = new MainWeaponsList();
            bundle = new Bundle();

            if (tabLayout.getTabCount() == 10 && position == 0) {
                bundle.putBoolean("isFavoriteTab", true);
            } else if (tabLayout.getTabCount() == 10) {
                bundle.putInt("pos", position - 1);
            } else {
                bundle.putInt("pos", position);
            }

            currentFragment.setArguments(bundle);
            return currentFragment;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mStringArray.get(position);
        }
    }

    private List<String> mStringArray = new ArrayList<>();

    private TabLayout tabLayout;

    private String title[] = {"Assault Rifles", "Sniper Rifles", "SMGs", "Shotguns", "Pistols", "LMGs",
            "Throwables", "Melee", "Misc"};

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container,
            @Nullable final Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_home_weapons, container, false);
        ButterKnife.bind(this, view);

        mStringArray.addAll(Arrays.asList(title));

        final ViewPager viewPager = view.findViewById(R.id.viewpager);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getChildFragmentManager());
        viewPager.setAdapter(adapter);

        tabLayout = view.findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        if (getActivity() != null) {
            final SharedPreferences sharedPreferences = getActivity().getSharedPreferences("com.austinhodak.pubgcenter", MODE_PRIVATE);
            Set<String> favs = sharedPreferences.getStringSet("favoriteWeapons", null);
            if (favs != null && !favs.isEmpty()) {
                tabLayout.addTab(tabLayout.newTab().setCustomView(R.layout.tab_fav), 0);

                tabLayout.setScrollPosition(1, 0f, true);
                viewPager.setCurrentItem(1);

                int emoji = 0x2B50;

                mStringArray.add(0, new String(Character.toChars(emoji)));
                adapter.notifyDataSetChanged();
            }
        }

        return view;
    }
}
