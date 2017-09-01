package com.example.trio.whatchlist;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Parcelable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.trio.whatchlist.adapter.movie.MovieAdapter;
import com.example.trio.whatchlist.database.FavoriteContract;
import com.example.trio.whatchlist.model.movie.Movie;
import com.example.trio.whatchlist.model.movie.Movies;
import com.example.trio.whatchlist.utilities.Constant;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements MovieAdapter.ItemClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    private List<Movie> results = new ArrayList<>();
    private Gson gson = new Gson();
    private MovieAdapter mAdapter;

    @BindView(R.id.rv_movies) RecyclerView mRecyclerView;
    @BindView(R.id.line_network_retry) LinearLayout mLinearNetworkRetry;
    @BindView(R.id.tv_error_msg) TextView mTvErrorMsg;

    private Parcelable layoutManagerSaveState;
    private String selectedCategory;
    private String categorySelected;

    private LoaderManager.LoaderCallbacks<Cursor> loaderCallbacks;
    private Cursor favoriteData = null;

    private int currentPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mAdapter = new MovieAdapter(results, this);
        GridLayoutManager layoutManager = new GridLayoutManager(this, calculateNoOfColumns(MainActivity.this));

        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(mAdapter);

        if(isWifiConnected() || isNetworkConnected()){
           if(savedInstanceState != null) {
               selectedCategory = savedInstanceState.getString(Constant.KEY_SELECTED_CATEGORY);
               if (selectedCategory.equals(Constant.POPULAR)) {
                   getSupportActionBar().setSubtitle(R.string.action_most_popular);
               } else if (selectedCategory.equals(Constant.TOP_RATED)) {
                   getSupportActionBar().setSubtitle(R.string.action_top_rated);
               } else {
                   getSupportActionBar().setSubtitle(R.string.action_favorites);
               }
               layoutManagerSaveState = savedInstanceState.getParcelable(Constant.LAYOUT_MANAGER);
           } else {
                //default
               selectedCategory = Constant.POPULAR;
               getSupportActionBar().setSubtitle(R.string.action_most_popular);
           }

        } else {
            mRecyclerView.setVisibility(View.INVISIBLE);
            mLinearNetworkRetry.setVisibility(View.VISIBLE);
        }

        if (selectedCategory.equals(Constant.FAVORITES)) {
            setCategory(selectedCategory);
            setupLoader(this, getContentResolver());
            restartLoader(getSupportLoaderManager());
        } else {
            loadData(selectedCategory);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        switch (itemId) {
            case R.id.act_most_popular:
                selectedCategory = Constant.POPULAR;
                resetPage();
                loadData(selectedCategory);
                getSupportActionBar().setSubtitle(R.string.action_most_popular);
                return true;
            case R.id.act_top_rated:
                selectedCategory = Constant.TOP_RATED;
                resetPage();
                loadData(selectedCategory);
                getSupportActionBar().setSubtitle(R.string.action_top_rated);
                return true;
            case R.id.act_favorites:
                selectedCategory = Constant.FAVORITES;
                getSupportActionBar().setSubtitle(R.string.action_favorites);
                setCategory(selectedCategory);
                setupLoader(this, getContentResolver());
                restartLoader(getSupportLoaderManager());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onItemClick(Movie data, int position) {
        Toast.makeText(this, data.getTitle(), Toast.LENGTH_SHORT).show();
        Intent startDetailActivity = new Intent(this, DetailActivity.class);
        startDetailActivity.putExtra(Constant.KEY_MOVIE, gson.toJson(data));
        //startDetailActivity.putExtra(Constant.MOVIE_ID, String.valueOf(data.getId()));
        startActivity(startDetailActivity);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(Constant.KEY_SELECTED_CATEGORY, selectedCategory);
        outState.putParcelable(Constant.LAYOUT_MANAGER, mRecyclerView.getLayoutManager().onSaveInstanceState());
    }

   /* @Override
    public void onRefresh() {
        if (selectedCategory.equals(Constant.FAVORITES)) {
            restartLoader(getSupportLoaderManager());
        } else {
            resetPage();
            loadData(selectedCategory);
        }
    }*/

    private void setupLoader(final MainActivity mainActivity, final ContentResolver contentResolver) {
        loaderCallbacks = new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                return new AsyncTaskLoader<Cursor>(mainActivity) {
                    @Override
                    public Cursor loadInBackground() {
                        try {
                            return contentResolver.query(
                                    FavoriteContract.Entry.CONTENT_URI,
                                    null,
                                    null,
                                    null,
                                    null
                            );
                        } catch (Exception e) {
                            e.printStackTrace();
                            return null;
                        }
                    }

                    @Override
                    protected void onStartLoading() {
                        if (categorySelected.equals(Constant.FAVORITES)) {
                            forceLoad();
                        }
                    }
                };
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
                Log.d("Favorites found: ", ""+data.getCount());
                favoriteData = data;
                generateFromCursor(favoriteData);
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {

            }
        };
    }

    private void generateFromCursor(Cursor cursor) {
        List<Movie> results = new ArrayList<>();
        cursor.moveToPosition(-1);
        try {
            while (cursor.moveToNext()) {
                Movie movieResult = new Movie();
                movieResult.setBackdrop_path(
                        cursor.getString(
                                cursor.getColumnIndex(FavoriteContract.Entry.COLUMN_BACKDROP)
                        )
                );
                movieResult.setPoster_path(
                        cursor.getString(
                                cursor.getColumnIndex(FavoriteContract.Entry.COLUMN_POSTER)
                        )
                );
                movieResult.setTitle(
                        cursor.getString(
                                cursor.getColumnIndex(FavoriteContract.Entry.COLUMN_TITLE)
                        )
                );
                movieResult.setRelease_date(
                        cursor.getString(
                                cursor.getColumnIndex(FavoriteContract.Entry.COLUMN_RELEASE_DATE)
                        )
                );
                movieResult.setOverview(
                        cursor.getString(
                                cursor.getColumnIndex(FavoriteContract.Entry.COLUMN_OVERVIEW)
                        )
                );
                movieResult.setVote_average(
                        cursor.getDouble(
                                cursor.getColumnIndex(FavoriteContract.Entry.COLUMN_RATING)
                        )
                );
                movieResult.setId(
                        cursor.getInt(
                                cursor.getColumnIndex(FavoriteContract.Entry.COLUMN_MOVIE_ID)
                        )
                );
                results.add(movieResult);
            }
        } finally {
            cursor.close();
        }

        onDataReceived(results, 1);
    }

    private void onDataReceived(List<Movie> results, int page) {
        Log.d("received", ""+results.size());
        if (page > 1) {
            mAdapter.updateData(results);
        } else {
            mAdapter.replaceAll(results);

            // retain scroll position
            if (layoutManagerSaveState != null) {
                mRecyclerView.getLayoutManager().onRestoreInstanceState(layoutManagerSaveState);
            }
        }
    }

    private void restartLoader(LoaderManager supportLoaderManager) {
        supportLoaderManager.restartLoader(Constant.LOADER_MAIN_ID, null, loaderCallbacks);
    }

    private void resetPage() {
        currentPage = 1;
    }

    private void setCategory(String selectedCategory) {
        categorySelected = selectedCategory;
    }

    private boolean isNetworkConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    private boolean isWifiConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected() && (ConnectivityManager.TYPE_WIFI == networkInfo.getType());
    }

    private int calculateNoOfColumns(MainActivity mainActivity) {
        DisplayMetrics displayMetrics = mainActivity.getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels /displayMetrics.density;
        int scalingFactor = 180;
        int noOfColumns = (int) (dpWidth / scalingFactor);
        return  noOfColumns;
    }

    private void loadData(String category) {
        mAdapter.replaceAll(results);
        if (category.equals(Constant.POPULAR))
            getDataFromAPI(Constant.POPULAR);
        else
            getDataFromAPI(Constant.TOP_RATED);
    }

    private void getDataFromAPI(String selectedCategory) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String url = Constant.URL_API + selectedCategory + Constant.PARAM_API_KEY + Constant.API_KEY;
        StringRequest stringRequest = new StringRequest(
                Request.Method.GET,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            Movies movies = gson.fromJson(response, Movies.class);
                            results.clear();
                            for (Movie item : movies.getResults()) {
                                results.add(item);
                            }
                            mAdapter.notifyDataSetChanged();
                        } catch (Exception e) {
                            Log.e(TAG, e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error != null) {
                            Log.e(TAG, error.getMessage());
                        } else {
                            Log.e(TAG, "Something error happened!");
                        }
                    }
                }
        );
        requestQueue.add(stringRequest);
    }
}
