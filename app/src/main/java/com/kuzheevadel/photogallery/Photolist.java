package com.kuzheevadel.photogallery;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

class Photolist {

    @SerializedName("photo")
    ArrayList<GalleryItem> mList;
}
