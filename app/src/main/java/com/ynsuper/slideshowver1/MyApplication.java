package com.ynsuper.slideshowver1;

import android.app.Application;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.wysaid.nativePort.CGENativeLibrary;

import java.io.IOException;
import java.io.InputStream;

import static com.ynsuper.slideshowver1.util.FilterUtils.getBitmapFromPath;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        CGENativeLibrary.setLoadImageCallback(mLoadImageCallback, null);

    }
    public CGENativeLibrary.LoadImageCallback mLoadImageCallback = new CGENativeLibrary.LoadImageCallback() {

        //Notice: the 'name' passed in is just what you write in the rule, e.g: 1.jpg
        //注意， 这里回传的name不包含任何路径名， 仅为具体的图片文件名如 1.jpg
        @Override
        public Bitmap loadImage(String name, Object arg) {
            if (name.contains(BuildConfig.APPLICATION_ID)) {
                return getBitmapFromPath(name);
            } else {
                AssetManager am = getAssets();
                InputStream is;
                try {
                    is = am.open(name);
                } catch (IOException e) {
                    return null;
                }

                return BitmapFactory.decodeStream(is);
            }

        }

        @Override
        public void loadImageOK(Bitmap bmp, Object arg) {
            //The bitmap is which you returned at 'loadImage'.
            //You can call recycle when this function is called, or just keep it for further usage.
            //唯一不需要马上recycle的应用场景为 多个不同的滤镜都使用到相同的bitmap
            //那么可以选择缓存起来。
            bmp.recycle();
        }
    };
}
