package com.ynsuper.slideshowver1.adapter;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;


import com.ynsuper.slideshowver1.R;
import com.ynsuper.slideshowver1.model.Color;

import java.util.ArrayList;


public class ColorTextAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<Color> colors;
    private Context context;
    private LayoutInflater inflater;
    private OnItemClickListener listener;
    public int selectedPosition = 0;

    public ColorTextAdapter(ArrayList<Color> colors, Context context, OnItemClickListener listener) {
        this.colors = colors;
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.listener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_color_text_shadow, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        final Color color = colors.get(position);
        final MyViewHolder myViewHolder = (MyViewHolder) holder;
        if (color != null) {
//            int padding = (Resources.getSystem().getDisplayMetrics().widthPixels / 6) / 6;
            if (selectedPosition == position) {
//                myViewHolder.btnColor.setBackgroundColor(context.getResources().getColor(R.color.colorItemTemplate));
                myViewHolder.imgBg.setVisibility(View.VISIBLE);
                myViewHolder.imageCheck.setVisibility(View.VISIBLE);
            } else {
                myViewHolder.imgBg.setVisibility(View.GONE);
                myViewHolder.imageCheck.setVisibility(View.GONE);
            }
//            myViewHolder.imgColor.getLayoutParams().width = Resources.getSystem().getDisplayMetrics().widthPixels / 6;
//            myViewHolder.imgColor.getLayoutParams().height = Resources.getSystem().getDisplayMetrics().widthPixels / 6;
//            myViewHolder.imgBg.getLayoutParams().width = Resources.getSystem().getDisplayMetrics().widthPixels / 8;
//            myViewHolder.imgBg.getLayoutParams().height = Resources.getSystem().getDisplayMetrics().widthPixels / 8;
//            myViewHolder.imgColor.setPadding(padding, padding, padding, padding);
            GradientDrawable gradientDrawableChecked = (GradientDrawable) myViewHolder.imgBg.getDrawable();
            gradientDrawableChecked.setStroke(3, context.getResources().getColor(color.getIdColor()));
            myViewHolder.imgColor.setColorFilter(context.getResources().getColor(color.getIdColor()), PorterDuff.Mode.SRC);
            myViewHolder.imgColor.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onItemCheck(view, position);
                    selectedPosition = position;
                    notifyDataSetChanged();
                }
            });

        }

    }

    public void resetSelected() {
        selectedPosition = -1;
        this.notifyDataSetChanged();
    }


    @Override
    public int getItemCount() {
        return colors.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private ImageView imgColor;
        private ImageView imgBg;
        private ImageView imageCheck;

        @SuppressLint("WrongViewCast")
        public MyViewHolder(View itemView) {
            super(itemView);
            imgColor = itemView.findViewById(R.id.img_color_text);
            imgBg = itemView.findViewById(R.id.imgBackground);
            imageCheck = itemView.findViewById(R.id.imgTickColor);
        }
    }

    public interface OnItemClickListener {
        void onItemCheck(View view, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

}
