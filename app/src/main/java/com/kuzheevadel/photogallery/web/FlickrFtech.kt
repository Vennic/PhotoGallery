package com.kuzheevadel.photogallery.web

import android.util.Log
import com.kuzheevadel.photogallery.GalleryItem
import com.kuzheevadel.photogallery.Photo
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

class FlickrFtech private constructor(){
    private val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://api.flickr.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    private val filckAri = retrofit.create(FlickAPI::class.java)

    fun getApi(): FlickAPI {
        return filckAri
    }

    private object Flickr {
        val INSTANCE = FlickrFtech()
    }

    companion object {
        val instance: FlickrFtech by lazy {
            Flickr.INSTANCE
        }
    }
}

interface FlicrDownload {

    fun onDownloaded(list: ArrayList<GalleryItem>)
}

interface FlickAPI {

    @GET("/services/rest/")
    fun getPhoto(@Query("method") method: String,
                 @Query("api_key") apiKey: String,
                 @Query("format") format: String,
                 @Query("nojsoncallback") jsonCallback: String,
                 @Query("extras") url: String,
                 @Query("page") pageNumber: String,
                 @Query("text") text: String?): Call<Photo>
}

fun getPhotos(typeOfMethod: Int, text: String?, pageNumber: Int, download: FlicrDownload) {
    val searchMethod = "flickr.photos.search"
    val recentPhotos = "flickr.photos.getRecent"
    val apiKey = "a2151c9414f1522ffd1fa7ffee126670"
    var photos: ArrayList<GalleryItem>

    val method = if (typeOfMethod == 1) recentPhotos else searchMethod

    FlickrFtech.instance.getApi().getPhoto(method, apiKey, "json", "1", "url_s", pageNumber.toString(), text)
            .enqueue(object : Callback<Photo> {
                override fun onFailure(call: Call<Photo>, t: Throwable) {
                    Log.i("Retrofit", t.message)
                }

                override fun onResponse(call: Call<Photo>, response: Response<Photo>) {
                    val photo: Photo? = response.body()
                    Log.i("Retrofit", photo.toString())
                    photos = photo!!.photolist.list
                    download.onDownloaded(photos)
                }
            })

}