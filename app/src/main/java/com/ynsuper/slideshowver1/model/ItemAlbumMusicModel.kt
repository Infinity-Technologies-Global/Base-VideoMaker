package com.ynsuper.slideshowver1.model

import com.google.gson.annotations.Expose

import com.google.gson.annotations.SerializedName


class ItemAlbumMusicModel {
    @SerializedName("id")
    @Expose
    var id: Int? = null

    @SerializedName("title")
    @Expose
    var title: String? = null

    @SerializedName("sounds_count")
    @Expose
    var soundsCount: Int? = null

    @SerializedName("cover")
    @Expose
    var cover: String? = null

    @SerializedName("path")
    @Expose
    var path: String? = null

}