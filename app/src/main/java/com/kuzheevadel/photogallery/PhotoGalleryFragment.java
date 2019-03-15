package com.kuzheevadel.photogallery;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.support.v7.widget.SearchView;
import android.widget.ProgressBar;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;


public class PhotoGalleryFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private ProgressBar mProgressBar;
    List<GalleryItem> mGalleryItems = new ArrayList<>();
    private PhotoAdapter mPhotoAdapter;
    private int pageNumber = 1;
    private boolean isLoading = false;
    private static final String TAG_DISPLAY = "DisplayCount";
    private Picasso mPicasso;
    private int jobId;

    public static PhotoGalleryFragment newInstance() {
        return new PhotoGalleryFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
        jobId = 1503201915;

        PullServiceJobService.startJob(getActivity(), jobId);

        updateItems(pageNumber);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_photo_gallery, menu);

        MenuItem menuItem = menu.findItem(R.id.menu_item_search);
        SearchView searchView = (SearchView) menuItem.getActionView();

        MenuItem toggleItem = menu.findItem(R.id.menu_item_toggle_pulling);

        if (PullServiceJobService.isAlarmOn(getActivity(), jobId)) {
            toggleItem.setTitle(R.string.stop_polling);
        } else {
            toggleItem.setTitle(R.string.start_polling);
        }

        searchView.setOnSearchClickListener(v -> {
            String query = QueryPreferences.getStoredQuery(Objects.requireNonNull(getActivity()));
            searchView.setQuery(query, false);
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mRecyclerView.setAdapter(null);
                mGalleryItems.clear();
                pageNumber = 1;
                searchView.clearFocus();
                searchView.onActionViewCollapsed();
                QueryPreferences.setStoredQuery(Objects.requireNonNull(getActivity()), query);
                updateItems(pageNumber);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_clear:
                mRecyclerView.setAdapter(null);
                QueryPreferences.setStoredQuery(Objects.requireNonNull(getActivity()), null);
                mGalleryItems.clear();
                pageNumber = 1;
                updateItems(pageNumber);
                return true;
            case R.id.menu_item_toggle_pulling:
                boolean shouldStartAlarm = !PullServiceJobService.isAlarmOn(getActivity(), jobId);

                if (shouldStartAlarm) {
                    PullServiceJobService.startJob(getActivity(), jobId);
                } else {
                    PullServiceJobService.stopJob(getActivity(), jobId);
                }

                getActivity().invalidateOptionsMenu();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateItems(Integer page) {
        String query = QueryPreferences.getStoredQuery(Objects.requireNonNull(getActivity()));
        new FetchItemTask(query).execute(page);
    }

    private void setAdapter() {
        if (isAdded()) {
                mPhotoAdapter = new PhotoAdapter(mGalleryItems);
                mRecyclerView.setAdapter(mPhotoAdapter);
                Log.i("FlickrFetch", "Adapters count = " + String.valueOf(mPhotoAdapter.getItemCount()));
        }
    }

    private int getRwColumnsCount() {
        WindowManager wm = (WindowManager) Objects.requireNonNull(getActivity()).getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        Log.i(TAG_DISPLAY, String.valueOf(width));
        return width / 300;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_fragment, container, false);
        mRecyclerView = v.findViewById(R.id.photo_gallery_recycler);
        mProgressBar = v.findViewById(R.id.progressBar);

        int columnCount = getRwColumnsCount();
        Log.i(TAG_DISPLAY, "columnsCount = " + String.valueOf(columnCount));

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
                        new FetchItemTask(QueryPreferences.getStoredQuery(Objects.requireNonNull(getActivity()))).execute(pageNumber);
                    }
                }

            }
        });
        setAdapter();
        return v;
    }


    private class PhotoViewHolder extends RecyclerView.ViewHolder {
        private ImageView mItemImageView;

        PhotoViewHolder(View v) {
            super(v);
            mItemImageView = v.findViewById(R.id.item_image_view);
        }

    }

    private class PhotoAdapter extends RecyclerView.Adapter<PhotoViewHolder> {

        List<GalleryItem> items;

        PhotoAdapter(List<GalleryItem> list) {
            items = list;
            mPicasso = Picasso.get();
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
            mPicasso.load(item.getUrl())
                    .into(photoViewHolder.mItemImageView);
        }


        @Override
        public int getItemCount() {
            return items.size();
        }
    }

    public class FetchItemTask extends AsyncTask<Integer, Void, List<GalleryItem>> {

        private String mQuery;

        FetchItemTask(String query) {
            mQuery = query;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (mProgressBar != null && pageNumber == 1) {
                mProgressBar.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected List<GalleryItem> doInBackground(Integer ... integers) {
            isLoading = true;

            if (mQuery == null) {
                pageNumber++;
                return new FlickrFetchr().fetchRecentPhotos(integers[0]);
            } else {
                pageNumber++;
                return new FlickrFetchr().fetchSearchPhotos(mQuery, integers[0]);
            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
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

            if (mProgressBar != null) {
                mProgressBar.setVisibility(View.GONE);
            }

        }
    }

}
