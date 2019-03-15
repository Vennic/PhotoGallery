package com.kuzheevadel.photogallery;

import com.google.gson.annotations.SerializedName;

public class Photo {

    @SerializedName("photos")
    public Photolist mPhotolist;

    public Photolist getPhotolist() {
        return mPhotolist;
    }

    @Override
    public String toString() {
        return String.valueOf(mPhotolist.mList.size());
    }
}

