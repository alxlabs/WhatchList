package com.example.trio.whatchlist.adapter.trailer;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.trio.whatchlist.R;
import com.example.trio.whatchlist.model.trailer.Trailer;
import com.example.trio.whatchlist.utilities.TrailerUtil;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by ASUS on 29/08/2017.
 */

public class TrailerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = TrailerAdapter.class.getSimpleName();
    private List<Trailer> list = new ArrayList<>();

    private final TrailerItemClickListener mOnClickListener;

    public TrailerAdapter(List<Trailer> list, TrailerItemClickListener mOnClickListener) {
        this.list = list;
        this.mOnClickListener = mOnClickListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutIdFromListItem = R.layout.row_trailer;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdFromListItem, parent, shouldAttachToParentImmediately);
        TrailerViewHolder viewHolder = new TrailerViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((TrailerViewHolder) holder).bind(list.get(position), position);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public interface TrailerItemClickListener {
        void onTrailerItemClick(Trailer data, int position);
    }

    public class TrailerViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.iv_trailer) ImageView trailerThumbnail;
        @BindView(R.id.tv_trailer_title) TextView trailerTitle;
        @BindView(R.id.tv_trailer_type) TextView trailerType;

        public TrailerViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(final Trailer data, final int position) {
            Picasso.with(itemView.getContext())
                    .load(TrailerUtil.getVideoThumbnailUrl(data.getKey()))
                    .placeholder(R.drawable.ic_local_movies)
                    .error(R.drawable.ic_error)
                    .into(trailerThumbnail);
            trailerTitle.setText(data.getName());
            trailerType.setText(data.getType());
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mOnClickListener.onTrailerItemClick(data, position);
                }
            });
        }
    }
}
