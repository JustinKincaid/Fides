package com.ibea.fides.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;

import com.astuetz.PagerSlidingTabStrip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ibea.fides.BaseActivity;
import com.ibea.fides.Constants;
import com.ibea.fides.R;
import com.ibea.fides.adapters.SwipelessViewPager;
import com.ibea.fides.adapters.UniversalPagerAdapter;
import com.ibea.fides.models.User;

import org.parceler.Parcels;

import java.util.ArrayList;

// Main organization page, nested with profile and shift tab.

public class MainActivity_Volunteer extends BaseActivity{
    User mUser;
    Boolean isOrganization;
    String currentUserId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volunteer_home);

        //TODO: Eventually will have user objects pacakaged in when navigating to this activity from a searched user list, typically indicating that the viewing user is not the same.

        Intent intent = getIntent();
        isOrganization = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(Constants.KEY_ISORGANIZATION, false);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if(intent.getExtras() != null){
            mUser = Parcels.unwrap(intent.getExtras().getParcelable("user"));
            populateTabs();
        }else{
            FirebaseDatabase.getInstance().getReference().child(Constants.DB_NODE_USERS).child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    mUser = dataSnapshot.getValue(User.class);
                    populateTabs();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }
    }

    public void populateTabs(){

        SwipelessViewPager viewPager = (SwipelessViewPager) findViewById(R.id.viewpager);

        //Profile only
        ArrayList<Fragment> fragmentList = new ArrayList<Fragment>();
        ArrayList<String> tabTitles = new ArrayList<String>();

        //TODO: This is going to need much more granular parsing

        if(isOrganization){
            tabTitles.add("Profile");
            fragmentList.add(new ProfileForVolunteerFragment());
            viewPager.setAdapter(new UniversalPagerAdapter(getSupportFragmentManager(), 1, tabTitles, fragmentList));
        }else {
            //User is volunteer, and this is their page
            tabTitles.add("Profile");
            tabTitles.add("Shifts");
            tabTitles.add("History");
            fragmentList.add(new ProfileForVolunteerFragment().newInstance(mUser));
            fragmentList.add(new ShiftsPendingForVolunteerFragment());
            fragmentList.add(new ShiftsCompletedForVolunteerFragment());
            viewPager.setAdapter(new UniversalPagerAdapter(getSupportFragmentManager(), 3, tabTitles, fragmentList));
        }

        // Get the ViewPager and set it's PagerAdapter so that it can display items
        PagerSlidingTabStrip tabsStrip = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        // Attach the view pager to the tab strip
        tabsStrip.setViewPager(viewPager);
    }

}