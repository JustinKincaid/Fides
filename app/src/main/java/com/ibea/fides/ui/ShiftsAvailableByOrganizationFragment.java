package com.ibea.fides.ui;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ibea.fides.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class ShiftsAvailableByOrganizationFragment extends Fragment {


    public ShiftsAvailableByOrganizationFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_shifts_available_by_organization, container, false);
    }

}
