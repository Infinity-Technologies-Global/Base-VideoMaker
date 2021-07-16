package com.ynsuper.slideshowver1.util;

import android.os.Environment;

import com.ynsuper.slideshowver1.BuildConfig;
import com.ynsuper.slideshowver1.R;
import com.ynsuper.slideshowver1.model.Color;

import java.util.ArrayList;

public interface Constants {

    String URL_BASE_CLOUD_IMAGE_EDIT =
            "https://s3.amazonaws.com/cdn.antuvustudio.com/photo-editor-pro/edit_image";

    String PATH_DOWNLOAD_STICKER_FROM_CLOUD = Environment.getExternalStorageDirectory()
            .toString() + "/Android/Slideshow/" + BuildConfig.APPLICATION_ID + "/Sticker";

    String PATH_DOWNLOAD_MUSIC_FROM_CLOUD = Environment.getExternalStorageDirectory()
            .toString() + "/Android/Slideshow/" + BuildConfig.APPLICATION_ID + "/Musics";

    String PATH_SAVE_FILE_VIDEO = Environment.getExternalStorageDirectory()
            .toString() + "/Android/Slideshow/" + BuildConfig.APPLICATION_ID + "/Videos";

    public static ArrayList<Color> getColorText() {

        ArrayList<Color> colors = new ArrayList<>();
        colors.add(new Color(R.color.colorBackground1));
        colors.add(new Color(R.color.colorBackground2));
        colors.add(new Color(R.color.colorBackground3));
        colors.add(new Color(R.color.colorBackground4));
        colors.add(new Color(R.color.colorBackground5));
        colors.add(new Color(R.color.colorBackground6));
        colors.add(new Color(R.color.colorBackground7));
        colors.add(new Color(R.color.colorBackground8));
        colors.add(new Color(R.color.colorBackground9));
        colors.add(new Color(R.color.colorBackground10));
        colors.add(new Color(R.color.colorBackground11));
        colors.add(new Color(R.color.colorBackground12));
        colors.add(new Color(R.color.colorBackground13));
        colors.add(new Color(R.color.colorBackground14));
        colors.add(new Color(R.color.colorBackground15));
        colors.add(new Color(R.color.colorBackground16));
        colors.add(new Color(R.color.colorBackground17));
        colors.add(new Color(R.color.colorBackground18));
        colors.add(new Color(R.color.colorBackground19));
        colors.add(new Color(R.color.colorBackground20));
        colors.add(new Color(R.color.colorBackground21));
        colors.add(new Color(R.color.colorBackground22));
        colors.add(new Color(R.color.colorBackground23));
        colors.add(new Color(R.color.colorBackground24));
        colors.add(new Color(R.color.colorBackground25));
        colors.add(new Color(R.color.colorBackground26));
        colors.add(new Color(R.color.colorBackground27));
        colors.add(new Color(R.color.colorBackground28));
        colors.add(new Color(R.color.colorBackground29));
        colors.add(new Color(R.color.colorBackground30));
        colors.add(new Color(R.color.colorBackground31));
        colors.add(new Color(R.color.colorBackground32));
        colors.add(new Color(R.color.colorBackground33));
        colors.add(new Color(R.color.colorBackground34));
        colors.add(new Color(R.color.colorBackground35));
        colors.add(new Color(R.color.colorBackground36));
        colors.add(new Color(R.color.colorBackground37));
        colors.add(new Color(R.color.colorBackground38));
        colors.add(new Color(R.color.colorBackground39));
        colors.add(new Color(R.color.colorBackground40));
        colors.add(new Color(R.color.colorBackground41));
        colors.add(new Color(R.color.colorBackground42));
        colors.add(new Color(R.color.colorBackground43));
        colors.add(new Color(R.color.colorBackground44));
        colors.add(new Color(R.color.colorBackground45));
        colors.add(new Color(R.color.colorBackground46));
        colors.add(new Color(R.color.colorBackground47));
        colors.add(new Color(R.color.colorBackground48));
        colors.add(new Color(R.color.colorBackground49));
        colors.add(new Color(R.color.colorBackground50));
        return colors;
    }
}