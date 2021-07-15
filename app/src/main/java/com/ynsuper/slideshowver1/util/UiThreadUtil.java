package com.ynsuper.slideshowver1.util;

import android.os.Handler;
import android.os.Looper;


public class UiThreadUtil {
    private static Handler mUiHandler = new Handler(Looper.getMainLooper());

    private static Object mToken = new Object();

    public final static boolean post(Runnable r) {
        if (mUiHandler == null) {
            return false;
        }
        return mUiHandler.post(r);
    }

    public final static boolean postDelayed(Runnable r, long delayMillis) {
        if (mUiHandler == null) {
            return false;
        }
        return mUiHandler.postDelayed(r, delayMillis);
    }

    public final static Handler getUiHandler() {
        return mUiHandler;
    }

    public final static boolean postOnceDelayed(Runnable r, long delayMillis) {
        if (mUiHandler == null) {
            return false;
        }
        mUiHandler.removeCallbacks(r, mToken);
        return mUiHandler.postDelayed(r, delayMillis);
    }

    public static void removeCallbacks(Runnable runnable) {
        if (mUiHandler == null) {
            return;
        }
        mUiHandler.removeCallbacks(runnable);
    }
}
