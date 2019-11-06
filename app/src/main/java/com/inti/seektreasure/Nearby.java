package com.inti.seektreasure;

/**
 * Create by OCF on 2019
 */

public class Nearby {
    //use same name in database
    public String profileimage, fullname, status, country;

    public Nearby()
    {
        //empty constructor
    }

    public Nearby(String profileimage, String fullname, String status, String country) {
        this.profileimage = profileimage;
        this.fullname = fullname;
        this.status = status;
        this.country = country;
    }

    public String getProfileimage() {
        return profileimage;
    }

    public void setProfileimage(String profileimage) {
        this.profileimage = profileimage;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}

