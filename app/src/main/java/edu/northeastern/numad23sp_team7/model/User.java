package edu.northeastern.numad23sp_team7.model;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class User {
    private String username;
    //    public boolean isSignedIn;
    private ArrayList<History> sentRecords;
    private ArrayList<History> receivedRecords;

    public User(String username) {
        this.username = username;
        this.sentRecords = new ArrayList<>();
        this.receivedRecords = new ArrayList<>();
    }

    public User() {
    }


    public String getUsername() {
        return username;
    }

    public ArrayList<History> getSentRecords() {
        return sentRecords;
    }


    public ArrayList<History> getReceivedRecords() {
        return receivedRecords;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("username", username);
        result.put("setRecords", sentRecords);
        result.put("receivedRecords", receivedRecords);

        return result;
    }
}

