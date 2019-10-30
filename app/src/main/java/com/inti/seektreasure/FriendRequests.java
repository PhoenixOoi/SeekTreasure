package com.inti.seektreasure;

public class FriendRequests {


    //make sure same anem as in database Messages node
    public String date, time, fullname, request_type;

    public FriendRequests()
    {
        //default constructor
    }

    public FriendRequests(String date, String time, String fullname, String request_type) {
        this.date = date;
        this.time = time;
        this.fullname = fullname;
        this.request_type = request_type;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getRequest_type() {
        return request_type;
    }

    public void setRequest_type(String request_type) {
        this.request_type = request_type;
    }
}
