package com.inti.seektreasure;

public class Friends
{
    public String date; //make sure name same as database inside Friends parent node

    public Friends()
    {
        //default constructor
    }

    public Friends(String date)
    {
        this.date = date;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
