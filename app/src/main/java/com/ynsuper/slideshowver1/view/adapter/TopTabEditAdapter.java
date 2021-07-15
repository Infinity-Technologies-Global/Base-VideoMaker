package com.ynsuper.slideshowver1.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.ynsuper.slideshowver1.R;
import com.ynsuper.slideshowver1.model.ListSticker;
import com.ynsuper.slideshowver1.util.Constants;

public class TopTabEditAdapter extends RecyclerTabLayout.Adapter<TopTabEditAdapter.ViewHolder> {

    private ListSticker arrListSticker;
    private PagerAdapter mAdapater;
    private Context context;

    public TopTabEditAdapter(ViewPager viewPager, Context context, ListSticker arrListSticker) {
        super(viewPager);
        this.context = context;
        mAdapater = mViewPager.getAdapter();
        this.arrListSticker = arrListSticker;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.top_tab_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (arrListSticker != null && arrListSticker.getArraySticker() != null) {
            Glide.with(context).load(
                    Constants.URL_BASE_CLOUD_IMAGE_EDIT + "/" + arrListSticker.getArraySticker().get(position).getImageTapLayout())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.imageView);
        }

        holder.imageView.setSelected(position == getCurrentIndicatorPosition());
    }

    @Override
    public int getItemCount() {
        return mAdapater.getCount();
    }

    public void setArrListSticker(ListSticker arrListSticker) {

        this.arrListSticker = arrListSticker;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getViewPager().setCurrentItem(getAdapterPosition());
                }
            });
        }
    }

}
