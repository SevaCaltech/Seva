package edu.caltech.seva.models;

import android.support.annotation.Nullable;

import java.util.ArrayList;

import static java.util.Objects.hash;

/**
 * Data object containing relevant user information. Needed for querying the database.
 */
public class User {
    private String name;
    private String email;
    private String phone;
    private String uid;

    private ArrayList<String> toiletNames;
    private ArrayList<Toilet> toiletList;

    /**
     * Constructor for the User object. Taking all info from DynamoDB.
     *
     * @param name        Name of user
     * @param email       Email of user
     * @param phone       Phone number of user
     * @param uid         AWS Congnito user ID
     * @param toiletNames List of toilet names assigned to user
     */
    public User(String name, String email, String phone, String uid, ArrayList<String> toiletNames) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.uid = uid;
        this.toiletNames = toiletNames;
        this.toiletList = new ArrayList<>();
    }

    /**
     * Constructor for the User object. Doesn't include phone number
     *
     * @param name        Name of user
     * @param email       Email of user
     * @param uid         AWS Cognito user ID
     * @param toiletNames List of toilet names assigned to user
     */
    public User(String name, String email, String uid, ArrayList<String> toiletNames) {
        this.name = name;
        this.email = email;
        this.phone = "";
        this.uid = uid;
        this.toiletNames = toiletNames;
        this.toiletList = new ArrayList<>();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof User)) {
            return false;
        }
        User u = (User) obj;

        return u.toiletNames.equals(this.toiletNames) && u.phone.equals(this.phone)
                && u.email.equals(this.email) && u.name.equals(this.name)
                && u.uid.equals(this.uid);
    }

    @Override
    public int hashCode() {
        return hash(toiletList, toiletNames, phone, email, uid, name);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public ArrayList<String> getToiletNames() {
        return toiletNames;
    }

    public void setToiletNames(ArrayList<String> toiletNames) {
        this.toiletNames = toiletNames;
    }

    public ArrayList<Toilet> getToiletList() {
        return toiletList;
    }

    public void setToiletList(ArrayList<Toilet> toiletList) {
        this.toiletList = toiletList;
    }

    public int getNumToilets() {
        return toiletNames.size();
    }
}
