package com.ibea.fides.models;

import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by KincaidJ on 1/24/17.
 */

@Parcel
public class User {
    String pushId;
    String name;
    String email;
    boolean isOrganization = false;
    boolean isAdmin = false;
    List<Integer> ranking = new ArrayList<>();

    // Empty Constructor for Parceler
    public User() {}

    // Basic Constructor
    public User(String pushId, String name, String email) {
        this.pushId = pushId;
        this.name = name;
        this.email = email;
    }

    // Getters and Setters
    public String getPushId() {
        return pushId;
    }
    public void setPushId(String pushId) {
        this.pushId = pushId;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public boolean getIsOrganization() { return isOrganization; }
    public void setIsOrganization(boolean isOrganization) {
        this.isOrganization = isOrganization;
    }
    public boolean getIsAdmin() { return isAdmin; }
    public void setIsAdmin(boolean isAdmin) { this.isAdmin = isAdmin; }
    public List<Integer> getRanking() { return ranking; }
    public void setRanking(List<Integer> ranking) { this.ranking = ranking; }


    // Rating
    public int getRank() { return ((this.ranking.get(0)/this.ranking.get(1))*100); }
}
