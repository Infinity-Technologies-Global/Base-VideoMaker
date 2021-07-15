package com.ynsuper.slideshowver1.util;

import android.os.Environment;

import com.ynsuper.slideshowver1.BuildConfig;

public interface Constants {

    String URL_BASE_CLOUD_IMAGE_EDIT =
            "https://s3.amazonaws.com/cdn.antuvustudio.com/photo-editor-pro/edit_image";

    String PATH_DOWNLOAD_STICKER_FROM_CLOUD = Environment.getExternalStorageDirectory()
            .toString() + "/Android/Slideshow/" + BuildConfig.APPLICATION_ID + "/Sticker";

    String PATH_DOWNLOAD_MUSIC_FROM_CLOUD = Environment.getExternalStorageDirectory()
            .toString() + "/Android/Slideshow/" + BuildConfig.APPLICATION_ID + "/Musics";

    String PATH_SAVE_FILE_VIDEO = Environment.getExternalStorageDirectory()
            .toString() + "/Android/Slideshow/" + BuildConfig.APPLICATION_ID + "/Videos";
}