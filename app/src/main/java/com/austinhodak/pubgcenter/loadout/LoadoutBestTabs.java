package com.austinhodak.pubgcenter.loadout;


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
import butterknife.ButterKnife;
import com.austinhodak.pubgcenter.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

/**
 * A simple {@link Fragment} subclass.
 */
public class LoadoutBestTabs extends Fragment {


    class ViewPagerAdapter extends FragmentPagerAdapter {

        private String title[] = {"ASSAULT", "DEFENSE", "CLOSE QUARTERS", "RANGED"};

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public int getCount() {
            return title.length;
        }

        @Override
        public Fragment getItem(int position) {
            Fragment currentFragment = null;
            switch (position) {
                case 0:
                    currentFragment = new LoadoutBestAssault();
                    break;
                case 1:
                    currentFragment = new LoadoutBestDefense();
                    break;
                case 2:
                    currentFragment = new LoadoutBestCQ();
                    break;
                case 3:
                    currentFragment = new LoadoutBestRanged();
                    break;
                default:
                    currentFragment = new LoadoutBestAssault();
            }
            return currentFragment;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return title[position];
        }
    }

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private FirebaseStorage storage = FirebaseStorage.getInstance();

    private TabLayout tabLayout;

    private ViewPager viewPager;

    public LoadoutBestTabs() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container,
            @Nullable final Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_home_weapons, container, false);

        ButterKnife.bind(this, view);

        viewPager = (ViewPager) view.findViewById(R.id.viewpager);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getChildFragmentManager());
        viewPager.setAdapter(adapter);

        tabLayout = (TabLayout) view.findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        //tabLayout.addTab(tabLayout.newTab().setCustomView(R.layout.tab_group), 0);

        //tabLayout.setScrollPosition(1, 0f, true);
        //viewPager.setCurrentItem(1);

        return view;
    }

}
