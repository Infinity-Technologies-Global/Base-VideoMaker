package com.ynsuper.slideshowver1.model

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable

class ImageModel() : Parcelable {
    var uriImage: Uri = Uri.EMPTY
    var isVideo: Boolean = false

    constructor(parcel: Parcel) : this() {
        uriImage = parcel.readParcelable(Uri::class.java.classLoader)!!
        isVideo = parcel.readByte().toInt() != 0
    }

    constructor(uri: Uri, isVideo: Boolean = false) : this() {
        this.uriImage = uri
        this.isVideo = isVideo
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(uriImage, flags)
        parcel.writeByte(if (isVideo) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ImageModel> {
        override fun createFromParcel(parcel: Parcel): ImageModel {
            return ImageModel(parcel)
        }

        override fun newArray(size: Int): Array<ImageModel?> {
            return arrayOfNulls(size)
        }
    }
}