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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ibea.fides.Constants;
import com.ibea.fides.R;
import com.ibea.fides.adapters.DirtyFirebaseShiftViewHolder;
import com.ibea.fides.adapters.FirebaseCompletedShiftViewHolder;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class ShiftsPendingForOrganizationsFragment extends Fragment {
    @Bind(R.id.recyclerView)
    RecyclerView mRecyclerView;

    private FirebaseRecyclerAdapter mFirebaseAdapter;
    private RecyclerView.Adapter mRecyclerAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private FirebaseUser mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
    private Boolean isOrganization;

    private DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference dbShiftsPendingForOrganizations = dbRef.child(Constants.DB_NODE_SHIFTSPENDING).child(Constants.DB_SUBNODE_ORGANIZATIONS).child(mCurrentUser.getUid());

    public ShiftsPendingForOrganizationsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_shifts_pending_for_organizations, container, false);
        ButterKnife.bind(this, view);

        dbRef.child(Constants.DB_NODE_USERS).child(mCurrentUser.getUid()).child("isOrganization").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                isOrganization = dataSnapshot.getValue(Boolean.class);
                if(isOrganization){
                    setUpFirebaseAdapter();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return view;
    }

    // newInstance constructor for creating fragment with arguments
    public static ShiftsPendingForOrganizationsFragment newInstance(int page, String title) {
        ShiftsPendingForOrganizationsFragment fragmentFirst = new ShiftsPendingForOrganizationsFragment();
        return fragmentFirst;
    }

    private void setUpFirebaseAdapter() {
        Log.v(">>>>", "SfO adapter");
        mFirebaseAdapter = new FirebaseRecyclerAdapter<String, DirtyFirebaseShiftViewHolder>
                (String.class, R.layout.dirty_shift_list_item, DirtyFirebaseShiftViewHolder.class, dbShiftsPendingForOrganizations) {

            @Override
            protected void populateViewHolder(DirtyFirebaseShiftViewHolder viewHolder, String shiftId, int position) {
                viewHolder.bindShift(shiftId, isOrganization);
            }
        };
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        mRecyclerView.setAdapter(mFirebaseAdapter);
    }

}