package com.ibea.fides.ui;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ibea.fides.Constants;
import com.ibea.fides.R;
import com.ibea.fides.adapters.FirebaseCompletedShiftViewHolder;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class ShiftsCompletedForVolunteerFragment extends Fragment {
    @Bind(R.id.recyclerView) RecyclerView mRecyclerView;

    FirebaseRecyclerAdapter mFirebaseAdapter;
    Boolean isOrganization;
    DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
    String mUserId;


    public ShiftsCompletedForVolunteerFragment() {
        // Required empty public constructor
    }

    // newInstance constructor for creating fragment with arguments
    public static ShiftsCompletedForVolunteerFragment newInstance(int page, String title) {
        ShiftsCompletedForVolunteerFragment fragmentFirst = new ShiftsCompletedForVolunteerFragment();
        Log.v("<<<<<", "ShiftsCompleted newInstance");
        return fragmentFirst;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_shifts_completed_for_volunteers, container, false);
        ButterKnife.bind(this, view);
        Log.v("<<<<<", "In onCreateView for Completed");

        mUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        dbRef.child(Constants.DB_NODE_USERS).child(mUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                isOrganization = dataSnapshot.child("isOrganization").getValue(Boolean.class);
                Log.d(">UserID", mUserId);
                Log.d(">isOrg>", String.valueOf(isOrganization));
                setUpFirebaseAdapter();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return view;
    }

    private void setUpFirebaseAdapter() {
        Log.v(">>>>>", "in CompletedSetup");
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference dbFirebaseNode;

        dbFirebaseNode = FirebaseDatabase.getInstance().getReference().child(Constants.DB_NODE_SHIFTSCOMPLETE).child(Constants.DB_SUBNODE_VOLUNTEERS).child(currentUserId);
        mFirebaseAdapter = new FirebaseRecyclerAdapter<String, FirebaseCompletedShiftViewHolder>
                (String.class, R.layout.completed_shift_list_item, FirebaseCompletedShiftViewHolder.class, dbFirebaseNode) {

            @Override
            protected void populateViewHolder(FirebaseCompletedShiftViewHolder viewHolder, String shiftId, int position) {
                viewHolder.bindShift(shiftId, false);
            }
        };
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        mRecyclerView.setAdapter(mFirebaseAdapter);

    }
}