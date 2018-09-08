package com.respondingio.battlegroundsbuddy.attachments;

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
import com.respondingio.battlegroundsbuddy.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

public class HomeAttachmentsFragment extends Fragment {

    class ViewPagerAdapter extends FragmentPagerAdapter {

        private String title[] = {"Muzzle", "Upper Rail", "Lower Rail", "Magazine", "Stock"};

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
            Bundle bundle;
            currentFragment = new HomeAttachmentsList();
            bundle = new Bundle();
            bundle.putInt("pos", position);
            currentFragment.setArguments(bundle);
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

        return view;
    }
}
