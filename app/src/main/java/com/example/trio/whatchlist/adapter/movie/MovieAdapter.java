package com.example.trio.whatchlist.adapter.movie;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.trio.whatchlist.R;
import com.example.trio.whatchlist.model.movie.Movie;
import com.example.trio.whatchlist.utilities.ImageUrlBuilder;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by ASUS on 27/08/2017.
 */

public class MovieAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = MovieAdapter.class.getSimpleName();

    private List<Movie> list = new ArrayList<>();
    private final ItemClickListener mOnClickListener;

    public MovieAdapter(List<Movie> list, ItemClickListener mOnClickListener) {
        this.list = list;
        this.mOnClickListener = mOnClickListener;
    }

    public void replaceAll(List<Movie> list) {
        this.list.clear();
        this.list = list;
        notifyDataSetChanged();
    }

    public void updateData(List<Movie> list){
        this.list.addAll(list);
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutId = R.layout.movie_list_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttach = false;

        View view = inflater.inflate(layoutId, parent, shouldAttach);
        MovieViewHolder viewHolder = new MovieViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((MovieViewHolder) holder).bind(list.get(position), position);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public interface ItemClickListener {
        void onItemClick(Movie data, int position);
    }

    public class MovieViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.iv_poster_path) ImageView poster_path;
        @BindView(R.id.tv_movie_rate) TextView tv_movie_rate;
        public MovieViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }

        public void bind(final Movie data, final int position){
            Picasso.with(itemView.getContext())
                    .load(ImageUrlBuilder.getPosterUrl(data.getPoster_path()))
                    .placeholder(R.drawable.ic_local_movies)
                    .error(R.drawable.ic_error)
                    .into(poster_path);
            tv_movie_rate.setText(String.valueOf(data.getVote_average()));
            if(data.getVote_average()<6.5)
                tv_movie_rate.setBackgroundColor(itemView.getResources().getColor(R.color.colorSecondaryText));
            else
                tv_movie_rate.setBackgroundColor(itemView.getResources().getColor(R.color.colorAccent));
            itemView.setOnClickListener(
                    new View.OnClickListener(){
                        @Override
                        public void onClick(View view) {
                            mOnClickListener.onItemClick(data, position);
                        }
                    }
            );
        }
    }
}
