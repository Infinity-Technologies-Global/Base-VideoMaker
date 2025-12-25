package com.ynsuper.slideshowver1.ads

import android.content.Context
import android.util.Log
import com.facebook.ads.AdSettings
import com.facebook.ads.AudienceNetworkAds
import com.facebook.ads.BuildConfig


class AudienceNetworkInitializeHelper : AudienceNetworkAds.InitListener {

    override fun onInitialized(result: AudienceNetworkAds.InitResult) {
        Log.d("Ynsuper", result.message)
    }

    companion object {

        /**
         * It's recommended to call this method from Application.onCreate().
         * Otherwise you can call it from all Activity.onCreate()
         * methods for Activities that contain ads.
         *
         * @param context Application or Activity.
         */
        internal fun initialize(context: Context) {
            if (!AudienceNetworkAds.isInitialized(context)) {
                if (BuildConfig.DEBUG) {
                    AdSettings.turnOnSDKDebugger(context)
//                    AdSettings.addTestDevice("eae660ba-4730-4641-92b2-ea5558eb5663");
//                    AdSettings.addTestDevice("b0bf6730-4d26-4efd-80f3-028e6bef4e0d");
//                    AdSettings.addTestDevice("3f6a811f-180b-4e3b-ac73-59ee497c1130");
//                    AdSettings.addTestDevice("551be576-7cfa-4307-923e-dbc0e1a9e2b0");
                }
                AdSettings.addTestDevices(AdConfig.DEVICES_FAN)

                AdSettings.setVideoAutoplay(false)
                AdSettings.setVideoAutoplayOnMobile(false)
                AudienceNetworkAds
                    .buildInitSettings(context)
                    .withInitListener(AudienceNetworkInitializeHelper())
                    .initialize()

            }
        }
    }
}