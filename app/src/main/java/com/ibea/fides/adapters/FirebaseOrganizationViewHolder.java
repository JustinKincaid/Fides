package com.ibea.fides.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.ibea.fides.Constants;
import com.ibea.fides.R;
import com.ibea.fides.models.Organization;
import com.ibea.fides.models.User;


/**
 * Created by KincaidJ on 1/28/17.
 */

//This is for the admin page. Don't get it twisted.

public class FirebaseOrganizationViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private View mView;
    private Organization mOrganization;

    public FirebaseOrganizationViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        itemView.setOnClickListener(this);
    }

    public void bindOrganization(Organization organization) {

        TextView nameText = (TextView) mView.findViewById(R.id.nameText);
        mOrganization = organization;
        nameText.setText(organization.getName());
    }

    @Override
    public void onClick(View view) {
        //TODO: Add rejection option
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        String organizationPushId = mOrganization.getPushId();

        //Remove from applications node
        dbRef.child(Constants.DB_NODE_APPLICATIONS).child(organizationPushId).removeValue();

        //Create organization
        dbRef.child(Constants.DB_NODE_ORGANIZATIONS).child(organizationPushId).setValue(mOrganization);;

        //Create user entry for organization
        User newUser = new User(organizationPushId, mOrganization.getName(), mOrganization.getContactEmail());
        newUser.setIsOrganization(true);
        dbRef.child(Constants.DB_NODE_USERS).child(organizationPushId).setValue(newUser);

        //Create search entry
        String searchKey = mOrganization.getName() + "|" + mOrganization.getZipcode() + "|" + mOrganization.getCityAddress() + "|" + mOrganization.getStateAddress();
        dbRef.child(Constants.DB_NODE_SEARCH).child(Constants.DB_SUBNODE_ORGANIZATIONS).child(organizationPushId).setValue(searchKey);
    }
}