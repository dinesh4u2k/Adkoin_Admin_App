package com.example.ramya.adminapp2;

import com.google.firebase.database.Exclude;

/**
 * Created by ramya on 29/9/18.
 */

public class Upload {
    private String mName;
    private String mImageUrl;
    private String mKey;


    public Upload(){

    }
    public Upload(String name, String imageUrl){
        if (name.trim().equals("")){
            name= "No Name";
        }
        mName = name;
        mImageUrl = imageUrl;

    }

    public String getName() {
        return mName;
    }

    public String getImageUrl() {
        return mImageUrl;
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
