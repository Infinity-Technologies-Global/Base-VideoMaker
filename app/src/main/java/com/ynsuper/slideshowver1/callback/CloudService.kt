package com.ynsuper.slideshowver1.callback

import com.ynsuper.slideshowver1.model.AlbumMusicModel
import com.ynsuper.slideshowver1.model.ListSticker
import com.ynsuper.slideshowver1.model.MusicModel
import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Streaming
import retrofit2.http.Url


interface CloudService {
    @GET("/next-slideshow/music.json")
    fun getListCategoryMusic(): Observable<AlbumMusicModel>

    @GET("/next-slideshow/{path}")
    fun getListMusic(@Path("path")  path: String): Observable<List<MusicModel>>

    @GET("/cdn.taptapstudio.online/photo-editor-pro/edit_image/json/sticker2.json")
    fun getListStickerModel(): Observable<ListSticker>

    @GET
    @Streaming
    fun downloadMP3(@Url url: String): Observable<Response<ResponseBody>>

}