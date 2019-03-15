package com.kuzheevadel.photogallery;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class Photolist {
    public ArrayList<GalleryItem> getList() {
        return mList;
    }

    @SerializedName("photo")
    public ArrayList<GalleryItem> mList;
}
