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
    private final static String FLICKR_RECENT_PHOTOS = "flickr.photos.getRecent";
    private final static String FLICKR_SEARCH_PHOTOS = "flickr.photos.search";
    private static final Uri ENDPOINT = Uri.parse("https://api.flickr.com/services/rest/")
            .buildUpon()
            .appendQueryParameter("api_key", API_KEY)
            .appendQueryParameter("format", "json")
            .appendQueryParameter("nojsoncallback", "1")
            .appendQueryParameter("extras", "url_s")
            .build();

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
    private String getUrlString(String urlSpec) throws IOException{
        return new String(getUrlBytes(urlSpec));
    }

    private List<GalleryItem> downloanGalleryItem(Integer pageNumber, String url) {
        List<GalleryItem> list = new ArrayList<>();

        try {
            String urlPage = Uri.parse(url)
                    .buildUpon()
                    .appendQueryParameter("page", String.valueOf(pageNumber))
                    .build().toString();

            String jsonString = getUrlString(urlPage);
            Log.i(TAG, "Received json " + jsonString);
            list = parseItem(jsonString);
        } catch (IOException e) {
            Log.e(TAG, "Failed to fetch items", e);
        }

        return list;
    }

    private String buildUrl(String method, String query) {
        Uri.Builder builder = ENDPOINT.buildUpon()
                .appendQueryParameter("method", method);

        if (method.equals(FLICKR_SEARCH_PHOTOS)) {
            builder.appendQueryParameter("text", query);
        }

        return builder.build().toString();
    }

    public List<GalleryItem> fetchRecentPhotos(Integer pageNumber) {
        String url = buildUrl(FLICKR_RECENT_PHOTOS, null);
        return downloanGalleryItem(pageNumber, url);
    }

    public List<GalleryItem> fetchSearchPhotos(String text, Integer pageNumber) {
        String url = buildUrl(FLICKR_SEARCH_PHOTOS, text);
        return downloanGalleryItem(pageNumber, url);
    }

    private List<GalleryItem> parseItem(String jsonString) {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        Photo photo = gson.fromJson(jsonString, Photo.class);
        return photo.mPhotolist.mList;
    }

}
