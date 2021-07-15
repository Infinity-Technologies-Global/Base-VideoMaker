package com.ynsuper.slideshowver1.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class StickerModel implements Parcelable {
    @SerializedName("name")
    private String name;
    @SerializedName("imageTapLayout")
    private String imageTapLayout;
    @SerializedName("imageThumb")
    private String imageThumb;
    @SerializedName("zipFile")
    private String urlZip;
    @SerializedName("isPremium")
    private boolean isPremium;
    @SerializedName("count")
    private String countStickers;
    @SerializedName("size")
    private String size;
    @SerializedName("imagePreview")
    private String imagePreview;

    public StickerModel() {
    }

    public StickerModel(String name, String imageTapLayout, String imageThumb, String urlZip, boolean isPremium, String countStickers, String size, String imagePreview) {
        this.name = name;
        this.imageTapLayout = imageTapLayout;
        this.imageThumb = imageThumb;
        this.urlZip = urlZip;
        this.isPremium = isPremium;
        this.countStickers = countStickers;
        this.size = size;
        this.imagePreview = imagePreview;
    }

    protected StickerModel(Parcel in) {
        name = in.readString();
        imageTapLayout = in.readString();
        imageThumb = in.readString();
        urlZip = in.readString();
        isPremium = in.readByte() != 0;
        countStickers = in.readString();
        size = in.readString();
        imagePreview = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(imageTapLayout);
        dest.writeString(imageThumb);
        dest.writeString(urlZip);
        dest.writeByte((byte) (isPremium ? 1 : 0));
        dest.writeString(countStickers);
        dest.writeString(size);
        dest.writeString(imagePreview);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<StickerModel> CREATOR = new Creator<StickerModel>() {
        @Override
        public StickerModel createFromParcel(Parcel in) {
            return new StickerModel(in);
        }

        @Override
        public StickerModel[] newArray(int size) {
            return new StickerModel[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageTapLayout() {
        return imageTapLayout;
    }

    public void setImageTapLayout(String imageTapLayout) {
        this.imageTapLayout = imageTapLayout;
    }

    public String getImageThumb() {
        return imageThumb;
    }

    public void setImageThumb(String imageThumb) {
        this.imageThumb = imageThumb;
    }

    public String getUrlZip() {
        return urlZip;
    }

    public void setUrlZip(String urlZip) {
        this.urlZip = urlZip;
    }

    public boolean isPremium() {
        return isPremium;
    }

    public void setPremium(boolean premium) {
        isPremium = premium;
    }

    public String getCountStickers() {
        return countStickers;
    }

    public void setCountStickers(String countStickers) {
        this.countStickers = countStickers;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getImagePreview() {
        return imagePreview;
    }

    public void setImagePreview(String imagePreview) {
        this.imagePreview = imagePreview;
    }
}
