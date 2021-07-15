package com.ynsuper.slideshowver1.util

import android.content.Context
import android.content.res.Resources
import android.net.ConnectivityManager
import android.util.Log
import androidx.core.content.ContextCompat.getSystemService
import java.net.InetAddress


object Util {
    val Int.toPx: Int
        get() = (this * Resources.getSystem().displayMetrics.density).toInt()

    val Int.toDp: Int
        get() = (this / Resources.getSystem().displayMetrics.density).toInt()

     fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        val activeNetworkInfo = connectivityManager!!.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }
}