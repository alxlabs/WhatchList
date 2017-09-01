package com.example.trio.whatchlist;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.trio.whatchlist.adapter.review.ReviewAdapter;
import com.example.trio.whatchlist.adapter.trailer.TrailerAdapter;
import com.example.trio.whatchlist.database.FavoriteContract;
import com.example.trio.whatchlist.model.movie.Movie;
import com.example.trio.whatchlist.model.review.Review;
import com.example.trio.whatchlist.model.review.Reviews;
import com.example.trio.whatchlist.model.trailer.Trailer;
import com.example.trio.whatchlist.model.trailer.Trailers;
import com.example.trio.whatchlist.utilities.Constant;
import com.example.trio.whatchlist.utilities.DateFormatter;
import com.example.trio.whatchlist.utilities.ImageUrlBuilder;
import com.example.trio.whatchlist.utilities.TrailerUtil;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailActivity extends AppCompatActivity implements TrailerAdapter.TrailerItemClickListener{
    private static final String TAG = DetailActivity.class.getSimpleName();

    @BindView(R.id.iv_backdrop_path) ImageView backdrop;
    @BindView(R.id.iv_poster) ImageView poster;
    @BindView(R.id.tv_release_date) TextView releaseDate;
    @BindView(R.id.tv_vote_average) TextView voteAverage;
    @BindView(R.id.tv_overview) TextView overview;
    @BindView(R.id.parent_detail) CoordinatorLayout parentDetail;
    @BindView(R.id.fab) FloatingActionButton fab;
    @BindView(R.id.toolbar) Toolbar toolbar;

    private String jsonData;
    private Movie movieResults;
    private Gson gson = new Gson();

    private LoaderManager.LoaderCallbacks<Cursor> loaderCallbacks;

    @BindView(R.id.rv_trailers) RecyclerView mTrailerRecyclerView;
    private TrailerAdapter mTrailerAdapter;
    private List<Trailer> trailerResult = new ArrayList<>();

    @BindView(R.id.rv_reviews) RecyclerView mReviewRecyclerView;
    private ReviewAdapter mReviewAdapter;
    private List<Review> reviewResult = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        jsonData = getIntent().getStringExtra(Constant.KEY_MOVIE);

        if (jsonData != null) {
            movieResults = getMovieItem(jsonData);

            bindData();
            trailerRecyclerView();
            reviewRecyclerView();
            setupLoader(this, getContentResolver(), movieResults.getId());
            initLoader(getSupportLoaderManager());

            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if ((Integer)fab.getTag() == R.drawable.ic_star_selected) {
                        unsetAsFavorite(getContentResolver(), movieResults);
                    } else {
                        saveAsFavorite(getContentResolver(), movieResults);
                        restartLoader(getSupportLoaderManager());
                    }
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        restartLoader(getSupportLoaderManager());
    }

    @Override
    public void onTrailerItemClick(Trailer data, int position) {
        Uri webPage = Uri.parse(TrailerUtil.getYoutubeUrl(data.getKey()));
        Intent intent = new Intent(Intent.ACTION_VIEW, webPage);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    private Movie getMovieItem(String json) {
        return gson.fromJson(json, Movie.class);
    }

    private void bindData() {
        parentDetail.setVisibility(View.VISIBLE);
        getSupportActionBar().setTitle(movieResults.getTitle());
        Picasso.with(this)
                .load(ImageUrlBuilder.getBackdropUrl(movieResults.getBackdrop_path()))
                .placeholder(R.drawable.ic_local_movies)
                .error(R.drawable.ic_error)
                .into(backdrop);
        Picasso.with(this)
                .load(ImageUrlBuilder.getPosterUrl(movieResults.getPoster_path()))
                .placeholder(R.drawable.ic_local_movies)
                .error(R.drawable.ic_error)
                .into(poster);
        releaseDate.setText(DateFormatter.getReadableDate(movieResults.getRelease_date()));
        voteAverage.setText(String.valueOf(movieResults.getVote_average()));
        overview.setText(movieResults.getOverview());
    }

    private void initLoader(LoaderManager supportLoaderManager) {
        supportLoaderManager.initLoader(Constant.LOADER_ID, null, loaderCallbacks);
    }

    private void setupLoader(final DetailActivity detailActivity, final ContentResolver contentResolver, final long movieID) {
        loaderCallbacks = new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                return new AsyncTaskLoader<Cursor>(detailActivity) {
                    @Override
                    public Cursor loadInBackground() {
                        try {
                            return contentResolver.query(
                                    uriWithIDBuilder(movieID),
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
                        forceLoad();
                    }
                };
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
                setFavoriteButton(data.getCount());
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {

            }
        };
    }

    private void setFavoriteButton(int count) {
        if (count > 0) {
            onStatusReceived(true);
        } else {
            onStatusReceived(false);
        }
    }

    private void unsetAsFavorite(ContentResolver contentResolver, Movie movieItem) {
        long result = contentResolver.delete(uriWithIDBuilder(movieItem.getId()), null, null);
        if (result > 0) {
            restartLoader(getSupportLoaderManager());
        }
    }

    private void onStatusReceived(boolean isFavorite) {
        if (isFavorite) {
            fab.setImageResource(R.drawable.ic_star_selected);
            fab.setTag(R.drawable.ic_star_selected);
        } else {
            fab.setImageResource(R.drawable.ic_star_unselected);
            fab.setTag(R.drawable.ic_star_unselected);
        }
    }

    private void saveAsFavorite(ContentResolver resolver, Movie item) {
        ContentValues cv = new ContentValues();
        cv.put(FavoriteContract.Entry.COLUMN_MOVIE_ID, item.getId());
        cv.put(FavoriteContract.Entry.COLUMN_TITLE, item.getTitle());
        cv.put(FavoriteContract.Entry.COLUMN_BACKDROP, item.getBackdrop_path());
        cv.put(FavoriteContract.Entry.COLUMN_POSTER, item.getPoster_path());
        cv.put(FavoriteContract.Entry.COLUMN_RATING, item.getVote_average());
        cv.put(FavoriteContract.Entry.COLUMN_RELEASE_DATE, item.getRelease_date());
        cv.put(FavoriteContract.Entry.COLUMN_OVERVIEW, item.getOverview());
        resolver.insert(FavoriteContract.Entry.CONTENT_URI, cv);
    }

    private Uri uriWithIDBuilder(long id) {
        return ContentUris.withAppendedId(FavoriteContract.Entry.CONTENT_URI, id);
    }

    private void restartLoader(LoaderManager supportLoaderManager) {
        supportLoaderManager.restartLoader(Constant.LOADER_ID, null, loaderCallbacks);
    }

    private void reviewRecyclerView() {
        mReviewAdapter = new ReviewAdapter(reviewResult);
        mReviewRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mReviewRecyclerView.setHasFixedSize(true);
        mReviewRecyclerView.setAdapter(mReviewAdapter);
        mReviewRecyclerView.setNestedScrollingEnabled(false);
        getReviewFromAPI(String.valueOf(movieResults.getId()));
    }

    private void trailerRecyclerView() {
        mTrailerAdapter = new TrailerAdapter(trailerResult, this);
        mTrailerRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mTrailerRecyclerView.setHasFixedSize(true);
        mTrailerRecyclerView.setAdapter(mTrailerAdapter);
        getTrailerFromAPI(String.valueOf(movieResults.getId()));
    }

    private void getReviewFromAPI(String movieId) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String url = Constant.URL_API + movieId + Constant.REVIEW + Constant.PARAM_API_KEY  + Constant.API_KEY;
        StringRequest stringRequest = new StringRequest(
                Request.Method.GET,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            Reviews reviews = gson.fromJson(response, Reviews.class);
                            for (Review result : reviews.getResults()) {
                                reviewResult.add(result);
                            }
                            mReviewAdapter.notifyDataSetChanged();
                        } catch (Exception e) {
                            Log.e(TAG, "Something error happened!");
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

    private void getTrailerFromAPI(String movieId) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String url = Constant.URL_API + movieId + Constant.VIDEOS + Constant.PARAM_API_KEY  + Constant.API_KEY;
        StringRequest stringRequest = new StringRequest(
                Request.Method.GET,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            Trailers trailers = gson.fromJson(response, Trailers.class);
                            for (Trailer result : trailers.getResults()) {
                                trailerResult.add(result);
                            }
                            mTrailerAdapter.notifyDataSetChanged();
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
