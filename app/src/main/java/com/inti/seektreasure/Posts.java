package com.inti.seektreasure;


//retrieve posts information from firebase
public class Posts
{
    public String uid, time, date, postimage, description, price, category, pname, profileimage, fullname;

    //create default constructor
    public Posts()
    {
        //create module class
    }


    //constructor with parameters
    public Posts(String uid, String time, String date, String postimage, String description, String price, String category, String pname, String profileimage, String fullname) {
        this.uid = uid;
        this.time = time;
        this.date = date;
        this.postimage = postimage;
        this.description = description;
        this.price = price;
        this.category = category;
        this.pname = pname;
        this.profileimage = profileimage;
        this.fullname = fullname;
    }

    //getter and setter

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getPostimage() {
        return postimage;
    }

    public void setPostimage(String postimage) {
        this.postimage = postimage;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPname() {
        return pname;
    }

    public void setPname(String pname) {
        this.pname = pname;
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
}


