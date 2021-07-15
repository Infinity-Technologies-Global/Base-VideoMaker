package com.ynsuper.slideshowver1.remoteconfig

import com.ynsuper.slideshowver1.BuildConfig
object RemoteConfigApp : FirebaseRemoteEasy(devMode = BuildConfig.DEBUG) {

    init {

    }

    val show_native_facebook: Boolean by boolean(false)
    val show_intersitial_facebook: Boolean by boolean(false)
    val is_show_snow_fall: Boolean by boolean(true)

    val is_show_splash_facebook :Boolean by boolean(false)


}