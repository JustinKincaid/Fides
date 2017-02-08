package com.ibea.fides.ui;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ibea.fides.Constants;
import com.ibea.fides.R;
import com.ibea.fides.adapters.NewShiftSearchAdapter;
import com.ibea.fides.models.Shift;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class NewShiftSearchFragment extends Fragment implements View.OnClickListener{
    @Bind(R.id.searchView_City) SearchView mSearchView_City;
    @Bind(R.id.searchView_State) SearchView mSearchView_State;
    @Bind(R.id.searchView_Zip) SearchView mSearchView_Zip;
    @Bind(R.id.recyclerView) RecyclerView mRecyclerView;
    @Bind(R.id.button_Search) Button mButton_Search;

    private DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();

    private final String TAG = "NewShiftsSearchFragment";
    private ArrayList<Shift> shifts = new ArrayList<>();
    private RecyclerView.Adapter mRecyclerAdapter;
    private Context mContext;
    private String zipQuery;

    public NewShiftSearchFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_shift_search, container, false);
        ButterKnife.bind(this, view);
        mContext = this.getContext();

        //TODO: Replace with population from users once all users are required to have these fields

        mSearchView_State.setQuery("OR", false);
        mSearchView_City.setQuery("Portland", false);

        mRecyclerAdapter = new NewShiftSearchAdapter(mContext, shifts);
        mRecyclerView.setHasFixedSize(false);
        mRecyclerView.setAdapter(mRecyclerAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));

        mButton_Search.setOnClickListener(this);

        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onClick(View view) {
        if(view == mButton_Search){
            //TODO: Lowercase cityQuery once database also has lowercase city nodes
            String cityQuery = mSearchView_City.getQuery().toString();
            String stateQuery = mSearchView_State.getQuery().toString();
            String zipQuery = mSearchView_Zip.getQuery().toString();

            //TODO: Remove stateQuery check once state dropdown is in
            if(cityQuery.length() != 0 && stateQuery.length() != 0 && validateZip(zipQuery)){
                Boolean filterByZip;
                if(zipQuery.length() == 0){
                    filterByZip = false;
                }else filterByZip = true;
                fetchShiftIds(cityQuery, stateQuery, filterByZip);
            }else{
                if(cityQuery.length() == 0){
                    Toast.makeText(mContext, "Please enter a city", Toast.LENGTH_SHORT).show();
                }
                if(stateQuery.length() == 0){
                    Toast.makeText(mContext, "Please enter a valid state", Toast.LENGTH_SHORT).show();
                }
                if(!validateZip(zipQuery)){
                    Toast.makeText(mContext, "Invalid zip code", Toast.LENGTH_SHORT).show();
                }
            }
            //Sets off a series of functions that fetches shift Ids, resolves them, and then filters them.
        }
    }

    //Retrieves full list of shiftIDs
    public void fetchShiftIds(String _city, String _state, final Boolean filterByZip){
        Log.d(TAG, "in fetchShifts");

        int itemCount = shifts.size();
        shifts.clear();
        mRecyclerAdapter.notifyItemRangeRemoved(0, itemCount);

        final ArrayList<String> shiftIds = new ArrayList<>();
        DatabaseReference dbShiftsByStateCity = dbRef.child(Constants.DB_NODE_SHIFTSAVAILABLE).child(Constants.DB_SUBNODE_STATECITY);

        Query query = dbShiftsByStateCity.child(_state).child(_city).orderByKey().limitToFirst(25);
        Log.d(TAG, String.valueOf(shifts.size()));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "fetching shifts");
                for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                    String shiftId = snapshot.getValue(String.class);
                    fetchShift(shiftId, filterByZip);
                }
                Log.d(TAG, String.valueOf(shifts.size()));

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void fetchShift(String _shiftId, final Boolean filterByZip){
        final String zipQuery = mSearchView_Zip.getQuery().toString();

        dbRef.child(Constants.DB_NODE_SHIFTS).child(_shiftId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Shift shift = dataSnapshot.getValue(Shift.class);

                if(filterByZip && filterByZip(shift, zipQuery)){
                    shifts.add(shift);
                    mRecyclerAdapter.notifyItemInserted(shifts.indexOf(shift));
                }else if(!filterByZip){
                    shifts.add(shift);
                    mRecyclerAdapter.notifyItemInserted(shifts.indexOf(shift));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    public Boolean filterByZip(Shift _shift, String zipQuery){
        if(_shift.getZip().equals(zipQuery)){
            return true;
        }else return false;
    }

    public Boolean validateZip(String _query){
        String onlyNumbers = "[0-9]+";

        if(_query.length() != 0){
            if(_query.length() == 5 && _query.matches(onlyNumbers)){
                return true;
            }else{
                //TODO: Zack fix this toast
                Toast.makeText(mContext, "Invalid zip code", Toast.LENGTH_SHORT).show();
                return false;
            }
        }else return true;
    }

    @Override
    public void onPause() {
        super.onPause();
        mSearchView_Zip.setQuery("", false);
        mSearchView_City.setQuery("", false);
        mSearchView_State.setQuery("", false);
    }
}
