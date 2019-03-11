package com.kuzheevadel.photogallery;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.util.Log;

public class PhotoCache extends LruCache<String, Bitmap> {

    public static final String TAG_CACHE = "PhotoItem";
    public PhotoCache(int maxSize) {
        super(maxSize);
    }

    public Bitmap getBitmapFromCache(String url) {
       return this.get(url);
    }

    public void setBitmapInCache(String url, Bitmap photo) {
        if (getBitmapFromCache(url) == null) {
            this.put(url, photo);
            Log.i(TAG_CACHE, "Picture added in cache: size = " + this.size() + ", URL " + url);
        }
    }
}
