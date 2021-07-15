package com.ynsuper.slideshowver1.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose

import com.google.gson.annotations.SerializedName


class AlbumMusicModel() : Parcelable {
    @SerializedName("version")
    @Expose
    private var version: Int? = null

    @SerializedName("data")
    @Expose
    private var data: List<ItemAlbumMusicModel?>? = null

    constructor(parcel: Parcel) : this() {
        version = parcel.readValue(Int::class.java.classLoader) as? Int
    }

    fun getVersion(): Int? {
        return version
    }

    fun setVersion(version: Int?) {
        this.version = version
    }

    fun getData(): List<ItemAlbumMusicModel?>? {
        return data
    }

    fun setData(data: List<ItemAlbumMusicModel?>?) {
        this.data = data
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(version)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AlbumMusicModel> {
        override fun createFromParcel(parcel: Parcel): AlbumMusicModel {
            return AlbumMusicModel(parcel)
        }

        override fun newArray(size: Int): Array<AlbumMusicModel?> {
            return arrayOfNulls(size)
        }
    }

}