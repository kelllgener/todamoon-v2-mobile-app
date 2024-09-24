package com.toda.todamoon_v2.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.toda.todamoon_v2.driver.fragments.BillingFragment;
import com.toda.todamoon_v2.driver.fragments.QueueEntryFragment;

public class MyPagerAdapter extends FragmentStatePagerAdapter {

    public MyPagerAdapter(@NonNull FragmentManager fm) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        if (position == 1) {
            return new BillingFragment();
        }
        return new QueueEntryFragment();
    }

    @Override
    public int getCount() {
        return 2;
    }
}
