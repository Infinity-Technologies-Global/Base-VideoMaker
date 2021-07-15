package com.ynsuper.slideshowver1.view.custom_view.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.ynsuper.slideshowver1.R;
import com.ynsuper.slideshowver1.ads.AdConfig;
import com.ynsuper.slideshowver1.ads.RewardedAdLoader;
import com.ynsuper.slideshowver1.callback.IUnzipFile;
import com.ynsuper.slideshowver1.model.StickerModel;
import com.ynsuper.slideshowver1.util.Constants;

import org.jetbrains.annotations.NotNull;

import timber.log.Timber;


public class UnlockItemDialogFragment extends BottomSheetDialogFragment {

    private final String urlImageThumb;
    private final Context mContext;
    private final Fragment mFragment;
    private ImageView imageThumb;
    private Button buttonUnlock;
    private StickerModel mSticker;
    private RewardedAdLoader rewardedAdLoader;

    public UnlockItemDialogFragment(Context context,
                                    Fragment fragment,
                                    StickerModel sticker,
                                    String imageThumb) {
        this.urlImageThumb = imageThumb;
        this.mSticker = sticker;
        this.mContext = context;
        this.mFragment = fragment;
        rewardedAdLoader = new RewardedAdLoader();
    }

    public static UnlockItemDialogFragment newInstance(Context context,
                                                       Fragment fragment,
                                                       StickerModel sticker,
                                                       String imageThumb) {
        return new UnlockItemDialogFragment(context, fragment, sticker, imageThumb);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_unlock_bottom_sheet, container,
                false);
        setStyle(STYLE_NORMAL, R.style.AppBottomSheetDialogTheme);

        initView(view);

        return view;

    }

    private void initView(View view) {
        imageThumb = view.findViewById(R.id.image_preview_sticker);
        Glide.with(getContext()).load(Constants.URL_BASE_CLOUD_IMAGE_EDIT + "/" + urlImageThumb)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.ic_download_24)
//                    .centerCrop()
                .into(imageThumb);
        buttonUnlock = view.findViewById(R.id.button_download_sticker);
        buttonUnlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(getContext(), "Show QC tai day", Toast.LENGTH_SHORT).show();

                rewardedAdLoader.setAdsId(getContext(), AdConfig.AD_ADMOB_STORE_ASSETS_REWARDED);
                rewardedAdLoader.loadRewardedAd(eventRewardedAdCallback);
            }
        });
    }

    private RewardedAdLoader.EventRewardedAdCallback eventRewardedAdCallback = new RewardedAdLoader.EventRewardedAdCallback() {
        @Override
        public void onAdClosed() {

        }

        @Override
        public void onUserEarnedReward(@NotNull RewardItem reward) {
            Timber.d("onUserEarnedReward:%s", reward.getAmount());

            new DownloadStickerFromUrl(getActivity(), new IUnzipFile() {
                @Override
                public void unZipSuccess(String zipFile) {
                    Toast.makeText(mContext, R.string.text_download_success, Toast.LENGTH_SHORT).show();
                }
            }).execute(Constants.URL_BASE_CLOUD_IMAGE_EDIT +
                    mSticker.getUrlZip()

            );
        }
    };

}