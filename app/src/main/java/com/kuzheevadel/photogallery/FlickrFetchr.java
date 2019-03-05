package com.kuzheevadel.photogallery;

import android.net.Uri;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class FlickrFetchr {

    private static final String TAG = "FlickrFetch";
    private static final String API_KEY = "a2151c9414f1522ffd1fa7ffee126670";

    //raw data from URL
    public byte[] getUrlBytes(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException(connection.getResponseMessage() + ": with " + urlSpec);
            }

            int bytesRead;
            byte[] buffer = new byte[1024];

            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            return out.toByteArray();
        } finally {
            connection.disconnect();
        }
    }

    //Bytes to String
    public String getUrlString(String urlSpec) throws IOException{
        return new String(getUrlBytes(urlSpec));
    }

    public List<GalleryItem> fetchItems(Integer pageNumber) {
        List<GalleryItem> list = new ArrayList<>();

        try {
            String url = Uri.parse("https://api.flickr.com/services/rest/")
                    .buildUpon()
                    .appendQueryParameter("method", "flickr.photos.getRecent")
                    .appendQueryParameter("api_key", API_KEY)
                    .appendQueryParameter("format", "json")
                    .appendQueryParameter("nojsoncallback", "1")
                    .appendQueryParameter("extras", "url_s")
                    .appendQueryParameter("page", String.valueOf(pageNumber))
                    .build().toString();

            String jsonString = getUrlString(url);
            Log.i(TAG, "Received json " + jsonString);
            list = parseItem(jsonString);
        } catch (IOException e) {
            Log.e(TAG, "Failed to fetch items", e);
        }

        return list;
    }

    private List<GalleryItem> parseItem(String jsonString) {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        Photo photo = gson.fromJson(jsonString, Photo.class);

        Log.i(TAG, "Photo url: " + photo.mPhotolist.mList.get(1).getUrl());
        return photo.mPhotolist.mList;
    }

}
