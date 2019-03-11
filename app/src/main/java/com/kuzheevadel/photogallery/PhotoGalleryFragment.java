package com.kuzheevadel.photogallery;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class PhotoGalleryFragment extends Fragment {

    private RecyclerView mRecyclerView;
    List<GalleryItem> mGalleryItems = new ArrayList<>();
    private PhotoAdapter mPhotoAdapter;
    private static int page = 1;
    private boolean isLoading = false;
    private int columnCount;
    private static final String TAG_DISLPAY = "DisplayCount";
    private ThubnailDownloader<PhotoViewHolder> mThubnailDownloader;
    private PhotoCache mCache;

    public static PhotoGalleryFragment newInstance() {
        return new PhotoGalleryFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        new FetchItemTask().execute(page);

        Handler responseHandler = new Handler();
        mCache = new PhotoCache(200);
        mThubnailDownloader = new ThubnailDownloader<>(responseHandler, mCache);

        mThubnailDownloader.setThumbnailDownloadListener((target, thumbnail) -> {
            Drawable drawable = new BitmapDrawable(getResources(), thumbnail);
            target.bindGalleryItem(drawable);
            Log.i("Looper", "ImageView обновлен");
        });

        String message;
        if (mThubnailDownloader.getLooper() == null) {
            message = "Looper is null" + getActivity().getMainLooper().toString();
        } else {
            message = mThubnailDownloader.getLooper().toString();
        }

        Log.i("Looper", "До start " + message);

        mThubnailDownloader.start();
        Log.i("Looper", "После start()" + mThubnailDownloader.getLooper().toString());
        mThubnailDownloader.getLooper();

        Log.i("Looper", "После getLooper()  " + mThubnailDownloader.getLooper().toString());
        Log.i(TAG_DISLPAY, "Background thread started");
    }

    private void setAdapter() {
        if (isAdded()) {
                mPhotoAdapter = new PhotoAdapter(mGalleryItems);
                mRecyclerView.setAdapter(mPhotoAdapter);
                Log.i("FlickrFetch", "Adapters count = " + String.valueOf(mPhotoAdapter.getItemCount()));
        }
    }

    private int getRwColumnsCount() {
        WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        Log.i(TAG_DISLPAY, String.valueOf(width));
        return width / 300;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_fragment, container, false);
        mRecyclerView = v.findViewById(R.id.photo_gallery_recycler);

       columnCount = getRwColumnsCount();
        Log.i(TAG_DISLPAY, "columnsCount = " + String.valueOf(columnCount));

        final GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), columnCount);
        mRecyclerView.setLayoutManager(layoutManager);

        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int visibleItemCount = layoutManager.getChildCount();
                int firstVisibleItem = layoutManager.findFirstVisibleItemPosition();
                int totalItemsCount = layoutManager.getItemCount();

                if (!isLoading) {
                    if ((visibleItemCount + firstVisibleItem) >= totalItemsCount - 30) {
                        new FetchItemTask().execute(page);
                    }
                }

            }
        });
        setAdapter();
        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mThubnailDownloader.clearQueue();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mThubnailDownloader.quit();
        Log.i(TAG_DISLPAY, "Background thread destroyed");
    }

    private class PhotoViewHolder extends RecyclerView.ViewHolder {
        private ImageView mItemImageView;

        public PhotoViewHolder(View v) {
            super(v);
            mItemImageView = v.findViewById(R.id.item_image_view);
        }

        public void bindGalleryItem(Drawable drawable) {
            mItemImageView.setImageDrawable(drawable);
        }
    }

    private class PhotoAdapter extends RecyclerView.Adapter<PhotoViewHolder> {

        List<GalleryItem> items;

        public PhotoAdapter(List<GalleryItem> list) {
            items = list;
        }

        @NonNull
        @Override
        public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View v = inflater.inflate(R.layout.list_item_gallery, viewGroup, false);

            return new PhotoViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull PhotoViewHolder photoViewHolder, int i) {
            GalleryItem item = items.get(i);
            if (item.getUrl() == null) {

                photoViewHolder.mItemImageView.setImageDrawable(getResources().getDrawable(R.drawable.bill_up_close));

            } else if (mCache.getBitmapFromCache(item.getUrl()) != null) {
                Drawable drawable = new BitmapDrawable(getResources(), mCache.getBitmapFromCache(item.getUrl()));
                photoViewHolder.bindGalleryItem(drawable);
                Log.i(PhotoCache.TAG_CACHE, "Set picture from cache");
            } else {
                mThubnailDownloader.queueThubnail(photoViewHolder, item.getUrl());
                Log.i("Looper", "Холдер и URL отправдены");
            }
        }


        @Override
        public int getItemCount() {
            return items.size();
        }
    }

    public class FetchItemTask extends AsyncTask<Integer, Void, List<GalleryItem>> {

        @Override
        protected List<GalleryItem> doInBackground(Integer ... integers) {
            page++;
            isLoading = true;
            String query = "bugatti";

            if (query == null) {
                return new FlickrFetchr().fetchRecentPhotos(integers[0]);
            } else {
                return new FlickrFetchr().fetchSearchPhotos(query, page);
            }
        }

        @Override
        protected void onPostExecute(List<GalleryItem> items) {
            Log.i("FlickrFetch", "items size = " + String.valueOf(items.size()));
            if (mGalleryItems.size() == 0) {
                mGalleryItems = items;
                setAdapter();
                isLoading = false;
                Log.i("FlickrFetch", "mGalleryItems size = " + String.valueOf(mGalleryItems.size()));
            } else {
                mGalleryItems.addAll(items);
                mPhotoAdapter.notifyItemInserted(mPhotoAdapter.getItemCount() - items.size());
                isLoading = false;
                Log.i("FlickrFetch", "mGalleryItems size = " + String.valueOf(mGalleryItems.size()));
            }
        }
    }

}
