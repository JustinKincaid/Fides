package com.ibea.fides.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.ibea.fides.ui.FragmentTab1;
import com.ibea.fides.ui.ShiftFragment;

/**
 * Created by Nhat on 1/27/17.
 */

// This Page Adapter is handling fragment for the profile and shift tab.

public class ViewPagerAdapter extends FragmentPagerAdapter {

    final int PAGE_COUNT = 2;
    // Tab Titles
    private String tabtitles[] = new String[] { "Profile", "Shift" };
    Context context;

    public ViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {

            // Open FragmentTab1.java
            case 0:
                FragmentTab1 fragmenttab1 = new FragmentTab1();
                return fragmenttab1;

            // Open ShiftFragment.java
            case 1:
                ShiftFragment organizationshiftfragment = new ShiftFragment();
                return organizationshiftfragment;


//            case 2:
//                FragmentTab3 fragmenttab3 = new FragmentTab3();
//                return fragmenttab3;
        }
        return null;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabtitles[position];
    }
}