package com.kuzheevadel.photogallery;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class Photolist {

    @SerializedName("photo")
    public ArrayList<GalleryItem> mList;
}
