package com.ibea.fides.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ibea.fides.Constants;
import com.ibea.fides.R;
import com.ibea.fides.models.Shift;
import com.ibea.fides.models.User;

import java.util.List;


/**
 * Created by KincaidJ on 1/31/17.
 */

public class DirtyFirebaseVolunteerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    View mView;
    Context mContext;
    User mUser;
    Button mBadButton;
    Button mPoorButton;
    Button mGoodButton;
    Button mGreatButton;

    // Rating System
    int bad = -5;
    int poor = -2;
    int good = 2;
    int great = 3;
    int base = 2;


    String shiftId;
    String indexKey;
    DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();

    public DirtyFirebaseVolunteerViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        mContext = itemView.getContext();

    }

    public void bindUser(String userId, String _shiftId, int position) {
        final TextView userName = (TextView) mView.findViewById(R.id.textView_Name);

        mBadButton = (Button) mView.findViewById(R.id.badButton);
        mPoorButton = (Button) mView.findViewById(R.id.poorButton);
        mGoodButton = (Button) mView.findViewById(R.id.goodButton);
        mGreatButton = (Button) mView.findViewById(R.id.greatButton);

        mBadButton.setOnClickListener(this);
        mPoorButton.setOnClickListener(this);
        mGoodButton.setOnClickListener(this);
        mGreatButton.setOnClickListener(this);

        shiftId = _shiftId;
        indexKey = Integer.toString(position);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(Constants.DB_NODE_USERS).child(userId);
        ref.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                mUser = user;
                userName.setText(mUser.getName());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onClick(View view) {
        if(view == mBadButton) {
            rate(bad);
        } else if(view == mPoorButton) {
            rate(poor);
        } else if(view == mGoodButton) {
            rate(good);
        } else if(view == mGreatButton) {
            rate(great);
        }
    }

    public void rate(int rating) {
        mBadButton.setVisibility(View.GONE);
        mPoorButton.setVisibility(View.GONE);
        mGoodButton.setVisibility(View.GONE);
        mGreatButton.setVisibility(View.GONE);

        int currentPoints = mUser.getCurrentPoints();
        int maxPoints = mUser.getMaxPoints();

        currentPoints += rating;
        maxPoints += base;


        // Ensures that ranking doesn't drop below 0
        if(currentPoints < 0) {
            currentPoints = 0;
        }

        mUser.setCurrentPoints(currentPoints);
        mUser.setMaxPoints(maxPoints);

        dbRef.child(Constants.DB_NODE_USERS).child(mUser.getPushId()).setValue(mUser);

        dbRef.child(Constants.DB_NODE_SHIFTS).child(shiftId).child("currentVolunteers").child(indexKey).removeValue();

        dbRef.child(Constants.DB_NODE_SHIFTS).child(shiftId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Shift shift = dataSnapshot.getValue(Shift.class);

                // AJ VALIDATION
                // If you remove the following code and instead manipulate the shift with .removeVolunteers and push it back
                // up, then you should encounter the out of bounds error.. I think.

                shift.getCurrentVolunteers().remove(mUser.getPushId());
                shift.addRated(mUser.getPushId());
                dbRef.child(Constants.DB_NODE_SHIFTS).child(shiftId).child("currentVolunteers").setValue(shift.getCurrentVolunteers());
                dbRef.child(Constants.DB_NODE_SHIFTS).child(shiftId).child("ratedVolunteers").setValue(shift.getRatedVolunteers());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }
}