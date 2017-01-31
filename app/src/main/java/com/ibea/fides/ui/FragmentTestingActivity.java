package com.ibea.fides.ui;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;

import com.ibea.fides.BaseActivity;
import com.ibea.fides.R;

public class FragmentTestingActivity extends BaseActivity {
    FragmentPagerAdapter adapterViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_testing);
        ViewPager vpPager = (ViewPager) findViewById(R.id.vpPager);
        adapterViewPager = new MyPagerAdapter(getSupportFragmentManager());
        vpPager.setAdapter(adapterViewPager);
        vpPager.setCurrentItem(0);
    }

    public static class MyPagerAdapter extends FragmentPagerAdapter {
        private static int NUM_ITEMS = 4;
        private String tabtitles[] = new String[] { "Volunteer Opportunites", "Upcoming Shifts (Volunteers)", "Upcoming Shifts (Organizations)", "Completed Shifts (Volunteer)" };

        public MyPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        // Returns total number of pages
        @Override
        public int getCount() {
            return NUM_ITEMS;
        }

        // Returns the fragment to display for that page
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0: // Fragment # 0 - This will show FirstFragment
                    return ShiftsSearchFragment.newInstance(0, "Volunteer Opportunities");
                case 1:
                    return ShiftsPendingForVolunteerFragment.newInstance(0, "Pending Shifts");
                case 2:
                    return ShiftsPendingForOrganizationsFragment.newInstance(0, "Pending Shifts for Orgs");
                case 3:
                    Log.v("+++++", "Case triggered");
                    return ShiftsCompletedForVolunteersFragment.newInstance(0, "Completed Shifts for Volunteers");
                default:
                    return null;
            }
        }

        // Returns the page title for the top indicator
        @Override
        public CharSequence getPageTitle(int position) {
            return tabtitles[position];
        }

    }
}
