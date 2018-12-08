package com.example.adkoin.adminapp2;

import com.google.firebase.database.Exclude;

/**
 * Created by ramya on 29/9/18.
 */

public class Upload {
    private String mTag;
    private String mCost;
    private String mName;
    private String mImageUrl;
    private String mKey;


    public Upload(){

    }
    public Upload(String tag,String name,String cost, String imageUrl){
        if (tag.trim().equals("")){
            tag= "No Tag";
        }
        if (cost.trim().equals("")){
            cost= "No Cost";
        }

        if (name.trim().equals("")){
            name= "No Name";
        }
        mTag = tag;
        mCost = cost;
        mName = name;
        mImageUrl = imageUrl;

    }
    public String getTag(){
        return mTag;
    }
    public String getCost(){
        return mCost;
    }

    public String getName() {
        return mName;
    }

    public String getImageUrl() {
        return mImageUrl;
    }

    public void setTag(String tag) {
        mTag = tag;
    }
    public void setCost(String cost) {
        mCost = cost;
    }

    public void setName(String name) {
      mName = name;
    }

    public void setImageUrl(String imageUrl) {
        mImageUrl = imageUrl;
    }
    @Exclude
    public String getKey(){
        return mKey;
    }
    @Exclude
    public void setkey(String key){
        mKey = key;
    }
}
