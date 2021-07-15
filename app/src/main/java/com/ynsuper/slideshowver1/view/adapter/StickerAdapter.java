package com.ynsuper.slideshowver1.view.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ynsuper.slideshowver1.R;
import com.ynsuper.slideshowver1.view.SlideShowActivity;

import java.io.File;
import java.util.List;


public class StickerAdapter extends RecyclerView.Adapter<StickerAdapter.ViewHolder> {

    private Context context;
    private List<String> stickers;
    private int screenWidth;
    private OnClickStickerListener stickerListener;

    public StickerAdapter(Context context, List<String> stickers, int screenWidth, OnClickStickerListener stickerListener) {
        this.context = context;
        this.stickers = stickers;
        this.screenWidth = screenWidth;
        this.stickerListener = stickerListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        final View view = LayoutInflater.from(context).inflate(R.layout.sticker_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        try {
//            Glide.with(context).load(AssetUtils.loadBitmapFromAssets(context, stickers.get(position))).into(holder.sticker);
            Glide.with(context).load(stickers.get(position)).into(holder.sticker);

        } catch (Exception e) {

        }
    }

    @Override
    public int getItemCount() {
        return stickers.size();
    }

    public void setArraySticker(List<String> checkListStickerLocal) {
        this.stickers = checkListStickerLocal;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        RelativeLayout stickerWrapper;
        public ImageView sticker;

        public ViewHolder(final View itemView) {
            super(itemView);
            sticker = itemView.findViewById(R.id.txt_vp_item_list);
//            stickerWrapper = itemView.findViewById(R.id.sticker_wrapper);
//            ViewGroup.LayoutParams layoutParams = stickerWrapper.getLayoutParams();
//            layoutParams.width = screenWidth / 3;
//            layoutParams.height = screenWidth / 3;
//            stickerWrapper.setLayoutParams(layoutParams);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int selectedItem = getAdapterPosition();
            File image = new File(stickers.get(selectedItem));
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(),bmOptions);
            stickerListener.addSticker(bitmap);

            }
    }

    public interface OnClickStickerListener {
        void addSticker(Bitmap bitmap);
    }
}
