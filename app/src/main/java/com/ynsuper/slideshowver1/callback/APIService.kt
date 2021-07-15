package com.ynsuper.slideshowver1.callback

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

object APIService {
    //    const val URL = "https://static.taptapstudio.online"
    const val URL2 = "https://ap-south-1.linodeobjects.com"

    val service: CloudService by lazy {
        val logging = HttpLoggingInterceptor()
        val gson = GsonBuilder().serializeNulls().setPrettyPrinting().create()
        val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .build()
        Retrofit.Builder()
                .baseUrl(URL2)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create()) //(*)
                .client(client)
                .build().create<CloudService>(CloudService::class.java)
    }

}
