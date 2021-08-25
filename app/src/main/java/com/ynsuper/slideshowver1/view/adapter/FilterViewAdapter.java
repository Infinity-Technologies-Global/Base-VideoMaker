package com.ynsuper.slideshowver1.view.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.github.siyamed.shapeimageview.RoundedImageView;
import com.ynsuper.slideshowver1.R;
import com.ynsuper.slideshowver1.callback.FilterListener;
import com.ynsuper.slideshowver1.model.ListFilterModel;
import com.ynsuper.slideshowver1.util.Constants;
import com.ynsuper.slideshowver1.util.FilterUtils;
import com.ynsuper.slideshowver1.util.SystemUtil;

import java.util.ArrayList;
import java.util.List;

public class FilterViewAdapter extends RecyclerView.Adapter<FilterViewAdapter.ViewHolder> {

    private FilterListener mFilterListener;
    private List<FilterUtils.FilterBean> filterEffects;
    private ArrayList<ListFilterModel> filterEffect;
    private List<Bitmap> bitmaps;
    private Context context;
    private int selectedFilterIndex = 0;
    private int borderWidth;
    private boolean isPremium;

    public FilterViewAdapter(List<Bitmap> bitmaps, FilterListener filterListener, Context context, List<FilterUtils.FilterBean> filterEffects) {
        mFilterListener = filterListener;
        this.bitmaps = bitmaps;
        this.context = context;
        this.filterEffects = filterEffects;
        borderWidth = SystemUtil.dpToPx(context, 3);
    }

    public FilterViewAdapter(List<Bitmap> bitmaps, FilterListener filterListener, Context context, ArrayList<ListFilterModel> filterEffects, boolean isPackagePremium) {
        mFilterListener = filterListener;
        this.bitmaps = bitmaps;
        this.context = context;
        this.filterEffect = filterEffects;
        this.isPremium = isPackagePremium;

        borderWidth = SystemUtil.dpToPx(context, 3);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_filter_view, parent, false);
        return new ViewHolder(view);
    }

    public void reset() {
        selectedFilterIndex = 0;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.setIsRecyclable(false);
        //check Overlay can't show back
        if (position == 0 && filterEffect != null) {
            holder.mImageBack.setVisibility(View.VISIBLE);
        } else {
            holder.mImageFilterView.setImageBitmap(bitmaps.get(position));
            holder.mImageFilterView.setBorderColor(context.getResources().getColor(R.color.colorAccent));
        }

        // check Name overlay or filter
        if (filterEffect != null) {
            holder.mTxtFilterName.setText(filterEffect.get(position).getNameFilter());
        } else {
            holder.mTxtFilterName.setText(filterEffects.get(position).getName());

        }
        //set image for filter
        if (position != 0) {
            holder.mImageFilterView.setImageBitmap(bitmaps.get(position));
            holder.mImageFilterView.setBorderColor(context.getResources().getColor(R.color.colorAccent));
        }
        if (isPremium && position != 0) {
            holder.textPro.setVisibility(View.VISIBLE);
        }
        if (selectedFilterIndex == position) {
            holder.mImageFilterView.setBorderColor(context.getResources().getColor(R.color.colorAccent));
            holder.mImageFilterView.setBorderWidth(borderWidth);
        } else {
            holder.mImageFilterView.setBorderColor(Color.TRANSPARENT);
            holder.mImageFilterView.setBorderWidth(borderWidth);
        }
    }

    @Override
    public int getItemCount() {
        return bitmaps.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        RoundedImageView mImageFilterView;
        RoundedImageView mImageBack;
        TextView mTxtFilterName;
        TextView textPro;
        ConstraintLayout wrapFilterItem;

        ViewHolder(View itemView) {
            super(itemView);
            mImageFilterView = itemView.findViewById(R.id.imgFilterView);
            mTxtFilterName = itemView.findViewById(R.id.txtFilterName);
            wrapFilterItem = itemView.findViewById(R.id.wrapFilterItem);
            textPro = itemView.findViewById(R.id.text_pro);
            mImageBack = itemView.findViewById(R.id.image_back_filter);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedFilterIndex = getLayoutPosition();
                    mFilterListener.onFilterSelected(selectedFilterIndex);

                    notifyDataSetChanged();
                }
            });

        }
    }

}
