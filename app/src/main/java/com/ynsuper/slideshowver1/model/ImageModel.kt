package com.ynsuper.slideshowver1.model

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable

class ImageModel() : Parcelable{
      var uriImage : Uri = Uri.EMPTY

    constructor(parcel: Parcel) : this() {
        uriImage = parcel.readParcelable(Uri::class.java.classLoader)!!
    }

    constructor(uri: Uri) : this() {
        uriImage = uri
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(uriImage, flags)
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