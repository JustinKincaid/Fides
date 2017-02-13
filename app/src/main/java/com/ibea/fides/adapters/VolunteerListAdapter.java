package com.ibea.fides.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ibea.fides.Constants;
import com.ibea.fides.R;
import com.ibea.fides.models.Organization;
import com.ibea.fides.models.Shift;
import com.ibea.fides.models.User;
import com.ibea.fides.ui.MainActivity_Volunteer;

import org.parceler.Parcels;

import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class VolunteerListAdapter extends RecyclerView.Adapter<VolunteerListAdapter.VolunteerViewHolder> {
    private ArrayList<User> mVolunteers = new ArrayList<>();
    private User mVolunteer;
    private List<String> mUnratedVolunteers = new ArrayList<>();
    private Shift mShift;
    private int mRating;

    // Popup
    private PopupWindow mPopUp;
    private View mPop;
    private Button mNoShowButton;
    private Button mShowButton;
    private EditText mHoursInput;
    private String mHours;
    private String mDiffInput;

    // Rating System
    final private int DISLIKE = 0;
    final private int LIKE = 3;

    DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();

    public VolunteerListAdapter(Context context, ArrayList<User> volunteers, List<String> _unratedVolunteers, Shift _shift) {
        mVolunteers = volunteers;
        mUnratedVolunteers = _unratedVolunteers;
        mShift = _shift;
    }

    @Override
    public VolunteerListAdapter.VolunteerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_volunteer, parent, false);
        VolunteerViewHolder viewHolder = new VolunteerViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(VolunteerListAdapter.VolunteerViewHolder holder, int position) {
        holder.bindVolunteer(mVolunteers.get(position));
    }

    @Override
    public int getItemCount() {
        return mVolunteers.size();
    }

    public class VolunteerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private Context mContext;
        @Bind(R.id.textView_Name) TextView mTextView_Name;

        public VolunteerViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            mContext = itemView.getContext();
            itemView.setOnClickListener(this);
        }

        public void bindVolunteer(User volunteer) {
            mVolunteer = volunteer;
            mTextView_Name.setText(mVolunteer.getName());

            if(mUnratedVolunteers.contains(mVolunteer.getPushId())){
                mTextView_Name.setTextColor(Color.parseColor("#F44336"));
            }
        }

        @Override
        public void onClick(View view){
            if(view == mShowButton) {
                mHours = mHoursInput.getText().toString();
                dataUpdate(mHours, true);
            } else if(view == mNoShowButton) {
                dataUpdate(mHours, false);
            }else {
                Intent intent = new Intent(mContext, MainActivity_Volunteer.class);
                intent.putExtra("user", Parcels.wrap(mVolunteer));
                mContext.startActivity(intent);
            }
        }

        public String getVolunteerId(){
            return mVolunteer.getPushId();
        }

        private void popup(int rating) {
            mRating = rating;

            mPopUp = new PopupWindow(mPop, 1000, 1000, true);
            mPopUp.showAtLocation(mPop, Gravity.CENTER, 0, 0);
            mShowButton = (Button) mPop.findViewById(R.id.showButton);
            mNoShowButton = (Button) mPop.findViewById(R.id.noShowButton);
            mHoursInput = (EditText) mPop.findViewById(R.id.hoursInput);

            mHoursInput.setText(mDiffInput);
            mShowButton.setOnClickListener(this);
            mNoShowButton.setOnClickListener(this);
        }

        private void dataUpdate(final String time, final boolean showed) {
            dbRef.child(Constants.DB_NODE_SHIFTS).child(mShift.getPushId()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    mShift = dataSnapshot.getValue(Shift.class);
                    if(!mShift.getRatedVolunteers().contains(mVolunteer.getPushId())){
                        if(showed) {
                            setRating();
                            double currentHours = mVolunteer.getHours();
                            currentHours += Double.parseDouble(time);
                            dbRef.child(Constants.DB_NODE_USERS).child(mVolunteer.getPushId()).child("hours").setValue(currentHours);
                        } else {
                            setRating();
                            int absences = mVolunteer.getAbsences();
                            absences += 1;
                            dbRef.child(Constants.DB_NODE_USERS).child(mVolunteer.getPushId()).child("absences").setValue(absences);
                        }
                        mPopUp.dismiss();
                    }else{
                        Toast.makeText(mContext, "Volunteer already rated", Toast.LENGTH_SHORT).show();
                        mPopUp.dismiss();
                        //TODO: Update recyclerview
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        public void setRating(){
            // Retrieve User's rating history and calculate new rating
            List<Integer> ratingHistory = mVolunteer.getRatingHistory();
            ratingHistory.add(mRating);
            float size = ratingHistory.size();
            float modifiedRating = 0;
            float modifiedMax = 0;
            float index = 1;
            float modifier = 0;

            for(Integer rate : ratingHistory) {
                modifier = index/size;
                modifiedRating += (modifier * rate);
                modifiedMax += (modifier * LIKE);
                ++index;
            }
            float finalFloatRating = (modifiedRating/modifiedMax) * 100 ;
            int finalRating = Math.round(finalFloatRating);
            mVolunteer.setRating(finalRating);
            mVolunteer.setRatingHistory(ratingHistory);

            // Update Database with new User info and remove User from rated shift
            dbRef.child(Constants.DB_NODE_USERS).child(mVolunteer.getPushId()).setValue(mVolunteer);
            dbRef.child(Constants.DB_NODE_SHIFTS).child(mShift.getPushId()).child("currentVolunteers").child(mVolunteer.getPushId()).removeValue();

            // Retrieve duration of shift information
            // Start and End times/dates
            String startDate = mShift.getStartDate();
            String endDate = mShift.getEndDate();
            String from = mShift.getStartTime();
            String to = mShift.getEndTime();

            // Time and Date format
            SimpleDateFormat tFormat = new SimpleDateFormat("kk:mm");
            SimpleDateFormat dFormat = new SimpleDateFormat("MM-dd-yyyy");

            try {
                double difference = 0.00;

                // If dates aren't equal, calculate difference
                if(!startDate.equals(endDate)) {
                    Date date1 = dFormat.parse(startDate);
                    Date date2 = dFormat.parse(endDate);
                    difference = date2.getTime() - date1.getTime();
                }

                // Calculate difference for the times
                Date time1 = tFormat.parse(from);
                Date time2 = tFormat.parse(to);
                difference += time2.getTime() - time1.getTime();

                // Convert difference to hours and format correctly
                difference = (difference/(60 * 60 * 1000));
                DecimalFormat df = new DecimalFormat("0.00");
                mDiffInput = df.format(difference);

            } catch (ParseException ex){
                ex.printStackTrace();
            }

            // Move User from current volunteers on shift to rated volunteers
            mShift.getCurrentVolunteers().remove(mVolunteer.getPushId());
            mShift.addRated(mVolunteer.getPushId());
            dbRef.child(Constants.DB_NODE_SHIFTS).child(mShift.getPushId()).child("currentVolunteers").setValue(mShift.getCurrentVolunteers());
            dbRef.child(Constants.DB_NODE_SHIFTS).child(mShift.getPushId()).child("ratedVolunteers").setValue(mShift.getRatedVolunteers());
        }
    }
}