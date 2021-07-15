package com.ynsuper.slideshowver1.view.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;


import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.ynsuper.slideshowver1.R;
import com.ynsuper.slideshowver1.callback.IUnzipFile;
import com.ynsuper.slideshowver1.model.ListSticker;
import com.ynsuper.slideshowver1.model.StickerModel;
import com.ynsuper.slideshowver1.util.Constants;
import com.ynsuper.slideshowver1.view.custom_view.dialog.DownloadStickerFromUrl;
import com.ynsuper.slideshowver1.view.custom_view.dialog.UnlockItemDialogFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class StickerPagerAdapter extends PagerAdapter {
    private final int width;
    private final StickerAdapter.OnClickStickerListener stickerListener;
    private final IUnzipFile unZipCallBack;
    private Context context;
    private ListSticker arrListSticker = new ListSticker();
    private ImageView imageThumb;
    private Button buttonDownLoad;
    private ConstraintLayout relativeDownload;
    private RecyclerView recyclerView;
    private StickerAdapter stickerAdapter;
    private int currentPosition;

    public StickerPagerAdapter(Context context,
                               ListSticker listSticker,
                               int width,
                               StickerAdapter.OnClickStickerListener stickerListener,
                               IUnzipFile iUnzipFile) {
        this.context = context;
        this.arrListSticker = listSticker;
        this.width = width;
        this.stickerListener = stickerListener;
        this.unZipCallBack = iUnzipFile;
    }


    @Override
    public int getCount() {
        if (arrListSticker != null && arrListSticker.getArraySticker() != null && arrListSticker.getArraySticker().size() > 0) {
            return arrListSticker.getArraySticker().size();
        }
        return 0;
    }

    public ListSticker getArrListSticker() {
        return arrListSticker;
    }

    public void setArrListSticker(ListSticker arrListSticker) {
        this.arrListSticker = arrListSticker;
        notifyDataSetChanged();
    }

    @Override
    public boolean isViewFromObject(@NonNull final View view, @NonNull final Object object) {
        return view.equals(object);
    }

    @Override
    public void destroyItem(@NonNull final View container, final int position, @NonNull final Object object) {
        ((ViewPager) container).removeView((View) object);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull final ViewGroup container, final int position) {
        final View view = LayoutInflater.from(
                context).inflate(R.layout.sticker_items, null, false);


        initView(view, position);
        container.addView(view);
        return view;
    }

    private void initView(View view, int position) {

        relativeDownload = view.findViewById(R.id.relative_sticker);
        imageThumb = view.findViewById(R.id.image_sticker);
        buttonDownLoad = view.findViewById(R.id.button_download_sticker);
        recyclerView = view.findViewById(R.id.rv);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(context, 4));

        List<String> arrSticker = checkListStickerLocal(position);
        if (arrSticker.size() <= 0) {
            Glide.with(context).load(Constants.URL_BASE_CLOUD_IMAGE_EDIT + "/"
                    + arrListSticker.getArraySticker().get(position).getImagePreview())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.ic_close_black_24dp)
//                    .centerCrop()
                    .into(imageThumb);

            relativeDownload.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            relativeDownload.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
        stickerAdapter = new StickerAdapter(context, arrSticker, width, stickerListener);
        recyclerView.setAdapter(stickerAdapter);
        if (arrListSticker.getArraySticker().get(position).isPremium()) {
            buttonDownLoad.setText(R.string.unlock);
            buttonDownLoad.setTextColor(Color.WHITE);
            buttonDownLoad.setBackground(context.getDrawable(R.drawable.btn_unlock_sticker));
            buttonDownLoad.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_unlock_24,0,0,0);
        } else {
            buttonDownLoad.setText(R.string.text_download);
            buttonDownLoad.setTextColor(Color.WHITE);
            buttonDownLoad.setBackground(context.getDrawable(R.drawable.bg_btn_purchase));
            buttonDownLoad.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_download_24,0,0,0);

        }
        currentPosition = position;
        buttonDownLoad.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (arrListSticker.getArraySticker().get(position).isPremium()) {
                    StickerModel stickerModel = arrListSticker.getArraySticker().get(position);
                    UnlockItemDialogFragment.newInstance(
                            context,
                            null,
                            stickerModel,
                            stickerModel.getImageThumb()
                    )
                            .show(((AppCompatActivity)context).getSupportFragmentManager(),"");

                } else {

                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
//                        FileUtils.downloadZipFile(context,arrListSticker.getArraySticker().get(position).getUrlZip());
                            new DownloadStickerFromUrl(context, unZipCallBack).execute(Constants.URL_BASE_CLOUD_IMAGE_EDIT +
                                    arrListSticker.getArraySticker().get(position).getUrlZip()

                            );
                        }
                    });
                }


            }
        });
    }

    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    private List<String> checkListStickerLocal(int position) {
        currentPosition = position;
        List<String> arrListImageFile = new ArrayList<>();
        if (arrListSticker != null && arrListSticker.getArraySticker() != null) {
            try {
                String path = Constants.PATH_DOWNLOAD_STICKER_FROM_CLOUD + "/" + arrListSticker.getArraySticker().get(position).getName();
                File directory = new File(path);
                File[] files = directory.listFiles();
                for (int i = 0; i < files.length; i++) {
                    arrListImageFile.add(files[i].getAbsolutePath());
                }
                return arrListImageFile;
            } catch (Exception e) {

            }

        }
        return arrListImageFile;
    }

    public void loadPackageDownloaded(int currentIndicatorPosition) {
        List<String> arrSticker = checkListStickerLocal(currentIndicatorPosition);
        if (arrSticker.size() <= 0) {
            Glide.with(context).load(Constants.URL_BASE_CLOUD_IMAGE_EDIT + "/" +
                    arrListSticker.getArraySticker().get(currentIndicatorPosition).getImageThumb())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.ic_download_24)
                    .into(imageThumb);
            relativeDownload.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            relativeDownload.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

}
