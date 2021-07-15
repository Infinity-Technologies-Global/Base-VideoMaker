package com.ynsuper.slideshowver1.model

import com.google.gson.annotations.Expose

import com.google.gson.annotations.SerializedName


class MusicModel {
    @SerializedName("id")
    @Expose
    var id: Int? = null

    @SerializedName("name")
    @Expose
    var name: String? = null

    @SerializedName("artist")
    @Expose
    var artist: String? = null

    @SerializedName("tempo")
    @Expose
    var tempo: String? = null

    @SerializedName("license")
    @Expose
    var license: String? = null

    @SerializedName("mood")
    @Expose
    var mood: String? = null

    @SerializedName("priority")
    @Expose
    var priority: Any? = null

    @SerializedName("content")
    @Expose
    var content: String? = null

    @SerializedName("audio")
    @Expose
    var audio: String? = null

}