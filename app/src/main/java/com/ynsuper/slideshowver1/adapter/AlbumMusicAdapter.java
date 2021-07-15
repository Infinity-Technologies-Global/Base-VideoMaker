package com.ynsuper.slideshowver1.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ynsuper.slideshowver1.R;
import com.ynsuper.slideshowver1.model.AlbumMusicModel;

public class AlbumMusicAdapter extends RecyclerView.Adapter<AlbumMusicAdapter.MyViewHolder> {
    private final Context context;
    private final AlbumMusicModel listMusic;
    private final OnAlbumMusicClickListener onAlbumMusicClickListener;

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_album_music, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.txtAlbum.setText(listMusic.getData().get(position).getTitle());
        holder.txtAlbumSize.setText(listMusic.getData().get(position).getSoundsCount() + " tracks");
        Glide.with(context).load(listMusic.getData().get(position).getCover()).into(holder.imgAlbum);
        holder.imgAlbum.setClipToOutline(true);
    }

    @Override
    public int getItemCount() {
        return listMusic.getData().size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout btnAlbum;
        ImageView imgAlbum;
        TextView txtAlbum, txtAlbumSize;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAlbum = itemView.findViewById(R.id.imgItemMusic);
            txtAlbum = itemView.findViewById(R.id.txtMusicType);
            txtAlbumSize= itemView.findViewById(R.id.txtSize);
            btnAlbum = itemView.findViewById(R.id.btnAlbum);
            btnAlbum.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onAlbumMusicClickListener != null) {
                        onAlbumMusicClickListener.onAlbumClick(listMusic.getData().get(getAdapterPosition()).getPath());
                    }
                }
            });
        }
    }

    public AlbumMusicAdapter(AlbumMusicModel listMusic, Context context, OnAlbumMusicClickListener onAlbumMusicClickListener) {
        this.context = context;
        this.listMusic = listMusic;
        this.onAlbumMusicClickListener = onAlbumMusicClickListener;
    }

    public interface OnAlbumMusicClickListener {

        void onAlbumClick(String path);
    }
}
