package com.ynsuper.slideshowver1.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class ListFilterModel implements Parcelable {
    @SerializedName("nameFilter")
    private String nameFilter;
    @SerializedName("textFilter")
    private String textFilter;
    @SerializedName("isFile")
    private boolean isFile;
    @SerializedName("local")
    private String localFilter;

    public ListFilterModel(String nameFilter, String textFilter, boolean isFile, String localFilter) {
        this.nameFilter = nameFilter;
        this.textFilter = textFilter;
        this.isFile = isFile;
        this.localFilter = localFilter;
    }

    protected ListFilterModel(Parcel in) {
        nameFilter = in.readString();
        textFilter = in.readString();
        isFile = in.readByte() != 0;
        localFilter = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(nameFilter);
        dest.writeString(textFilter);
        dest.writeByte((byte) (isFile ? 1 : 0));
        dest.writeString(localFilter);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ListFilterModel> CREATOR = new Creator<ListFilterModel>() {
        @Override
        public ListFilterModel createFromParcel(Parcel in) {
            return new ListFilterModel(in);
        }

        @Override
        public ListFilterModel[] newArray(int size) {
            return new ListFilterModel[size];
        }
    };

    public String getNameFilter() {
        return nameFilter;
    }

    public void setNameFilter(String nameFilter) {
        this.nameFilter = nameFilter;
    }

    public String getTextFilter() {
        return textFilter;
    }

    public void setTextFilter(String textFilter) {
        this.textFilter = textFilter;
    }

    public boolean isFile() {
        return isFile;
    }

    public void setFile(boolean file) {
        isFile = file;
    }

    public String getLocalFilter() {
        return localFilter;
    }

    public void setLocalFilter(String localFilter) {
        this.localFilter = localFilter;
    }
}
